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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.isInProgressStatus;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ReceiptBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.AbsoluteReferencesAsUUIDToggle;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@Tag("Smoketest")
@Tag("UseCase:Close")
@DisplayName("E-Rezept dispensieren")
class TaskCloseUsecase extends ErpTest {

  private static final Boolean expectFullUrlAsUuidInReceipt =
      featureConf.getToggle(new AbsoluteReferencesAsUUIDToggle());

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  private static Stream<Arguments> prescriptionTypesProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY, // given assignment kind
            PrescriptionFlowType.FLOW_TYPE_160) // expected flow type
        .arguments(
            VersicherungsArtDeBasis.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_169)
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            PrescriptionFlowType.FLOW_TYPE_209)
        .create();
  }

  @TestcaseId("ERP_TASK_CLOSE_01")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName("Dispensieren eines E-Rezeptes als Abgebende Apotheke")
  @MethodSource("prescriptionTypesProvider")
  void closeTask(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
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

    val acceptation = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation = flughafen.performs(ClosePrescription.acceptedWith(acceptation));

    val verifier =
        Verify.that(dispensation)
            .withExpectedType(ErpAfos.A_19230)
            .hasResponseWith(returnCode(200));

    if (expectFullUrlAsUuidInReceipt) {
      verifier
          .and(entryFullUrlIsUuid())
          .and(compAuthorRefIsUuid())
          .and(signatureRefIsUuid())
          .and(compSectionRefIsUuid());
    }
    flughafen.attemptsTo(verifier.isCorrect());
  }

  @TestcaseId("ERP_TASK_CLOSE_02")
  @ParameterizedTest(name = "[{index}] -> Dispensiere ein {0} E-Rezept für {1} aus")
  @DisplayName(
      "Die TelematikID in der MedicationDispense muss gegen den ACCESS_TOKEN geprüft werden")
  @MethodSource("prescriptionTypesProvider")
  void closeTaskWithFakedTelematikId(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
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

    val acceptation = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    // see https://twitter.com/H3NK3P3NK/status/1543168037204918273?t=zwpWZN_6uvd54GlfA0gvxw&s=09
    val dispensation =
        flughafen.performs(
            ClosePrescription.alternative().performer("I don't care!").acceptedWith(acceptation));
    flughafen.attemptsTo(
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
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
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

    val acceptation = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation =
        flughafen.performs(
            ClosePrescription.alternative()
                .prescriptionId(PrescriptionId.random())
                .acceptedWith(acceptation));
    flughafen.attemptsTo(
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
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      PrescriptionFlowType expectedFlowType) {

    sina.changePatientInsuranceType(insuranceType);

    val activation =
        doctor.performs(
            IssuePrescription.forPatient(sina)
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

    val acceptation = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());

    val dispensation =
        flughafen.performs(
            ClosePrescription.alternative().kvnr(KVNR.random()).acceptedWith(acceptation));
    flughafen.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_19248)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }
}
