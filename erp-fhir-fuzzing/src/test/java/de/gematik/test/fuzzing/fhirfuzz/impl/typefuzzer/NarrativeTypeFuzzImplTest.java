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

package de.gematik.test.fuzzing.fhirfuzz.impl.typefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.NarrativeTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Narrative;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;

class NarrativeTypeFuzzImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  private static NarrativeTypeFuzzImpl typeFuzzer;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    typeFuzzer = new NarrativeTypeFuzzImpl(fuzzerContext);
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzStatus() {

    fuzzConfig.setPercentOfAll(00.0f);
    val nType = new Narrative();
    nType.setDiv(null);
    assertFalse(nType.hasStatus());
    typeFuzzer.fuzz(nType);
    assertTrue(nType.hasStatus());
    fuzzConfig.setPercentOfAll(100.0f);
    nType.setStatus(null);
    assertFalse(nType.hasStatus());
    fuzzConfig.setPercentOfAll(00.0f);
    val c = Narrative.NarrativeStatus.ADDITIONAL;
    nType.setStatus(c);
    typeFuzzer.fuzz(nType);
    assertTrue(nType.hasStatus());
    assertNotEquals(c, nType.getStatus());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {

    fuzzConfig.setPercentOfAll(00.0f);
    val nType = new Narrative();
    nType.setId(null);
    assertFalse(nType.hasId());
    typeFuzzer.fuzz(nType);
    assertTrue(nType.hasId());
    fuzzConfig.setPercentOfAll(100.0f);
    nType.setId(null);
    assertFalse(nType.hasId());
    fuzzConfig.setPercentOfAll(00.0f);
    val c = fuzzerContext.getIdFuzzer().generateRandom();
    nType.setId(c);
    typeFuzzer.fuzz(nType);
    assertTrue(nType.hasId());
    assertNotEquals(c, nType.getId());
  }

  @RepeatedTest(REPETITIONS)
  void ShouldGenerateRandom() {
    assertNotNull(typeFuzzer.generateRandom());
  }

  @RepeatedTest(REPETITIONS)
  void shouldGetContext() {
    assertNotNull(typeFuzzer.getContext());
  }
}
