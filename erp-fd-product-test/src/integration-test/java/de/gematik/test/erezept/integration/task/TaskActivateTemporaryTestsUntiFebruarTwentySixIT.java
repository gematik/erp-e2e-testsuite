/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept ausstellen")
@Tag("ActivateWithOltProfiles")
class TaskActivateTemporaryTestsUntiFebruarTwentySixIT extends ErpTest {
  private static final KbvItaErpVersion itaErpVersion = KbvItaErpVersion.V1_3_0;
  private static final KbvItaForVersion itaForVersion = KbvItaForVersion.V1_2_0;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doc;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_01")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer PZN-Verordnung, Freitextverordnung oder"
          + " Wirkstoffverordnung muss OHNE Dosierkennzeichen und MIT Dosierung der Fachdienst den"
          + " Fehler detektiert die Verordnung ablehnt.")
  void submitDosageInstructionWithoutDosageFlagShouldFail() {

    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication = KbvErpMedicationPZNFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(dosage -> dosage.setExtension(List.of())))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeDosierkennzeichenPflicht: Bei einer PZN-Verordnung,"
                        + " Freitextverordnung oder Wirkstoffverordnung muss das Dosierkennzeichen"
                        + " angegeben werden, wenn kein Sprechstundenbedarf verordnet wird und eine"
                        + " Dosierung angegeben werden soll",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_02")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer Rezeptur-Verordnung der FD beim Vorhanden sein eines"
          + " Dosierkennzeichen den Fehler detektiert und die Verordnung ablehnt.")
  void submitCompoundingMedicationWithDosageFlagShouldFail() {
    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(
                                dosage ->
                                    dosage.setExtension(
                                        List.of(
                                            KbvItaErpStructDef.DOSAGE_FLAG.asBooleanExtension(
                                                true)))))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeDosierungRezepturVerbot: Bei einer Rezepturverordnung dürfen"
                        + " Dosierkennzeichen und Dosieranweisung nicht angegeben werden.",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_03")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer Rezeptur-Verordnung der FD beim Vorhanden sein einer"
          + " Dosieranweisung den Fehler detektiert und die Verordnung ablehnt.")
  void submitCompoundingMedicationWithDosageInstructionShouldFail() {
    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(dosage -> dosage.setText("Dosieranweisung xy")))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeDosierungRezepturVerbot: Bei einer Rezepturverordnung dürfen"
                        + " Dosierkennzeichen und Dosieranweisung nicht angegeben werden.",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_04")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer Rezeptur-Verordnung der FD beim Vorhanden sein einer"
          + " Dosieranweisung den Fehler detektiert und die Verordnung ablehnt.")
  void submitPZNMedicationWithPatientInstructionShouldFail() {

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationCompoundingFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(dosage -> dosage.setPatientInstruction(null)))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeGebrauchsanweisungPflicht: Die Gebrauchsanweisung"
                        + " (dosageInstruction.patientInstruction) fehlt, muss bei einer"
                        + " Rezepturverordnung aber angegeben werden.",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_05")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer Ingredient-Verordnung der FD beim Vorhanden sein"
          + " einer PatientInstruction den Fehler detektiert und die Verordnung ablehnt.")
  void submitIngredientMedicationWithPatientInstructionShouldFail() {
    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication =
        KbvErpMedicationIngredientFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(
                                dosage ->
                                    dosage.setPatientInstruction(
                                        "Eine Gebrauchsanweisung ist auf dem Heap, Yeah!")))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeGebrauchsanweisungVerbot: Eine Gebrauchsanweisung"
                        + " (dosageInstruction.patientInstruction) darf nur angegeben werden, wenn"
                        + " es sich um eine Rezepturverordnung handelt.",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_ACTIVATE_TEMP_06")
  @Test()
  @DisplayName(
      "Es muss geprüft werden, dass Bei einer FreiText-Verordnung der FD beim Vorhanden sein einer"
          + " PatientInstruction den Fehler detektiert und die Verordnung ablehnt.")
  void submitFreeTextMedicationWithoutPatientInstructionShouldFail() {
    val dosageInstruction = "Dosieranweisung im Textfeld";

    doc.setVersion(itaForVersion);
    patient.setVersion(itaForVersion);

    val medication = KbvErpMedicationFreeTextFaker.builder(itaErpVersion).withVaccine(false).fake();
    val activation =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getDosageInstruction()
                            .forEach(
                                dosage ->
                                    dosage.setPatientInstruction(
                                        "Eine Gebrauchsanweisung ist auf dem Heap, Yeah!")))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder(itaErpVersion, itaForVersion)
                        .withMedication(medication)
                        .withDosageInstruction(dosageInstruction)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(activation)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIs(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "-erp-angabeGebrauchsanweisungVerbot: Eine Gebrauchsanweisung"
                        + " (dosageInstruction.patientInstruction) darf nur angegeben werden, wenn"
                        + " es sich um eine Rezepturverordnung handelt.",
                    FhirRequirements.FHIR_PROFILES))
            .isCorrect());
  }
}
