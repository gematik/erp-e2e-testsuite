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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CanonicalTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import org.hl7.fhir.r4.model.CanonicalType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class CanonicalTypeFuzzerImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  CanonicalTypeFuzzerImpl typeFuzzer;
  CanonicalType canonicalType;
  final String TESTSTRING = fuzzerContext.getStringFuzz().generateRandom(300);

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    typeFuzzer = new CanonicalTypeFuzzerImpl(fuzzerContext);
    canonicalType = new CanonicalType();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(canonicalType.hasId());
    typeFuzzer.fuzz(canonicalType);
    assertTrue(canonicalType.hasId());
    typeFuzzer.fuzz(canonicalType);

    canonicalType.setId(TESTSTRING);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(canonicalType);
    assertNotEquals(TESTSTRING, canonicalType.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldValue() {
    assertFalse(canonicalType.hasValue());
    typeFuzzer.fuzz(canonicalType);
    assertTrue(canonicalType.hasValue());
    typeFuzzer.fuzz(canonicalType);
    canonicalType.setValue(TESTSTRING);
    fuzzConfig.setPercentOfAll(0.00f);
    typeFuzzer.fuzz(canonicalType);
    assertNotEquals(TESTSTRING, canonicalType.getValue());
  }
}
