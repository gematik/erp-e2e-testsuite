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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RatioTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class RatioTypeFuzzerImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static RatioTypeFuzzerImpl typeFuzzer;
  private Ratio ratio;

  private static final String TESTSTRING = "Teststring";

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    typeFuzzer = new RatioTypeFuzzerImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    ratio = new Ratio();
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertNotNull(typeFuzzer.generateRandom());
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(typeFuzzer.getContext());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNominator() {
    assertFalse(ratio.hasNumerator());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasNumerator());
    typeFuzzer.fuzz(ratio);
    assertFalse(ratio.hasNumerator());
    val testObject = new Quantity(fuzzerContext.getRandom().nextInt());
    ratio.setNumerator(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertNotEquals(testObject, ratio.getNumerator());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDenom() {
    assertFalse(ratio.hasDenominator());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasDenominator());
    typeFuzzer.fuzz(ratio);
    assertFalse(ratio.hasDenominator());
    val testObject = new Quantity(fuzzerContext.getRandom().nextInt());
    ratio.setDenominator(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertNotEquals(testObject, ratio.getDenominator());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(ratio.hasId());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasId());
    typeFuzzer.fuzz(ratio);
    val testObject = fuzzerContext.getIdFuzzer().generateRandom();
    ratio.setId(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertNotEquals(testObject, ratio.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzExtension() {
    assertFalse(ratio.hasExtension());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasExtension());
    typeFuzzer.fuzz(ratio);
    assertFalse(ratio.hasExtension());
    val ext =
        fuzzerContext
            .getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext))
            .generateRandom();
    ratio.setExtension(List.of(ext.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertNotEquals(ext.getUrl(), ratio.getExtension().get(0).getUrl());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNumeratorCode() {
    ratio.setNumerator(new Quantity(2));
    assertFalse(ratio.getNumerator().hasCode());
    ratio.getNumerator().setCode(TESTSTRING);
    assertTrue(ratio.getNumerator().hasCode());
    typeFuzzer.fuzz(ratio);
    assertFalse(ratio.getNumerator().hasCode());
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.getNumerator().hasCode());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNumeratorSystem() {
    ratio.setNumerator(new Quantity(2));
    assertFalse(ratio.getNumerator().hasSystem());
    ratio.getNumerator().setSystem(TESTSTRING);
    assertTrue(ratio.getNumerator().hasSystem());
    ratio.getNumerator().setSystem(TESTSTRING);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.getNumerator().hasSystem());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzNumeratorUnit() {
    ratio.setNumerator(new Quantity(2));
    assertFalse(ratio.getNumerator().hasUnit());
    ratio.getNumerator().setUnit(TESTSTRING);
    assertTrue(ratio.getNumerator().hasUnit());
    ratio.getNumerator().setUnit(TESTSTRING);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.getNumerator().hasUnit());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDenomCode() {
    ratio.setDenominator(new Quantity(5));
    assertFalse(ratio.getDenominator().hasCode());
    ratio.getDenominator().setCode(TESTSTRING);
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasExtension());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDenomSystem() {
    ratio.setDenominator(new Quantity(5));
    assertFalse(ratio.hasExtension());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasExtension());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDenomUnit() {
    ratio.setDenominator(new Quantity(5));
    assertFalse(ratio.hasExtension());
    typeFuzzer.fuzz(ratio);
    assertTrue(ratio.hasExtension());
  }
}
