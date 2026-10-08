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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import de.gematik.test.erezept.eml.fhir.r4.dgmp.DosageDgMP;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.ReferenceFeatureToggle;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

class KbvErpBundleFakerTest extends ErpFhirParsingTest {

  @Test
  void buildFakeKbvErpBundleWithPrescriptionId() {
    val bundle = KbvErpBundleFaker.builder().withPrescriptionId(PrescriptionId.random()).fake();
    val bundle2 =
        KbvErpBundleFaker.builder().withPrescriptionId(PrescriptionId.random().getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildFakeKbvErpBundleWithStatusKennzeichen(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val practitioner = KbvPractitionerFaker.builder(forVersion).fake();
    val medication = KbvErpMedicationPZNFaker.builder(erpVersion).fake();
    val patient = KbvPatientFaker.builder(forVersion).fake();
    val coverage = KbvCoverageFaker.builder(forVersion).fake();
    val bundle =
        KbvErpBundleFaker.builder(erpVersion, forVersion)
            .withStatusKennzeichen(StatusKennzeichen.NONE.getCode(), practitioner)
            .withMedication(medication)
            .withPractitioner(practitioner)
            .withPatient(patient)
            .withInsurance(coverage, patient)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvEprBundleWithKvnr() {
    val kvnr = KVNR.random();
    val bundle = KbvErpBundleFaker.builder().withKvnr(kvnr).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(kvnr.getValue(), bundle.getPatient().getKvnr().getValue());
  }

  @Test
  void buildFakeKbvEprBundleWithPatient() {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
            .fake();
    val bundle = KbvErpBundleFaker.builder().withPatient(patient).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertEquals(patient.hashCode(), bundle.getPatient().hashCode());
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithDosageInstruction() {
    val bundle = KbvErpBundleFaker.builder().withDosageInstruction("di").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("di", bundle.getMedicationRequest().getDosageInstructionFirstRep().getText());
  }

  @Test
  void buildFakeKbvErpBundleWithBvg() {
    val bundle = KbvErpBundleFaker.builder().withBvg(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    // BVG is only valid for old profiles
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertTrue(bundle.getMedicationRequest().isBvg());
    } else {
      assertFalse(bundle.getMedicationRequest().isBvg());
    }
  }

  @Test
  void buildFakeKbvErpBundleWithEmergencyServiceFee() {
    val bundle = KbvErpBundleFaker.builder().withEmergencyServiceFee(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMedicationRequest().hasEmergencyServiceFee());
  }

  @Test
  void buildFakeKbvErpBundleWithAccident() {
    val bundle = KbvErpBundleFaker.builder().withAccident(AccidentExtension.accident()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvBundleVersions")
  void buildFakeKbvErpBundleWithMedicationRequestVersion(
      KbvItaForVersion forVersion, KbvItaErpVersion erpVersion) {
    val bundle = KbvErpBundleFaker.builder(erpVersion, forVersion).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithQuantity() {
    val bundle = KbvErpBundleFaker.builder().withDispenseQuantity(1).fake();
    val bundle2 = KbvErpBundleFaker.builder().withDispenseQuantity(fakerAmount()).fake();
    val bundle3 = KbvErpBundleFaker.builder().withDispenseQuantity(fakerAmount()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    val result3 = ValidatorUtil.encodeAndValidate(parser, bundle3);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
    assertTrue(result3.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithCoPaymentStatus() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withCoPaymentStatus(fakerValueSet(StatusCoPayment.class))
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithNote() {
    val bundle = KbvErpBundleFaker.builder().withNote("note").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("note", bundle.getMedicationRequest().getNoteText().orElse(""));
  }

  @Test
  void buildFakeKbvErpBundleWitDosageDgMP() {
    val dosagDGMP =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.MG)
            .timing(
                TimingBuilder.forRepeatComp()
                    .period(3, Timing.UnitsOfTime.D)
                    .frequency(1)
                    .timeOfDay("08:00:00")
                    .build())
            .build();

    val bundle =
        KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withDosageDgmp(dosagDGMP)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(
        "alle 3 Tage: 08:00 Uhr — je 2 mg",
        bundle.getMedicationRequest().getRenderedDosageInstruction().get());
  }

  @Test
  void buildFakeKbvErpBundleWitDosageDgMPList() {
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
    val bundle =
        KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withDosageDgmp(dosageDgMPList)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals(
        "täglich: 22:00 Uhr — je 2 Stück; 18:00 Uhr — je 1 Stück; 01:00 Uhr — je 3 Stück",
        bundle.getMedicationRequest().getRenderedDosageInstruction().get());
  }

  @Test
  void buildFakeKbvErpBundleWithIntent() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withIntent(MedicationRequest.MedicationRequestIntent.ORDER.toCode())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertEquals("order", bundle.getMedicationRequest().getIntent().toCode());
  }

  @Test
  void buildFakeKbvErpBundleWithSubstitution() {
    val bundle = KbvErpBundleFaker.builder().withSubstitution(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertTrue(bundle.getMedicationRequest().allowSubstitution());
  }

  @Test
  void buildFakeKbvErpBundleWithAttester() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withPractitioner(
                KbvPractitionerFaker.builder()
                    .withQualificationType(QualificationType.DOCTOR_IN_TRAINING)
                    .fake())
            .withAttester(KbvPractitionerFaker.builder().fake())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithStatus() {
    val bundle =
        KbvErpBundleFaker.builder()
            .withStatus(MedicationRequest.MedicationRequestStatus.ACTIVE)
            .fake();
    val bundle2 = KbvErpBundleFaker.builder().withStatus("active").fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    val result2 = ValidatorUtil.encodeAndValidate(parser, bundle2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakeKbvErpBundleWithMvo() {
    val bundle = KbvErpBundleFaker.builder().withMvo(mvo()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildForPkvCoverage() {
    val bundle = KbvErpBundleFaker.builder(true).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, bundle);
    assertTrue(result.isSuccessful());
    assertNotNull(bundle.getCoverage());
    assertTrue(
        ((Coding)
                ((java.util.ArrayList<?>)
                        ((KbvCoverage)
                                bundle.getEntry().stream()
                                    .filter(KbvItaForStructDef.COVERAGE::matches)
                                    .toList()
                                    .get(0)
                                    .getResource())
                            .getType()
                            .getCoding())
                    .get(0))
            .getCode()
            .matches("PKV"));
  }

  @ParameterizedTest
  @EnumSource(ReferenceFeatureToggle.RefencingType.class)
  @ClearSystemProperty(key = ReferenceFeatureToggle.TOGGLE_KEY)
  void shouldFakeWithReferencingType(ReferenceFeatureToggle.RefencingType type) {
    System.setProperty(ReferenceFeatureToggle.TOGGLE_KEY, type.getValue());

    val kbvBundle = KbvErpBundleFaker.builder().fake();

    ValidatorUtil.encodeAndValidate(parser, kbvBundle, EncodingType.XML);

    val fullUrlPrefix = type == ReferenceFeatureToggle.RefencingType.UUID ? "urn:uuid:" : "http";
    val entries =
        kbvBundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getFullUrl)
            .allMatch(it -> it.startsWith(fullUrlPrefix));
    assertTrue(entries);

    val composition = kbvBundle.getComposition();
    // not all of them, but easier to check, HAPI should have caught them already
    Predicate<String> p = it -> it.startsWith("urn:uuid");
    p = type == ReferenceFeatureToggle.RefencingType.UUID ? p : p.negate();
    val compositionEntries =
        Stream.of(composition.getSubject(), composition.getCustodian())
            .map(Reference::getReference)
            .allMatch(p);
    assertTrue(compositionEntries);
  }

  /**
   * This UnitTest is similar to the IntegrationTest ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_08 (dosage
   * 1-17) It validates the correct handling of complex dosage instructions with multiple timings
   * and quantities. was found by Mr. Nighthold and communicated in ANFERP-4169 -
   * https://service.gematik.de/browse/ANFERP-4169 and tracked in B-FD_1699, Abweichende Generierung
   * / Validierung der Strukturierten Dosierinformationen
   *
   * <p>um die Blöcke zu umgehen wäre folgende Methode möglich
   *
   * <p>public DosageDgMP timeOfDayAndDayOfWeek(double dosageValue,@NonNull BmpDosiereinheit code,
   * double boundsDuration, @NonNull UnitsOfTimeDE boundDurationUnit, int period, Timing.UnitsOfTime
   * periodUnit, int frequency, List <Timing.DayOfWeek> daysOfWeek, List<String> timesOfDays,
   * List<Timing.EventTiming> when) {val timing =
   * TimingBuilder.forRepeatComp().boundsDuration(boundsDuration, boundDurationUnit);
   * timing.period(period, periodUnit); timing.frequency(frequency);
   * Optional.ofNullable(daysOfWeek).ifPresent(timing::dayOfWeek);
   * Optional.ofNullable(timesOfDays).ifPresent(timing::timeOfDay);
   * Optional.ofNullable(when).ifPresent(timing::when); return
   * DosageDgMPBuilder.dosageBuilder(dosageValue, code).timing(timing.build()).build(); }
   */
  @Ignore("this Unittest takes rd. about 10 seconds to run, so it is disabled for now")
  void shouldValidateExtrasBigDosageCorrect() {

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
    val dosageDGMP18 =
        DosageDgMPBuilder.dosageBuilder(0.25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .timeOfDay(List.of("14:00:00"))
                    .build())
            .build();

    val dosageDGMP19 =
        DosageDgMPBuilder.dosageBuilder(0.55, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .timeOfDay(List.of("10:00:00"))
                    .build())
            .build();

    val dosageDGMP20 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(12, UnitsOfTimeDE.WOCHE)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .timeOfDay(List.of("16:00:00"))
                    .build())
            .build();

    val bundle =
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
                    dosageDGMP17,
                    dosageDGMP18,
                    dosageDGMP19,
                    dosageDGMP20))
            .fake();
    val startTime = System.nanoTime();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, bundle).isSuccessful());

    long end = System.nanoTime();
    long durationMs = (end - startTime) / 1_000_000;

    System.out.println("Dauer: " + durationMs + " ms");
  }
}
