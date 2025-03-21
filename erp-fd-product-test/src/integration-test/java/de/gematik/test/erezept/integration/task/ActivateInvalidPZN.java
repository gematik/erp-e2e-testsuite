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
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
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
@DisplayName("Invalide PZN in Medication PZN or Compounding ")
public class ActivateInvalidPZN extends ErpTest {
  @Actor(name = "Leonie Hütter")
  private static PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private static DoctorActor doc;

  private static Stream<Arguments> basePZNTestcaseComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "11111114",
            "da die letzte Ziffer (Prüfziffer) eine 6 statt einer 4 beträgt",
            "Ungültige PZN: Die übergebene Pharmazentralnummer entspricht nicht den"
                + " vorgeschriebenen Prüfziffer-Validierungsregeln.")
        .arguments(
            "99999999",
            "da die letzte Ziffer (Prüfziffer) eine 9 statt einer 6 beträgt",
            "Ungültige PZN: Die übergebene Pharmazentralnummer entspricht nicht den"
                + " vorgeschriebenen Prüfziffer-Validierungsregeln.")
        .arguments(
            "24888481",
            "da die letzte Ziffer (Prüfziffer) eine 10 sein würde (durch die 1 dargestellt), was"
                + " verboten ist",
            "Ungültige PZN: Die übergebene Pharmazentralnummer entspricht nicht den"
                + " vorgeschriebenen Prüfziffer-Validierungsregeln.")
        .arguments(
            "A2345678",
            "da das erste Zeichen ein Buchstabe ist",
            "Ungültige PZN: Die übergebene Pharmazentralnummer entspricht nicht den"
                + " vorgeschriebenen Prüfziffer-Validierungsregeln.")
        .arguments(
            "1234Y678",
            "da ein Buchstabe enthalten ist",
            "Ungültige PZN: Die übergebene Pharmazentralnummer entspricht nicht den"
                + " vorgeschriebenen Prüfziffer-Validierungsregeln.")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .create();
  }

  private static Stream<Arguments> basePZNTestcaseComposerAsFhirValidationError() {
    return ArgumentComposer.composeWith()
        .arguments("2488849", "da die PZN zu kurz ist", "FHIR-Validation error")
        .arguments("123456789", "da die PZN zu lang ist", "FHIR-Validation error")
        .multiply(0, List.of(InsuranceTypeDe.BG, InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
        .multiply(1, PrescriptionAssignmentKind.class)
        .create();
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_PZN_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt Gündüla Gunther stellt ein E-Rezept mit invalider PZN:"
              + " {2} für den Kostenträger {0} als Darreichungsform {1} innerhalb einer"
              + " Medication_PZN Ressourceaus, {3}.")
  @DisplayName(
      "Es muss geprüft werden, dass die PZN in der Medication_PZN Ressource korrekt validiert wird"
          + " (ErpAfos.A_23892)")
  @MethodSource("basePZNTestcaseComposer")
  void activateInvalidPznInMedicationPZN(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      String pzn,
      String declaration,
      String detailedText) {

    patient.changePatientInsuranceType(insuranceType);
    val medication =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake();
    val activation = doc.performs(getIssuePrescription(assignmentKind, doc, medication));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_23892)
            .hasResponseWith(returnCode(400, ErpAfos.A_23892))
            .and(operationOutcomeHasDetailsText(detailedText, ErpAfos.A_23892))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_PZN_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt Gündüla Gunther stellt ein E-Rezept mit invalider PZN:"
              + " {2} für den Kostenträger {0} als Darreichungsform {1} innerhalb einer"
              + " Medication_Compounding Ressource aus, {3}.")
  @DisplayName(
      "Es muss geprüft werden, dass die PZN in der MedicationCompounding Ressource korrekt"
          + " validiert wird (ErpAfos.A_24034)")
  @MethodSource("basePZNTestcaseComposer")
  void activateInvalidPznInMedicationCompounding(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      String pzn,
      String declaration,
      String detailedText) {

    patient.changePatientInsuranceType(insuranceType);
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(pzn, fakerDrugName(), "freitext")
            .fake();

    val activation = doc.performs(getIssuePrescription(assignmentKind, doc, medication));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(ErpAfos.A_24034)
            .hasResponseWith(returnCode(400, ErpAfos.A_24034))
            .and(operationOutcomeHasDetailsText(detailedText, ErpAfos.A_24034))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_PZN_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt Gündüla Gunther stellt ein E-Rezept mit invalider PZN:"
              + " {2} für den Kostenträger {0} als Darreichungsform {1} innerhalb einer"
              + " Medication_PZN Ressourceaus, {3}.")
  @DisplayName(
      "Es muss geprüft werden, dass die PZN in der Medication_PZN Ressource korrekt validiert wird"
          + " (FHIR-Validation_Error)")
  @MethodSource("basePZNTestcaseComposerAsFhirValidationError")
  void activateInvalidPznInMedicationPznAsFhirValidationError(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      String pzn,
      String declaration,
      String detailedText) {

    patient.changePatientInsuranceType(insuranceType);
    val medication =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake();
    val activation = doc.performs(getIssuePrescription(assignmentKind, doc, medication));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(FhirRequirements.FHIR_VALIDATION_ERROR)
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_VALIDATION_ERROR))
            .and(
                operationOutcomeHasDetailsText(
                    detailedText, FhirRequirements.FHIR_VALIDATION_ERROR))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_INVALID_PZN_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Verordnender Arzt Gündüla Gunther stellt ein E-Rezept mit invalider PZN:"
              + " {2} für den Kostenträger {0} als Darreichungsform {1} innerhalb einer"
              + " Medication_Compounding Ressource aus, {3}.")
  @DisplayName(
      "Es muss geprüft werden, dass die PZN in der MedicationCompounding Ressource korrekt"
          + " validiert wird (FHIR-Validation_Error)")
  @MethodSource("basePZNTestcaseComposerAsFhirValidationError")
  void activateInvalidPznInMedicationCompoundingAsFhirValidationError(
      InsuranceTypeDe insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      String pzn,
      String declaration,
      String detailedText) {

    patient.changePatientInsuranceType(insuranceType);
    // @ freetext is a maximum length of 20 digits allowed
    val medication =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(pzn, "teurer Schleim", "4 mal täglich")
            .fake();

    val activation = doc.performs(getIssuePrescription(assignmentKind, doc, medication));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome(FhirRequirements.FHIR_VALIDATION_ERROR)
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_VALIDATION_ERROR))
            .and(
                operationOutcomeHasDetailsText(
                    detailedText, FhirRequirements.FHIR_VALIDATION_ERROR))
            .isCorrect());
  }

  private IssuePrescription getIssuePrescription(
      PrescriptionAssignmentKind assignmentKind,
      DoctorActor doctorActor,
      KbvErpMedication medication) {

    AccidentExtension accident = null;
    if (patient.getPatientInsuranceType().equals(InsuranceTypeDe.BG))
      accident = AccidentExtension.accidentAtWork().atWorkplace();

    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPractitioner(doctorActor.getPractitioner())
            .withMedication(medication)
            .withInsurance(patient.getInsuranceCoverage(), patient.getPatientData())
            .withAccident(accident)
            .toBuilder();

    return IssuePrescription.forPatient(patient)
        .ofAssignmentKind(assignmentKind)
        .withKbvBundleFrom(kbvBundleBuilder);
  }
}
