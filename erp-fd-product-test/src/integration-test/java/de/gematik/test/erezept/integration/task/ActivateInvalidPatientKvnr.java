/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
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
@DisplayName("Invalide KVNR im Patient in der Verordnung")
public class ActivateInvalidPatientKvnr extends ErpTest {

  @Actor(name = "Gündüla Gunther")
  private static DoctorActor doc;

  @Actor(name = "Leonie Hütter")
  private static PatientActor patient;

  private static Stream<Arguments> baseKVNRComposer() {
    var res =
        ArgumentComposer.composeWith()
            .arguments(
                "1234567890",
                "der führende Buchstabe fehlt",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .arguments(
                "B123456789",
                "die Prüfziffer 9 statt 2 ist",
                ErpAfos.A_23890,
                "Ungültige Versichertennummer (KVNR): Die übergebene Versichertennummer des Patienten entspricht nicht den Prüfziffer-Validierungsregeln.")
            .arguments(
                "A111111111",
                "die Prüfziffer 1 statt 4 ist",
                ErpAfos.A_23890,
                "Ungültige Versichertennummer (KVNR): Die übergebene Versichertennummer des Patienten entspricht nicht den Prüfziffer-Validierungsregeln.")
            .arguments(
                "a123456789",
                "die Prüfziffer 9 statt 2 ist und mit einem Kleinbuchstabe beginnt",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .arguments(
                "X1234567890",
                "die Prüfziffer 9 statt 2 ist und die KVNR zu lang ist",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .arguments(
                "E12345678",
                "die Prüfziffer fehlt",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .arguments(
                "E1234567",
                "die KVNR zu kurz ist und die Prüfziffer fehlt",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .arguments(
                "B123C56789",
                "ein 'C' an 5. Stelle ist",
                KbvProfileRules.KVNR_VALIDATION,
                "FHIR-Validation error")
            .multiply(
                List.of(
                    VersicherungsArtDeBasis.PKV,
                    VersicherungsArtDeBasis.GKV,
                    VersicherungsArtDeBasis.BG))
            .multiply(PrescriptionAssignmentKind.class)
            .create();
    return res;
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_KVNR_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt stellt ein E-Rezept für einen {1}-Versicherten  als {0} mit invalider KVNR: {2} ein, da {3}. ")
  @DisplayName(
      "Es muss geprüft werden, dass die KVNR in der Patient Ressource korrekt validiert wird")
  @MethodSource("baseKVNRComposer")
  void activateInvalidPatientInBundle(
      PrescriptionAssignmentKind assignmentKind,
      VersicherungsArtDeBasis insuranceType,
      String kvnr,
      String declaration,
      RequirementsSet requirementsSet,
      String detailedText) {

    patient.changePatientInsuranceType(insuranceType);

    val issuePrescription =
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(assignmentKind)
            .withResourceManipulator(
                kbvBundle -> kbvBundle.getPatient().getIdentifierFirstRep().setValue(kvnr))
            .withRandomKbvBundle();
    val activation = doc.performs(issuePrescription);

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(requirementsSet)
            .hasResponseWith(returnCode(400, requirementsSet))
            .and(operationOutcomeHasDetailsText(detailedText, requirementsSet))
            .isCorrect());
  }
}
