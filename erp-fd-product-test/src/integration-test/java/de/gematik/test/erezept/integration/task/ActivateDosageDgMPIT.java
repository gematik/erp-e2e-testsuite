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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeDoesNotContainsInDiagnostics;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpBfd;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.definitions.DgMPStructDef;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.MarkdownType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Activate Prescriptions with contained DosageDgMP Objects")
@Tag("DosageDgMP")
class ActivateDosageDgMPIT extends ErpTest {
  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

  @Actor(name = "Gündüla Gunther")
  private DoctorActor doc;

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_01")
  @Test()
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert")
  void checkSubmittedPrescriptionsDosageDgMPInformation() {

    val dosageDgMP =
        DosageDgMPBuilder.dosageWhenEntry(
            6,
            BmpDosiereinheit.STUECK,
            List.of(
                Timing.EventTiming.MORN,
                Timing.EventTiming.NOON,
                Timing.EventTiming.EVE,
                Timing.EventTiming.NIGHT),
            6,
            UnitsOfTimeDE.WOCHE,
            2,
            Timing.UnitsOfTime.D);

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosageDgMP)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpBfd.B_FD_1661))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_02")
  @ParameterizedTest(name = "[{index}]: Manipulated rendered DosageInstruction is: {0} ")
  @ValueSource(
      strings = {
        "für 6 Wochen alle 2 Tage: morgens — je 1 Stück; mittags — je 1 Stück; abends — je 1 Stück;"
            + " nachts — je 1 Stück",
        "für 6 Wochen alle 2 Tage: morgens — je 1 Stück; mittags — je 1 Stück; abends — je 1 Stück;"
            + " zur Nacht — 1 Stück",
        "für 6 Wochen alle 2 Tage: am Morgen — je 1 Stück; mittags — je 1 Stück; abends — je 1"
            + " Stück; zur Nacht — je 1 Stück",
        "für 6 Wochen alle 2 Tage: morgens — je 1 Stück; mittags — je 1 Stück; an Abend — je 1"
            + " Stück; zur Nacht — je 1 Stück",
        "für 6 Wochen alle 3 Tage: morgens — je 1 Stück; mittags — je 1 Stück; abends — je 1 Stück;"
            + " zur Nacht — je 1 Stück",
        "für 6 Wochen alle 2 Tage: morgens — je 1 Stück; mittags — je 2 Stück; abends — je 1 Stück;"
            + " zur Nacht — je 1 Stück",
        "für 6 Monate alle 2 Tage: morgens — je 1 Stück; mittags — je 1 Stück; abends — je 1 Stück;"
            + " zur Nacht — je 1 Stück",
      })
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und falsche gerenderte Texte detektiert")
  void checkSubmittedPrescriptionsDosageDgMPInformationAndDetectFailedRendering(
      String manipulatedRenderedDosageInstruction) {

    val dosagDGMP =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(6, UnitsOfTimeDE.WOCHE)
                    .period(2, Timing.UnitsOfTime.D)
                    .frequency(4)
                    .when("MORN")
                    .when("NOON")
                    .when("EVE")
                    .when("NIGHT")
                    .build())
            .build();

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest().getExtension().stream()
                            .filter(DgMPStructDef.MR_RENDERED_DOSAGE_INSTRUCTION::matches)
                            .forEach(
                                ext ->
                                    ext.setValue(
                                        new MarkdownType(manipulatedRenderedDosageInstruction))))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosagDGMP)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, ErpBfd.B_FD_1661))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "expected: für 6 Wochen alle 2 Tage: morgens — je 1 Stück; mittags — je 1"
                        + " Stück; abends — je 1 Stück; zur Nacht — je 1 Stück",
                    ErpBfd.B_FD_1661))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_03")
  @Test()
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und die timing sortierreihenfolge ignoriert")
  void checkTimingSortInDosageDgMP() {
    List<DosageDgMP> dosageDgMPList = new ArrayList<>();
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("22:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("18:00:00")
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(3, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("01:00:00")
                    .build())
            .build());

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosageDgMPList)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpBfd.B_FD_1659))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_04")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und keinen gerenderten Texte als Fehler detektiert")
  void checkSubmittedPrescriptionsDosageDgMPInformationAndDetectFailWhileRenderedDosageIsMissing() {

    val dosagDGMP =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(6, UnitsOfTimeDE.WOCHE)
                    .period(2, Timing.UnitsOfTime.D)
                    .frequency(4)
                    .when("MORN")
                    .when("NOON")
                    .when("EVE")
                    .when("NIGHT")
                    .build())
            .build();

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getExtension()
                            .removeIf(DgMPStructDef.MR_RENDERED_DOSAGE_INSTRUCTION::matches))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosagDGMP)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, ErpBfd.B_FD_1661))
            .and(
                operationOutcomeContainsInDiagnostics(
                    " DosageStructuredRequiresGeneratedText: Liegt eine strukturierte"
                        + " Dosierungsangabe vor (timing und doseAndRate belegt, text leer), muss"
                        + " die Extension GeneratedDosageInstructionsMeta vorhanden sein.",
                    ErpBfd.B_FD_1661))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_05")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, fehle gerenderten Dosierinformationen"
          + " korrekt validiert und die fehlende DosageFlag detektiert")
  void checkSubmittedPrescriptionsDosageDgMPInformationAndDetectFailWhileDosageFlagIsMissing() {

    val dosagDGMP =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(6, UnitsOfTimeDE.WOCHE)
                    .period(2, Timing.UnitsOfTime.D)
                    .frequency(4)
                    .when("MORN")
                    .when("NOON")
                    .when("EVE")
                    .when("NIGHT")
                    .build())
            .build();

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest()
                            .getExtension()
                            .removeIf(DgMPStructDef.GENERATE_DOSAGE_INSTRUCTION_META::matches))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosagDGMP)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_PROFILES))
            .and(
                operationOutcomeContainsInDiagnostics(
                    " DosageStructuredRequiresGeneratedText: Liegt eine strukturierte"
                        + " Dosierungsangabe vor (timing und doseAndRate belegt, text leer), muss"
                        + " die Extension GeneratedDosageInstructionsMeta vorhanden sein.",
                    ErpBfd.B_FD_1661))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_06")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen DosageDgMP Einträge auf Konformität"
          + " validiert und ggf. zurück weist, z.B wenn Zeitpunkte und Zeitspannen gemischt"
          + " verwendet werden oder timings fehlen")
  void checkSubmittedPrescriptionsDosageDgMPAndDetectMixedSchemas() {

    List<DosageDgMP> dosageDgMPList = new ArrayList<>();
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(5, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(4)
                    .when("NOON")
                    .when("NIGHT")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(5, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.D)
                    .frequency(4)
                    .timeOfDay(List.of("14:11:00", "21:22:00"))
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .build())
            .build());
    dosageDgMPList.add(DosageDgMPBuilder.dosageBuilder(2.75, BmpDosiereinheit.STUECK).build());

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosageDgMPList)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_PROFILES))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "TimingOnlyOneType: Only one kind of Timing is allowed. Current allowed"
                        + " timings: 4-Scheme, TimeOfDay, DayOfWeek, Interval, DayOfWeek and"
                        + " Time/4-Schema, Interval and Time/4-Schema",
                    ErpBfd.B_FD_1676))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_07")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen DosageDgMP Einträge auf Konformität"
          + " validiert und bei Vorhandensein von DosageDgMP - Einträgen mit verschiedenen"
          + " Zeitangaben (in einem `morgens` in einem anderen '08:00:00' den fehlerhaften Aufbau"
          + " detektiert und zurück weist")
  void checkSubmittedPrescriptionsDosageDgMPAndDetectMixedSchemas2() {

    List<DosageDgMP> dosageDgMPList = new ArrayList<>();
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .build())
            .build());
    dosageDgMPList.add(
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .when(Timing.EventTiming.MORN)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .build())
            .build());

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(dosageDgMPList)
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, FhirRequirements.FHIR_PROFILES))
            .and(
                operationOutcomeDoesNotContainsInDiagnostics(
                    "expected: montags morgens — je 2 Stück; 08:00 Uhr — je 1 Stück",
                    ErpBfd.B_FD_1676))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "TimingOnlyWhenOrTimeOfDay: Dosages Timings must not state a time of day and"
                        + " period of day across multiple dosage instances",
                    ErpBfd.B_FD_1676))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_08")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert")
  void checkSubmittedPrescriptionsUnusualBigDosageDgMPInformation() {

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(2)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay(List.of("06:00:00", "14:00:00"))
                    .build())
            .build();

    val dosageDGMP2 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP3 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay(List.of("18:00:00"))
                    .build())
            .build();

    val dosageDGMP4 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .timeOfDay(List.of("06:00:00"))
                    .build())
            .build();

    val dosageDGMP5 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP6 =
        DosageDgMPBuilder.dosageBuilder(0.75, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .timeOfDay(List.of("14:00:00"))
                    .build())
            .build();

    val dosageDGMP7 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .timeOfDay(List.of("18:00:00"))
                    .build())
            .build();

    val dosageDGMP8 =
        DosageDgMPBuilder.dosageBuilder(0.75, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP9 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .timeOfDay(List.of("18:00:00"))
                    .build())
            .build();

    val dosageDGMP10 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .timeOfDay(List.of("06:00:00"))
                    .build())
            .build();

    val dosageDGMP11 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .timeOfDay(List.of("14:00:00"))
                    .build())
            .build();

    val dosageDGMP12 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP13 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .timeOfDay(List.of("18:00:00"))
                    .build())
            .build();

    val dosageDGMP14 =
        DosageDgMPBuilder.dosageBuilder(0.75, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .timeOfDay(List.of("06:00:00"))
                    .build())
            .build();

    val dosageDGMP15 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .timeOfDay(List.of("14:00:00"))
                    .build())
            .build();

    val dosageDGMP16 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP17 =
        DosageDgMPBuilder.dosageBuilder(0.75, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .timeOfDay(List.of("18:00:00"))
                    .build())
            .build();

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(
                            List.of(
                                dosageDGMP1,
                                dosageDGMP2,
                                dosageDGMP3,
                                dosageDGMP4,
                                dosageDGMP5,
                                dosageDGMP6,
                                dosageDGMP7,
                                dosageDGMP8,
                                dosageDGMP9,
                                dosageDGMP10,
                                dosageDGMP11,
                                dosageDGMP12,
                                dosageDGMP13,
                                dosageDGMP14,
                                dosageDGMP15,
                                dosageDGMP16,
                                dosageDGMP17))
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpBfd.B_FD_1699))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_09")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und bei Kommaseparierten Werten mit folgenden Nullen, keine '.0' im"
          + " gerenderten Text erwartet")
  void checkSubmittedPrescriptionsWithFloatFourTeenInDosage() {

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageWhenEntry(
            2.0,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NOON),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.MON));

    val dosageDGMP2 =
        DosageDgMPBuilder.dosageWhenEntry(
            1,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.MORN, Timing.EventTiming.EVE),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.WED));

    val dosageDGMP3 =
        DosageDgMPBuilder.dosageWhenEntry(
            2,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NOON),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.FRI));

    val dosageDGMP4 =
        DosageDgMPBuilder.dosageWhenEntry(
            1,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NIGHT),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.SUN));

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(List.of(dosageDGMP1, dosageDGMP2, dosageDGMP3, dosageDGMP4))
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpBfd.B_FD_1699))
            .isCorrect());
  }

  @TestcaseId("ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_10")
  @ParameterizedTest(name = "[{index}]: Manipulated rendered DosageInstruction is: {0} ")
  @ValueSource(
      strings = {
        "für 14,0 Tage: montags 0-2-0-0 Stück; mittwochs 1-0-1-0 Stück; freitags 0-2-0-0 Stück;"
            + " sonntags 0-0-0-1 Stück",
        "für 14.0 Tage: montags 0-2-0-0 Stück; mittwochs 1-0-1-0 Stück; freitags 0-2-0-0 Stück;"
            + " sonntags 0-0-0-1 Stück",
        "für 14 Tage: montags 0-2.0-0-0 Stück; mittwochs 1-0-1-0 Stück; freitags 0-2-0-0 Stück;"
            + " sonntags 0-0-0-1 Stück",
        "für 14 Tage: montags 0-2,0-0-0 Stück; mittwochs 1-0-1-0 Stück; freitags 0-2-0-0 Stück;"
            + " sonntags 0-0-0-1 Stück",
      })
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und bei Kommaseparierten Werten mit folgenden Nullen im gerenderten"
          + " Text einen Fehler wirft")
  void checkSubmittedPrescriptionsWithFloatFourTeenInRenderedDosage(
      String manipulatedRenderedDosageInstruction) {

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageWhenEntry(
            2.0,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NOON),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.MON));

    val dosageDGMP2 =
        DosageDgMPBuilder.dosageWhenEntry(
            1,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.MORN, Timing.EventTiming.EVE),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.WED));

    val dosageDGMP3 =
        DosageDgMPBuilder.dosageWhenEntry(
            2.0,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NOON),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.FRI));

    val dosageDGMP4 =
        DosageDgMPBuilder.dosageWhenEntry(
            1,
            BmpDosiereinheit.STUECK,
            List.of(Timing.EventTiming.NIGHT),
            14.0,
            UnitsOfTimeDE.TAG,
            1,
            Timing.UnitsOfTime.WK,
            List.of(Timing.DayOfWeek.SUN));

    val task =
        doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                .withResourceManipulator(
                    b ->
                        b.getMedicationRequest().getExtension().stream()
                            .filter(DgMPStructDef.MR_RENDERED_DOSAGE_INSTRUCTION::matches)
                            .forEach(
                                ext ->
                                    ext.setValue(
                                        new MarkdownType(manipulatedRenderedDosageInstruction))))
                .withKbvBundleFrom(
                    KbvErpBundleFaker.builder()
                        .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                        .withDosageDgmp(List.of(dosageDGMP1, dosageDGMP2, dosageDGMP3, dosageDGMP4))
                        .toBuilder()));

    doc.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .and(
                operationOutcomeContainsInDiagnostics(
                    "expected: für 14 Tage: montags 0-2-0-0 Stück; mittwochs 1-0-1-0 Stück;"
                        + " freitags 0-2-0-0 Stück; sonntags 0-0-0-1 Stück",
                    ErpBfd.B_FD_1687))
            .hasResponseWith(returnCode(400, ErpBfd.B_FD_1699))
            .isCorrect());
  }
}
