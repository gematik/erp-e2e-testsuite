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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Invalider Versicherungs-Typ in der Verordnung")
public class ActivateInvalidCoverageType extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  private static Stream<Arguments> baseGkvComposer() {
    return ArgumentComposer.composeWith()
        .arguments(InsuranceTypeDe.GKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
        .arguments(InsuranceTypeDe.GKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .create();
  }

  private static Stream<Arguments> basePkvComposer() {
    return ArgumentComposer.composeWith()
        .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.PHARMACY_ONLY)
        .arguments(InsuranceTypeDe.PKV, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_COVERAGE_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Coverage-Code PKV"
              + " aus")
  @DisplayName(
      "Es muss geprüft werden, dass ein Task mit FlowType 160/169 nicht dem Coverage-Code 'PKV'"
          + " belegt ist")
  @MethodSource("baseGkvComposer")
  void activateGkvPrescriptionWithPkvCoverage(
      InsuranceTypeDe insuranceType, PrescriptionAssignmentKind assignmentKind) {

    sina.changePatientInsuranceType(insuranceType);

    val issuePrescription =
        IssuePrescription.forPatient(sina)
            .ofAssignmentKind(assignmentKind)
            .withResourceManipulator(
                kbvBundle -> kbvBundle.getCoverage().getType().getCodingFirstRep().setCode("PKV"))
            .withRandomKbvBundle();

    val activation = doctor.performs(issuePrescription);

    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_23443)
            .hasResponseWith(returnCode(400, ErpAfos.A_23443))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_COVERAGE_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} mit Coverage-Code GKV"
              + " aus")
  @DisplayName(
      "Es muss geprüft werden, dass ein Task mit FlowType 200/209 nicht dem Coverage-Code 'GKV'"
          + " belegt ist")
  @MethodSource("basePkvComposer")
  void activatePkvPrescriptionWithGkvCoverage(
      InsuranceTypeDe insuranceType, PrescriptionAssignmentKind assignmentKind) {

    sina.changePatientInsuranceType(insuranceType);

    val issuePrescription =
        IssuePrescription.forPatient(sina)
            .ofAssignmentKind(assignmentKind)
            .withResourceManipulator(
                kbvBundle -> kbvBundle.getCoverage().getType().getCodingFirstRep().setCode("GKV"))
            .withRandomKbvBundle();

    val activation = doctor.performs(issuePrescription);

    doctor.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_22347)
            .hasResponseWith(returnCode(400, ErpAfos.A_22347))
            .isCorrect());
  }
}
