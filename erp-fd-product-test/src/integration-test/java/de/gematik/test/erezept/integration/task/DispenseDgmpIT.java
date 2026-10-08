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

import static de.gematik.test.core.expectations.requirements.FhirRequirements.FHIR_PROFILES;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;
import static de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE.MONAT;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.ErpBfd;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.DispensePrescriptionNew;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
import java.util.Random;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept als Apotheke mit DosageDgMP dispensieren")
@Tag("UseCase:Dispense")
class DispenseDgmpIT extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor apoAmFlughafen;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doc;

  @TestcaseId("DISPENSE_DOSAGE_DGMP_1")
  @Test
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und die nun verbotene Kombination von TimeOfDay und DayOfWeek"
          + " innerhalb einer Ressource ablehnt")
  void checkSubmittedPrescriptionsWithNewCombinationInDosageLikeTimeOfDayAndDayOfWeek() {

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .when(Timing.EventTiming.MORN)
                    .build())
            .build();

    val dosageDGMP2 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .timeOfDay("22:00:00")
                    .build())
            .build();

    val task =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                    .withKbvBundleFrom(
                        KbvErpBundleFaker.builder()
                            .withMedication(KbvErpMedicationPZNFaker.builder().fake())
                            .withDosageDgmp(List.of(dosageDGMP1))
                            .toBuilder()))
            .getExpectedResponse();

    val acceptation = apoAmFlughafen.performs(AcceptPrescription.forTheTask(task));

    apoAmFlughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType()
            .hasResponseWith(returnCode(200, FHIR_PROFILES))
            .isCorrect());

    val medication2 =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(666)
            .withPzn(PZN.from("17377602"), "Spikevax von Moderna")
            .fake();

    val medDisp1 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication2)
            .withPerformer(apoAmFlughafen.getTelematikId().getValue())
            .withDgmp(List.of(dosageDGMP1, dosageDGMP2))
            .fake();

    val expectedMedicationDispenses = List.of(Pair.of(medDisp1, medication2));
    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();
    expectedMedicationDispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));
    val params = paramsBuilder.build();

    val dispensation =
        apoAmFlughafen.performs(
            DispensePrescriptionNew.withCredentials(
                    acceptation.getExpectedResponse().getTaskId(),
                    acceptation.getExpectedResponse().getSecret())
                .withParameters(params));

    apoAmFlughafen.attemptsTo(
        Verify.that(dispensation)
            .withOperationOutcome(ErpAfos.A_19297)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "TimingOnlyWhenOrTimeOfDay: Dosages Timings must not state a time of day and"
                        + " period of day across multiple dosage instances",
                    FHIR_PROFILES))
            .isCorrect());
  }

  @TestcaseId("DISPENSE_DOSAGE_DGMP_2")
  @ParameterizedTest(name = "[{index}]: many DosageDgMPs were integrated in: {0} ")
  @ValueSource(strings = {"Prescription", "CloseInputResource"})
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die übergebenen, gerenderten Dosierinformationen"
          + " korrekt validiert und die riesige Kombination von 20 DosageDgMP Objekten korrekt"
          + " verarbeitet und validiert")
  void checkSubmittedPrescriptionsWithVeryBigDosageDgMPObject(String focus) {

    val bundleBuilder =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.builder().fake());
    if (focus.equals("Prescription")) {
      bundleBuilder.withDosageDgmp(getSpecificAnfErpDosages());
    }

    val task =
        doc.performs(
                IssuePrescription.forPatient(patient)
                    .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                    .withKbvBundleFrom(bundleBuilder.toBuilder()))
            .getExpectedResponse();

    val acceptation = apoAmFlughafen.performs(AcceptPrescription.forTheTask(task));

    apoAmFlughafen.attemptsTo(
        Verify.that(acceptation)
            .withExpectedType(ErpBfd.B_FD_1729)
            .hasResponseWith(returnCode(200, FHIR_PROFILES))
            .isCorrect());

    val medication2 =
        GemErpMedicationFaker.forPznMedication()
            .withAmount(new Random().nextInt(1000))
            .withPzn(PZN.from("17377602"), "Spikevax von Moderna")
            .fake();

    val medDisp1 =
        ErxMedicationDispenseFaker.builder()
            .withKvnr(patient.getKvnr())
            .withPrescriptionId(task.getPrescriptionId())
            .withMedication(medication2)
            .withPerformer(apoAmFlughafen.getTelematikId().getValue());
    if (focus.equals("CloseInputResource")) {
      medDisp1.withDgmp(getSpecificAnfErpDosages());
    }

    val expectedMedicationDispenses = List.of(Pair.of(medDisp1.fake(), medication2));
    val paramsBuilder = GemOperationInputParameterBuilder.forDispensingPharmaceuticals();
    expectedMedicationDispenses.forEach(p -> paramsBuilder.with(p.getLeft(), p.getRight()));
    val params = paramsBuilder.build();

    val dispensation =
        apoAmFlughafen.performs(
            DispensePrescriptionNew.withCredentials(
                    acceptation.getExpectedResponse().getTaskId(),
                    acceptation.getExpectedResponse().getSecret())
                .withParameters(params));

    apoAmFlughafen.attemptsTo(
        Verify.that(dispensation)
            .withExpectedType(ErpBfd.B_FD_1729)
            .hasResponseWith(returnCode(200))
            .isCorrect());
  }

  private List<DosageDgMP> getSpecificAnfErpDosages() {
    double boundDurationFromAnfErpTicket = 300;
    BmpDosiereinheit dosiereinheitStueck = BmpDosiereinheit.STUECK;
    Timing.UnitsOfTime periodUnitWeek = Timing.UnitsOfTime.WK;

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageBuilder(1, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(4)
                    .dayOfWeek(Timing.DayOfWeek.MON)
                    .when(
                        List.of(
                            Timing.EventTiming.MORN,
                            Timing.EventTiming.NOON,
                            Timing.EventTiming.EVE,
                            Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP2 =
        DosageDgMPBuilder.dosageBuilder(100, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP3 =
        DosageDgMPBuilder.dosageBuilder(0.5, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP4 =
        DosageDgMPBuilder.dosageBuilder(1, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(2)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.EVE, Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP5 =
        DosageDgMPBuilder.dosageBuilder(10, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(List.of(Timing.DayOfWeek.WED))
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP6 =
        DosageDgMPBuilder.dosageBuilder(3, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(3)
                    .dayOfWeek(
                        List.of(Timing.DayOfWeek.WED, Timing.DayOfWeek.THU, Timing.DayOfWeek.SUN))
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP7 =
        DosageDgMPBuilder.dosageBuilder(2, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(List.of(Timing.DayOfWeek.WED))
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP8 =
        DosageDgMPBuilder.dosageBuilder(0.5, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP9 =
        DosageDgMPBuilder.dosageBuilder(2, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP10 =
        DosageDgMPBuilder.dosageBuilder(15, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP11 =
        DosageDgMPBuilder.dosageBuilder(30, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP12 =
        DosageDgMPBuilder.dosageBuilder(25, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP13 =
        DosageDgMPBuilder.dosageBuilder(1.5, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP14 =
        DosageDgMPBuilder.dosageBuilder(30, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP15 =
        DosageDgMPBuilder.dosageBuilder(16, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP16 =
        DosageDgMPBuilder.dosageBuilder(1, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(3)
                    .dayOfWeek(List.of(Timing.DayOfWeek.SAT))
                    .when(
                        List.of(
                            Timing.EventTiming.MORN,
                            Timing.EventTiming.NOON,
                            Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP17 =
        DosageDgMPBuilder.dosageBuilder(10.5, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP18 =
        DosageDgMPBuilder.dosageBuilder(30.5, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP19 =
        DosageDgMPBuilder.dosageBuilder(1, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP20 =
        DosageDgMPBuilder.dosageBuilder(12, dosiereinheitStueck)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(boundDurationFromAnfErpTicket, MONAT)
                    .period(1, periodUnitWeek)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    return List.of(
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
        dosageDGMP17,
        dosageDGMP18,
        dosageDGMP19,
        dosageDGMP20);
  }
}
