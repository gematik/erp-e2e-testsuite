/*
 * Copyright 2024 gematik GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.primsys.actors.Doctor;
import de.gematik.test.erezept.primsys.data.PrescribeRequestDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.mapping.*;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponseBuilder;
import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrescribeUseCase {

  private PrescribeUseCase() {
    throw new AssertionError();
  }

  public static Response issuePrescription(Doctor doctor, String kbvBundleXmlBody) {
    val kbvBundle = doctor.decode(KbvErpBundle.class, kbvBundleXmlBody);
    val originalFlowType = PrescriptionFlowType.fromPrescriptionId(kbvBundle.getPrescriptionId());
    return issuePrescription(doctor, kbvBundle, originalFlowType.isDirectAssignment());
  }

  public static Response issuePrescription(
      Doctor doctor, String kbvBundleXmlBody, boolean isDirectAssignment) {
    val kbvBundle = doctor.decode(KbvErpBundle.class, kbvBundleXmlBody);
    return issuePrescription(doctor, kbvBundle, isDirectAssignment);
  }

  public static Response issuePrescription(Doctor doctor, PrescribeRequestDto body) {
    return issuePrescription(doctor, body, false);
  }

  public static Response issuePrescription(
      Doctor doctor, PrescribeRequestDto body, boolean isDirectAssignment) {
    val bodyMapper = PrescribeRequestDataMapper.from(body);

    if (!bodyMapper.getPatientMapper().hasKvnr()) {
      throw ErrorResponseBuilder.createInternalErrorException(
          400, "KVNR is required field for the body");
    }

    val kbvBundle = bodyMapper.createKbvBundle(doctor.getName());
    return issuePrescription(doctor, kbvBundle, isDirectAssignment);
  }

  public static Response issuePrescription(
      Doctor doctor, KbvErpBundle kbvBundle, boolean isDirectAssignment) {

    val insuranceKind =
        kbvBundle.getCoverage().getInsuranceKindOptional().orElse(VersicherungsArtDeBasis.GKV);
    val flowType = PrescriptionFlowType.fromInsuranceKind(insuranceKind, isDirectAssignment);
    val create = new TaskCreateCommand(flowType);
    log.info(
        format(
            "Doctor {0} creates new ''{1}'' Task wit FlowType {2}",
            doctor.getName(), flowType.toString(), flowType.getCode()));
    val createResponse = doctor.erpRequest(create);
    val draftTask = createResponse.getExpectedResource();

    // make sure we get the value of the prescription ID but still remain the system from the
    // original kbv bundle because otherwise we will have a version mixture
    val draftPrescriptionId = draftTask.getPrescriptionId();
    val kbvBundlePrescriptionIdentifier = kbvBundle.getPrescriptionId().asIdentifier();
    kbvBundlePrescriptionIdentifier.setValue(draftPrescriptionId.getValue());
    val prescriptionId = PrescriptionId.from(kbvBundlePrescriptionIdentifier);

    val taskId = draftTask.getTaskId();
    kbvBundle.setPrescriptionId(prescriptionId);
    kbvBundle.setId(taskId.getValue());
    // will update all dates contained within the KBV Bundle to current date
    kbvBundle.setAllDates();
    if (kbvBundle.getMedicationRequest().isMultiple()) {
      val mr = kbvBundle.getMedicationRequest();
      mr.updateMvoDates();
    }

    val kbvXml = doctor.getClient().encode(kbvBundle, EncodingType.XML);
    val signedKbv = doctor.signDocument(kbvXml);

    log.info(format("Activate Task (authoredOn {0}", kbvBundle.getAuthoredOn()));
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();
    val activate = new TaskActivateCommand(draftTask.getTaskId(), accessCode, signedKbv);
    val activateResponse = doctor.erpRequest(activate);
    log.info(format("FD answered on $activate with: {0}", activateResponse.getResourceType()));

    val activatedTask = activateResponse.getExpectedResource();

    val kvnr = kbvBundle.getPatient().getKvnr();
    val patientMapper = PatientDataMapper.from(kbvBundle.getPatient());
    val coverageMapper = CoverageDataMapper.from(kbvBundle.getCoverage(), kbvBundle.getPatient());
    val medicationMapper = PznMedicationDataMapper.from(kbvBundle.getMedication());
    val medicationRequestMapper =
        MedicationRequestDataMapper.from(kbvBundle.getMedicationRequest())
            .requestedBy(kbvBundle.getPractitioner())
            .requestedFor(kbvBundle.getPatient())
            .coveredBy(kbvBundle.getCoverage())
            .forMedication(kbvBundle.getMedication());
    val doctorData = doctor.getDoctorInformation(kbvBundle);

    log.info(
        format(
            "Doctor {0} issues Prescription {1} to {2}", doctor.getName(), prescriptionId, kvnr));
    val prescriptionData =
        PrescriptionDto.builder()
            .practitioner(doctorData)
            .prescriptionId(activatedTask.getPrescriptionId().getValue())
            .accessCode(activatedTask.getAccessCode().getValue())
            .taskId(activatedTask.getTaskId().getValue())
            .authoredOn(activatedTask.getAuthoredOn())
            .patient(patientMapper.getDto())
            .coverage(coverageMapper.getDto())
            .medication(medicationMapper.getDto())
            .medicationRequest(medicationRequestMapper.getDto())
            .build();

    ActorContext.getInstance().addPrescription(prescriptionData);
    return Response.accepted(prescriptionData).build();
  }
}
