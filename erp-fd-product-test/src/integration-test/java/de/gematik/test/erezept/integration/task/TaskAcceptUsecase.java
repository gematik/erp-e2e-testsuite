/*
 * Copyright 2023 gematik GmbH
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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.hasWorkflowType;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;
import static de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType.*;
import static de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis.*;
import static de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind.*;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.DispensePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept akzeptieren")
@Tag("UseCase:Accept")
class TaskAcceptUsecase extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @TestcaseId("ERP_TASK_ACCEPT_01")
  @ParameterizedTest(name = "[{index}] -> Akzeptiere ein {0} E-Rezept für {1}")
  @DisplayName("Akzeptieren eines E-Rezeptes als Abgebende Apotheke")
  @MethodSource("prescriptionTypesProvider")
  void acceptTask(
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

    val acceptance = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(acceptance)
            .withExpectedType(ErpAfos.A_19166)
            .hasResponseWith(returnCode(200))
            .and(isInProgressStatus())
            .isCorrect());
    // cleanup
    flughafen.performs(DispensePrescription.acceptedWith(acceptance));
  }

  @TestcaseId("ERP_TASK_ACCEPT_02")
  @Test
  @DisplayName("Wiederholtes ACCEPT von Rezepten als Apotheker nicht möglich")
  void acceptTwiceShouldNotWork() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(sina).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    flughafen.performs(AcceptPrescription.forTheTask(task));
    val accepted2 = flughafen.performs(AcceptPrescription.forTheTask(task));
    flughafen.attemptsTo(
        Verify.that(accepted2)
            .withOperationOutcome(ErpAfos.A_19168_01)
            .responseWith(returnCodeIs(409))
            .and(
                operationOutcomeHasDetailsText(
                    "Task is processed by requesting institution", ErpAfos.A_19168_01))
            .isCorrect());
  }

  static Stream<Arguments> prescriptionTypesProvider() {
    val composer =
        ArgumentComposer.composeWith()
            .arguments(GKV, PHARMACY_ONLY, FLOW_TYPE_160)
            .arguments(GKV, DIRECT_ASSIGNMENT, FLOW_TYPE_169)
            .arguments(PKV, PHARMACY_ONLY, FLOW_TYPE_200)
            .arguments(PKV, DIRECT_ASSIGNMENT, FLOW_TYPE_209);

    return composer.create();
  }


}
