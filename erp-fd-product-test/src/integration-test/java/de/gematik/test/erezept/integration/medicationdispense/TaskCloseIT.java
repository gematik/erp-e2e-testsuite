/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.integration.medicationdispense;

import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.isInProgressStatus;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleHasLastMedicationDispenseDateAfterClose;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.prescriptionInStatus;
import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.compAuthorRefIsUuid;
import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.compSectionRefIsUuid;
import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.entryFullUrlIsUuid;
import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.signatureRefIsUuid;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@Tag("Smoketest")
@Tag("UseCase:Close")
@DisplayName("E-Rezept dispensieren")
class TaskCloseIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  private static Stream<Arguments> prescriptionTypesProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            InsuranceTypeDe.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY, // given assignment kind
            PrescriptionFlowType.FLOW_TYPE_160) // expected flow type
        .arguments(
            InsuranceTypeDe.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_169)
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            InsuranceTypeDe.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209)
        .create();
  }

  @TestcaseId("ERP_TASK_CLOSE_01")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName("Dispensieren eines E-Rezeptes als Abgebende Apotheke")
  @MethodSource("prescriptionTypesProvider")
  void closeTask(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .isCorrect());

    val task = activation.getExpectedResponse();

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation = pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    val verifier =
        Verify.that(dispensation)
            .withExpectedType(ErpAfos.A_19230)
            .hasResponseWith(returnCode(200))
            .and(entryFullUrlIsUuid())
            .and(compAuthorRefIsUuid())
            .and(signatureRefIsUuid())
            .and(compSectionRefIsUuid());

    pharmacy.attemptsTo(verifier.isCorrect());

    pharmacy.attemptsTo(verifier.isCorrect());
  }

  @TestcaseId("ERP_TASK_CLOSE_02")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName(
      "Die TelematikID in der MedicationDispense muss gegen den ACCESS_TOKEN geprüft werden")
  @MethodSource("prescriptionTypesProvider")
  void closeTaskWithFakedTelematikId(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .isCorrect());

    val task = activation.getExpectedResponse();

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation =
        pharmacy.performs(
            ClosePrescription.alternative().performer("I don't care!").acceptedWith(acceptation));
    pharmacy.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_19248)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_CLOSE_03")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName(
      "Die PrescriptionId in der MedicationDispense muss gegen die ursprüngliche Verordnung geprüft"
          + " werden")
  @MethodSource("prescriptionTypesProvider")
  void closeTaskWithInvalidPrescriptionId(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .isCorrect());

    val task = activation.getExpectedResponse();

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation =
        pharmacy.performs(
            ClosePrescription.alternative()
                .prescriptionId(PrescriptionId.random())
                .acceptedWith(acceptation));
    pharmacy.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_19248)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_CLOSE_04")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName(
      "Die KVNR in der MedicationDispense muss gegen die ursprüngliche KVNR der Verordnung geprüft"
          + " werden")
  @MethodSource("prescriptionTypesProvider")
  void closeTaskWithInvalidKvnr(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    patient.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(hasWorkflowType(expectedFlowType))
            .and(isInReadyStatus())
            .isCorrect());

    val task = activation.getExpectedResponse();

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task));
    pharmacy.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation =
        pharmacy.performs(
            ClosePrescription.alternative().kvnr(KVNR.random()).acceptedWith(acceptation));
    pharmacy.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_19248)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_CLOSE_05")
  @Test
  @DisplayName(
      "Prüfe, dass ein patient beim Abruf seiner MedicationDispense bei einem Close die"
          + " lastMedicationDispense Informationen der Dispensierung vorliegen")
  void shouldDownloadMedicDispenseWithAllInformation() {
    val task =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();

    shouldDownloadMedicDispenseWithAllInformation(acceptation);
  }

  private void shouldDownloadMedicDispenseWithAllInformation(ErxAcceptBundle acceptation) {
    val task = acceptation.getTask();
    val dispensation = pharmacy.performs(ClosePrescription.acceptedWith(acceptation));

    pharmacy.attemptsTo(Verify.that(dispensation).withExpectedType().isCorrect());

    val prescription =
        patient.performs(GetPrescriptionById.withTaskId(task.getTaskId()).withoutAuthentication());

    patient.attemptsTo(
        Verify.that(prescription)
            .withExpectedType()
            .has(bundleHasLastMedicationDispenseDateAfterClose())
            .is(prescriptionInStatus(Task.TaskStatus.COMPLETED))
            .isCorrect());
  }
}
