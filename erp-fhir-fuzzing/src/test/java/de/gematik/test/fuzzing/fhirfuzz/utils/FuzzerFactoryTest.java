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

package de.gematik.test.fuzzing.fhirfuzz.utils;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer.MedicationFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DosageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RepeatFuzzImpl;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class FuzzerFactoryTest {

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
  void shouldFuzzNoRessource() {
    Medication medication = new MedicationFuzzImpl(fuzzerContext).generateRandom();
    val copy = medication.copy();
    fuzzerContext.clearFuzzer();
    fuzzerContext.getFuzzerFor(Medication.class).ifPresent(rf -> rf.fuzz(medication));
    assertEquals(medication.getCode().getId(), copy.getCode().getId());
    fuzzerContext.setDefaultFuzzer();
    fuzzerContext.getFuzzerFor(Medication.class).ifPresent(rf -> rf.fuzz(medication));
    assertNotEquals(medication.getCode().getId(), copy.getCode().getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNoType() {
    val dosage = new DosageFuzzImpl(fuzzerContext).generateRandom();
    val copy = dosage.copy();
    fuzzerContext.clearFuzzer();
    fuzzerContext.getTypeFuzzerFor(Dosage.class).ifPresent(tf -> tf.fuzz(dosage));
    assertEquals(dosage.getText(), copy.getText());
    fuzzerContext.setDefaultFuzzer();
    fuzzerContext.getTypeFuzzerFor(Dosage.class).ifPresent(tf -> tf.fuzz(dosage));
    assertNotEquals(dosage.getText(), copy.getText());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNoBase() {
    val repeatComponent = new RepeatFuzzImpl(fuzzerContext).generateRandom();
    val copy = repeatComponent.copy();
    fuzzerContext.clearFuzzer();
    fuzzerContext
        .getBaseFuzzerFor(Timing.TimingRepeatComponent.class)
        .ifPresent(tf -> tf.fuzz(repeatComponent));
    assertEquals(repeatComponent.getId(), copy.getId());
    fuzzerContext.setDefaultFuzzer();
    fuzzerContext
        .getBaseFuzzerFor(Timing.TimingRepeatComponent.class)
        .ifPresent(tf -> tf.fuzz(repeatComponent));
    assertNotEquals(repeatComponent.getId(), copy.getId());
  }
}
