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

package de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.math.BigDecimal;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.TimeType;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class RepeatFuzzImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static RepeatFuzzImpl repeatFuzz;

  private Timing.TimingRepeatComponent repeat;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    repeatFuzz = new RepeatFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    repeat = new Timing.TimingRepeatComponent();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzCount() {
    assertFalse(repeat.hasCount());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasCount());
    val testObject = fuzzerContext.getRandom().nextInt();
    repeat.setCount(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getCount());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzCountMax() {
    assertFalse(repeat.hasCountMax());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasCountMax());
    val testObject = fuzzerContext.getRandom().nextInt();
    repeat.setCountMax(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getCountMax());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDuration() {
    assertFalse(repeat.hasDuration());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasDuration());
    val testObject = BigDecimal.valueOf(fuzzerContext.getRandom().nextLong());
    repeat.setDuration(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getDuration());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDurationMax() {
    assertFalse(repeat.hasDurationMax());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasDurationMax());
    val testObject = BigDecimal.valueOf(fuzzerContext.getRandom().nextLong());
    repeat.setDurationMax(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getDurationMax());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzFrequency() {
    assertFalse(repeat.hasFrequency());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasFrequency());
    val testObject = fuzzerContext.getRandom().nextInt();
    repeat.setFrequency(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getFrequency());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzFrequencyMax() {
    assertFalse(repeat.hasFrequencyMax());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasFrequencyMax());
    val testObject = fuzzerContext.getRandom().nextInt();
    repeat.setFrequencyMax(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getFrequencyMax());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPeriod() {
    assertFalse(repeat.hasPeriod());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasPeriod());
    val testObject = BigDecimal.valueOf(fuzzerContext.getRandom().nextInt());
    repeat.setPeriod(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getPeriod());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPeriodMax() {
    assertFalse(repeat.hasPeriodMax());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasPeriodMax());
    val testObject = BigDecimal.valueOf(fuzzerContext.getRandom().nextLong());
    repeat.setPeriodMax(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getPeriodMax());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPeriodUnit() {
    assertFalse(repeat.hasPeriodUnit());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasPeriodUnit());
    val testObject =
        fuzzerContext.getRandomOneOfClass(Timing.UnitsOfTime.class, Timing.UnitsOfTime.NULL);
    repeat.setPeriodUnit(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getPeriodUnit());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzTimeOfDay() {
    assertFalse(repeat.hasTimeOfDay());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasTimeOfDay());
    val testObject = (new TimeType(fuzzerContext.getStringFuzz().generateRandom(5)));
    repeat.setTimeOfDay(List.of(testObject.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject.getValue(), repeat.getTimeOfDay().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(repeat.hasId());
    repeatFuzz.fuzz(repeat);
    Assertions.assertTrue(repeat.hasId());
    val testObject = fuzzerContext.getIdFuzzer().generateRandom();
    repeat.setId(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    repeatFuzz.fuzz(repeat);
    assertNotEquals(testObject, repeat.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldGenerateRandom() {
    assertNotNull(repeatFuzz.generateRandom());
  }

  @RepeatedTest(REPETITIONS)
  void shouldGetContext() {
    assertNotNull(repeatFuzz.getContext());
  }
}
