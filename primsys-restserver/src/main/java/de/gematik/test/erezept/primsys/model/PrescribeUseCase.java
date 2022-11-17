/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.parser.DataFormatException;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import de.gematik.test.erezept.primsys.rest.data.*;
import de.gematik.test.erezept.primsys.rest.request.PrescribeRequest;
import de.gematik.test.erezept.primsys.rest.response.ErrorResponse;
import de.gematik.test.erezept.primsys.utils.Strings;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coverage;
import org.hl7.fhir.r4.model.Practitioner;

@Slf4j
public class PrescribeUseCase {

  private PrescribeUseCase() {
    throw new AssertionError();
  }

  public static Response issuePrescription(Doctor doctor, String kbvBundleXmlBody) {
    KbvErpBundle kbvBundle;
    try {
      kbvBundle = doctor.getClient().getFhir().decode(KbvErpBundle.class, kbvBundleXmlBody);
    } catch (DataFormatException dfe) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(new ErrorResponse("Given KBV Bundle cannot be parsed"))
              .build());
    }
    return issuePrescription(doctor, kbvBundle);
  }

  public static Response issuePrescription(Doctor doctor, PrescribeRequest body) {
    if (body.getPatient() == null || Strings.isNullOrEmpty(body.getPatient().getKvnr())) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(new ErrorResponse("KVNR is required field for the body"))
              .build());
    }
    val kbvBundle = getKbvBundleFromRequest(doctor.getName(), body);
    return issuePrescription(doctor, kbvBundle);
  }

  public static Response issuePrescription(Doctor doctor, KbvErpBundle kbvBundle) {
    val flowType =
        kbvBundle.getInsuranceType().equals(VersicherungsArtDeBasis.GKV)
            ? PrescriptionFlowType.FLOW_TYPE_160
            : PrescriptionFlowType.FLOW_TYPE_200;
    val create = new TaskCreateCommand(flowType);
    val createResponse = doctor.erpRequest(create);
    val draftTask =
        createResponse
            .getResourceOptional(create.expectedResponseBody())
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(createResponse.getStatusCode())
                            .entity(new ErrorResponse(createResponse))
                            .build()));

    val prescriptionId = draftTask.getPrescriptionId();
    val taskId = draftTask.getUnqualifiedId();
    kbvBundle.setPrescriptionId(prescriptionId);
    kbvBundle.setId(taskId);
    kbvBundle
        .setAllDates(); // will update all dates contained within the KBV Bundle to current date

    val kbvXml = doctor.getClient().encode(kbvBundle, EncodingType.XML);
    val signedKbv = doctor.signDocument(kbvXml);

    log.info(format("Activate Task (authoredOn {0}", kbvBundle.getAuthoredOn()));
    val accessCode = draftTask.getOptionalAccessCode().orElseThrow();
    val activate = new TaskActivateCommand(draftTask.getUnqualifiedId(), accessCode, signedKbv);
    val activateResponse = doctor.erpRequest(activate);
    log.info(format("FD answered on $activate with: {0}", activateResponse.getResourceType()));

    val activatedTask =
        activateResponse
            .getResourceOptional(activate.expectedResponseBody())
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(activateResponse.getStatusCode())
                            .entity(new ErrorResponse(activateResponse))
                            .build()));

    val kvnr = kbvBundle.getKvid();
    val patientData = PatientData.fromKbvBundle(kbvBundle);
    val coverageData = CoverageData.fromKbvBundle(kbvBundle);
    val medication = MedicationData.fromKbvBundle(kbvBundle);
    val doctorData = DoctorData.fromKbvBundle(doctor, kbvBundle);

    log.info(
        format(
            "Doctor {0} issues Prescription {1} to {2}", doctor.getName(), prescriptionId, kvnr));
    val prescriptionData =
        PrescriptionData.create(doctorData, activatedTask, patientData, coverageData, medication);
    ActorContext.getInstance().addPrescription(prescriptionData);
    return Response.accepted(
            Map.of(
                "task-id",
                prescriptionData.getTaskId(),
                "access-code",
                prescriptionData.getAccessCode()))
        .build();
  }

  private static KbvErpBundle getKbvBundleFromRequest(String docName, PrescribeRequest body) {
    val practitioner = PractitionerBuilder.faker(docName).build();
    val organization = MedicalOrganizationBuilder.faker().build();
    val patient = getPatientFromRequest(body.getPatient());
    val coverage = getCoverageFromRequest(patient, body.getCoverage());
    val medication = getMedicationFromRequest(body.getMedication());
    val medicationRequest =
        getMedicationRequest(patient, coverage, practitioner, medication, body.getMedication());
    return KbvErpBundleBuilder.forPrescription(GemFaker.fakerPrescriptionId())
        .practitioner(practitioner)
        .custodian(organization)
        .patient(patient)
        .insurance(coverage)
        .medicationRequest(medicationRequest) // what is the medication
        .medication(medication)
        .build();
  }

  private static KbvPatient getPatientFromRequest(PatientData patientData) {
    return PatientBuilder.builder()
        .kvIdentifierDe(patientData.getKvnr(), IdentifierTypeDe.GKV) // Only GKV here for now!
        .name(patientData.getFirstName(), patientData.getLastName())
        .birthDate(patientData.getBirthDate())
        .address(Country.D, patientData.getCity(), patientData.getPostal(), patientData.getStreet())
        .build();
  }

  private static Coverage getCoverageFromRequest(KbvPatient patient, CoverageData coverageData) {
    return CoverageBuilder.insurance(coverageData.getIknr(), coverageData.getInsuranceName())
        .beneficiary(patient)
        .personGroup(coverageData.getEnumPersonGroup())
        .dmpKennzeichen(DmpKennzeichen.NOT_SET) // NOT SET YET
        .wop(coverageData.getEnumWop())
        .versichertenStatus(coverageData.getEnumInsuranceState())
        .versicherungsArt(coverageData.getEnumInsuranceKind())
        .build();
  }

  private static KbvErpMedication getMedicationFromRequest(MedicationData medicationData) {
    return KbvErpMedicationBuilder.builder()
        .category(medicationData.getEnumCategory())
        .isVaccine(false) // default false
        .normgroesse(medicationData.getEnumStandardSize())
        .darreichungsform(medicationData.getEnumDarreichungsForm())
        .amount(medicationData.getAmount(), "Stk")
        .pzn(medicationData.getPzn(), medicationData.getName())
        .build();
  }

  private static KbvErpMedicationRequest getMedicationRequest(
      KbvPatient patient,
      Coverage insurance,
      Practitioner practitioner,
      KbvErpMedication medication,
      MedicationData medicationData) {
    val builder =
        MedicationRequestBuilder.forPatient(patient)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .dosage(medicationData.getDosage())
            .quantityPackages(medicationData.getPackageQuantity())
            .status("active") // default ACTIVE
            .intent("order") // default ORDER
            .isBVG(false) // Bundesversorgungsgesetz default true
            .hasEmergencyServiceFee(true) // default false
            .substitution(medicationData.isSubstitutionAllowed())
            .coPaymentStatus(StatusCoPayment.STATUS_1); // default StatusCoPayment.STATUS_0
    if (medicationData.getMvoData() != null) {
      if (medicationData.getMvoData().isValid()) {
        builder.mvo(
            MultiplePrescriptionExtension.asMultiple(
                    medicationData.getMvoData().getNumerator(),
                    medicationData.getMvoData().getDenominator())
                .fromNow()
                .withoutEndDate());
      } else {
        throw new WebApplicationException(
            Response.status(400).entity(new ErrorResponse("MVO Data is invalid")).build());
      }
    } else {
      builder.mvo(MultiplePrescriptionExtension.asNonMultiple());
    }
    return builder.build();
  }
}
