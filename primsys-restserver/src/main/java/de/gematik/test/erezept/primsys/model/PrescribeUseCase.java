/*
 * Copyright (c) 2023 gematik GmbH
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
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
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
import org.hl7.fhir.r4.model.Practitioner;

@Slf4j
public class PrescribeUseCase {

  private PrescribeUseCase() {
    throw new AssertionError();
  }

  public static Response issuePrescription(Doctor doctor, String kbvBundleXmlBody) {
    val kbvBundle = decodeKbvBundle(doctor, kbvBundleXmlBody);
    val originalFlowType = PrescriptionFlowType.fromPrescriptionId(kbvBundle.getPrescriptionId());
    return issuePrescription(doctor, kbvBundle, originalFlowType.isDirectAssignment());
  }

  public static Response issuePrescription(
      Doctor doctor, String kbvBundleXmlBody, boolean isDirectAssignment) {
    val kbvBundle = decodeKbvBundle(doctor, kbvBundleXmlBody);
    return issuePrescription(doctor, kbvBundle, isDirectAssignment);
  }

  public static Response issuePrescription(Doctor doctor, PrescribeRequest body) {
    return issuePrescription(doctor, body, false);
  }

  public static Response issuePrescription(
      Doctor doctor, PrescribeRequest body, boolean isDirectAssignment) {
    if (body.getPatient() == null || Strings.isNullOrEmpty(body.getPatient().getKvnr())) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(new ErrorResponse("KVNR is required field for the body"))
              .build());
    }
    val kbvBundle = getKbvBundleFromRequest(doctor.getName(), body);
    return issuePrescription(doctor, kbvBundle, isDirectAssignment);
  }

  public static Response issuePrescription(Doctor doctor, KbvErpBundle kbvBundle) {
    return issuePrescription(doctor, kbvBundle, false);
  }

  public static Response issuePrescription(
      Doctor doctor, KbvErpBundle kbvBundle, boolean isDirectAssignment) {

    val insuranceKind = kbvBundle.getCoverage().getInsuranceKind();
    val flowType = PrescriptionFlowType.fromInsuranceKind(insuranceKind, isDirectAssignment);
    val create = new TaskCreateCommand(flowType);
    log.info(
        format(
            "Doctor {0} creates new ''{1}'' Task wit FlowType {2}",
            doctor.getName(), flowType.toString(), flowType.getCode()));
    val createResponse = doctor.erpRequest(create);
    val draftTask =
        createResponse
            .getResourceOptional()
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(createResponse.getStatusCode())
                            .entity(new ErrorResponse(createResponse))
                            .build()));

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
    if (kbvBundle.isMultiple()) {
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

    val activatedTask =
        activateResponse
            .getResourceOptional()
            .orElseThrow(
                () ->
                    new WebApplicationException(
                        Response.status(activateResponse.getStatusCode())
                            .entity(new ErrorResponse(activateResponse))
                            .build()));

    val kvnr = kbvBundle.getKvnr();
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
    val patient =
        getPatientFromRequest(body.getPatient(), body.getCoverage().getEnumInsuranceKind());
    val coverage = getCoverageFromRequest(patient, body.getCoverage());
    val medication = getMedicationFromRequest(body.getMedication());
    val medicationRequest =
        getMedicationRequest(patient, coverage, practitioner, medication, body.getMedication());

    val kbvBuilder =
        KbvErpBundleBuilder.forPrescription(GemFaker.fakerPrescriptionId())
            .practitioner(practitioner)
            .custodian(organization)
            .patient(patient)
            .insurance(coverage)
            .medicationRequest(medicationRequest) // what is the medication
            .medication(medication);

    val isPkv = coverage.getInsuranceKind() == VersicherungsArtDeBasis.PKV;
    val isOldProfile = KbvItaForVersion.getDefaultVersion().compareTo(KbvItaForVersion.V1_0_3) == 0;
    if (isPkv && isOldProfile) {
      // assigner organization was only required in KbvItaFor 1.0.3
      // for now, we do not have the AssignerOrganization (which was faked anyway for getting a
      // Reference + Name
      // build a faked one matching the Reference of the patient
      val fakedAssignerOrganization = AssignerOrganizationBuilder.faker(patient);
      kbvBuilder.assigner(fakedAssignerOrganization.build());
    }

    return kbvBuilder.build();
  }

  private static KbvPatient getPatientFromRequest(
      PatientData patientData, VersicherungsArtDeBasis insuranceKind) {
    val patientBuilder =
        PatientBuilder.builder()
            .kvnr(KVNR.from(patientData.getKvnr()), insuranceKind)
            .name(patientData.getFirstName(), patientData.getLastName())
            .birthDate(patientData.getBirthDate())
            .address(
                Country.D, patientData.getCity(), patientData.getPostal(), patientData.getStreet());

    val isPkv = insuranceKind == VersicherungsArtDeBasis.PKV;
    val isOldProfile = KbvItaForVersion.getDefaultVersion().compareTo(KbvItaForVersion.V1_0_3) == 0;
    if (isPkv && isOldProfile) {
      // for now, we do not have the AssignerOrganization (which was faked anyway for getting a
      // Reference + Name
      // build a faked one matching the Reference of the patient
      patientBuilder.assigner(AssignerOrganizationBuilder.faker().build());
    }

    return patientBuilder.build();
  }

  private static KbvCoverage getCoverageFromRequest(KbvPatient patient, CoverageData coverageData) {
    return KbvCoverageBuilder.insurance(coverageData.getIknr(), coverageData.getInsuranceName())
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
      KbvCoverage insurance,
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

  private static KbvErpBundle decodeKbvBundle(Doctor doctor, String kbvBundleXmlBody) {
    try {
      return doctor.getClient().getFhir().decode(KbvErpBundle.class, kbvBundleXmlBody);
    } catch (DataFormatException dfe) {
      throw new WebApplicationException(
          Response.status(400)
              .entity(new ErrorResponse("Given KBV Bundle cannot be parsed"))
              .build());
    }
  }
}
