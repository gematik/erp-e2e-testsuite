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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.bbriccs.utils.StopwatchUtil;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.DosageDgMPBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.TimingBuilder;
import de.gematik.test.erezept.eml.fhir.builder.componentbuilder.dgmp.UnitsOfTimeDE;
import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Timing;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class GemOperationInputParameterBuilderTest extends ErpFhirParsingTest {

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(
        PrivateConstructorsUtil.isUtilityConstructor(GemOperationInputParameterBuilder.class));
  }

  @Test
  void shouldBuildCloseOperationForDiGAMedicationDispense() {
    val medDispense =
        ErxMedicationDispenseDiGAFaker.builder(ErpWorkflowVersion.getDefaultVersion()).fake();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingDiGA()
            .version(ErpWorkflowVersion.getDefaultVersion())
            .with(medDispense)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildCloseOperationForMultipleDiGAMedicationDispense() {
    val version = ErpWorkflowVersion.V1_4;

    val medDispense = ErxMedicationDispenseDiGAFaker.builder(version).fake();
    val medDispense2 = ErxMedicationDispenseDiGAFaker.builder(version).fake();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingDiGA()
            .with(medDispense)
            .with(medDispense2)
            .version(version)
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource("pharmaceuticalsParameterBuilderProvider")
  void shouldBuildInputOperationParametersForPharmaceuticalMedicationDispense(
      Supplier<GemDispenseCloseOperationPharmaceuticalsBuilder<?>> builderSupplier) {
    val version = ErpWorkflowVersion.V1_4;

    val medication = GemErpMedicationFaker.forPznMedication(version).fake();
    val medDispense = ErxMedicationDispenseFaker.builder(version).withMedication(medication).fake();

    val closeOperation =
        builderSupplier.get().version(version).with(medDispense, medication).build();

    val vr = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(vr.isSuccessful());
  }

  @ParameterizedTest
  @MethodSource("pharmaceuticalsParameterBuilderProvider")
  void shouldBuildInputOperationParametersForSplittedPharmaceuticalMedicationDispense(
      Supplier<GemDispenseCloseOperationPharmaceuticalsBuilder<?>> builderSupplier) {
    val version = ErpWorkflowVersion.V1_4;

    val builder = builderSupplier.get();

    IntStream.range(0, 4)
        .forEach(
            i -> {
              val medication = GemErpMedicationFaker.forPznMedication(version).fake();
              val medDispense =
                  ErxMedicationDispenseFaker.builder(version).withMedication(medication).fake();

              builder.with(medDispense, medication);
            });

    val closeOperation = builder.version(version).build();
    val vr = ValidatorUtil.encodeAndValidate(parser, closeOperation);
    assertTrue(vr.isSuccessful());
  }

  @Test
  void shouldBuildEmptyCloseOperationForPharmaceuticals() {
    val version = ErpWorkflowVersion.getDefaultVersion();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingPharmaceuticals().version(version).build();

    assertTrue(parser.isValid(closeOperation));
  }

  @ParameterizedTest
  @MethodSource("dispenseParameterBuilderProvider")
  void shouldNotAllowEmptyDispenseOperation(Supplier<ResourceBuilder<?, ?>> builderSupplier) {
    val builder = builderSupplier.get();
    assertThrows(BuilderException.class, builder::build);
  }

  @ParameterizedTest
  @MethodSource("closeParameterBuilderProvider")
  void shouldAllowEmptyParametersForCloseOperation(
      Supplier<ResourceBuilder<?, ?>> builderSupplier) {
    val builder = builderSupplier.get();
    assertDoesNotThrow(builder::build);
  }

  static Stream<Arguments> closeParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forClosingPharmaceuticals,
            GemOperationInputParameterBuilder::forClosingDiGA)
        .map(Arguments::of);
  }

  static Stream<Arguments> dispenseParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forDispensingPharmaceuticals)
        .map(Arguments::of);
  }

  static Stream<Arguments> pharmaceuticalsParameterBuilderProvider() {
    return Stream.of(
            (Supplier<ResourceBuilder<?, ?>>)
                GemOperationInputParameterBuilder::forClosingPharmaceuticals,
            GemOperationInputParameterBuilder::forClosingPharmaceuticals)
        .map(Arguments::of);
  }

  /**
   * This UnitTest is similar to the IntegrationTest ACTIVATE_DOSAGE_DGMP_PRESCRIPTIONS_08 (dosage
   * 1-17) It validates the correct handling of complex dosage instructions with multiple timings
   * and quantities. was found by Mr. Nighthold and communicated in ANFERP-4169 -
   * https://service.gematik.de/browse/ANFERP-4169 and tracked in B-FD_1699, Abweichende Generierung
   * / Validierung der Strukturierten Dosierinformationen
   */
  @Ignore("this Unittest takes rd. about 10 seconds to run, so it is disabled for now")
  void shouldValidateExtrasBigDosageCorrect() {

    val dosageDGMP1 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
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
        DosageDgMPBuilder.dosageBuilder(100, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP3 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP4 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(2)
                    .dayOfWeek(Timing.DayOfWeek.TUE)
                    .when(List.of(Timing.EventTiming.EVE, Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP5 =
        DosageDgMPBuilder.dosageBuilder(10, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(List.of(Timing.DayOfWeek.WED))
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP6 =
        DosageDgMPBuilder.dosageBuilder(3, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(3)
                    .dayOfWeek(
                        List.of(Timing.DayOfWeek.WED, Timing.DayOfWeek.THU, Timing.DayOfWeek.SUN))
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP7 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(List.of(Timing.DayOfWeek.WED))
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP8 =
        DosageDgMPBuilder.dosageBuilder(0.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.WED)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP9 =
        DosageDgMPBuilder.dosageBuilder(2, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP10 =
        DosageDgMPBuilder.dosageBuilder(15, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP11 =
        DosageDgMPBuilder.dosageBuilder(30, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.THU)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP12 =
        DosageDgMPBuilder.dosageBuilder(25, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP13 =
        DosageDgMPBuilder.dosageBuilder(1.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.NOON))
                    .build())
            .build();

    val dosageDGMP14 =
        DosageDgMPBuilder.dosageBuilder(30, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP15 =
        DosageDgMPBuilder.dosageBuilder(16, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.FRI)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP16 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
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
        DosageDgMPBuilder.dosageBuilder(10.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SAT)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val dosageDGMP18 =
        DosageDgMPBuilder.dosageBuilder(30.5, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.MORN))
                    .build())
            .build();

    val dosageDGMP19 =
        DosageDgMPBuilder.dosageBuilder(1, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.EVE))
                    .build())
            .build();

    val dosageDGMP20 =
        DosageDgMPBuilder.dosageBuilder(12, BmpDosiereinheit.STUECK)
            .timing(
                TimingBuilder.forRepeatComp()
                    .boundsDuration(300, UnitsOfTimeDE.MONAT)
                    .period(1, Timing.UnitsOfTime.WK)
                    .frequency(1)
                    .dayOfWeek(Timing.DayOfWeek.SUN)
                    .when(List.of(Timing.EventTiming.NIGHT))
                    .build())
            .build();

    val medication = GemErpMedicationFaker.forPznMedication().fake();
    val medDispense =
        ErxMedicationDispenseFaker.builder()
            .withDgmp(
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
            .withMedication(medication)
            .fake();

    val closeOperation =
        GemOperationInputParameterBuilder.forClosingPharmaceuticals()
            .with(medDispense, medication)
            .build();

    assertTrue(ValidatorUtil.encodeAndValidate(parser, closeOperation).isSuccessful());

    val bundle =
        KbvErpBundleFaker.builder()
            .withMedication(KbvErpMedicationPZNFaker.builder(KbvItaErpVersion.V1_4_0).fake())
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

    val measurement = StopwatchUtil.measure(() -> ValidatorUtil.encodeAndValidate(parser, bundle));
    val vr = measurement.response();
    val duration = measurement.duration();

    assertTrue(vr.isSuccessful());
    System.out.println("Dauer: " + duration + " ms");
  }
}
