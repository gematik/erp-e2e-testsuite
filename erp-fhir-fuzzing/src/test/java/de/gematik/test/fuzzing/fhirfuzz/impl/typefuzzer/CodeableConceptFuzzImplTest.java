/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodingTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class CodeableConceptFuzzImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static CodeableConceptFuzzImpl codeableConceptFuzzImpl;

  private CodeableConcept cc;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    codeableConceptFuzzImpl = new CodeableConceptFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    cc = new CodeableConcept();
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(codeableConceptFuzzImpl.getContext());
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertTrue(codeableConceptFuzzImpl.generateRandom().hasText());
    assertTrue(codeableConceptFuzzImpl.generateRandom().hasCoding());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(cc.hasText());
    codeableConceptFuzzImpl.fuzz(cc);
    assertTrue(cc.hasText());
    codeableConceptFuzzImpl.fuzz(cc);
    val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
    cc.setText(teststring);
    fuzzConfig.setPercentOfAll(0.00f);
    codeableConceptFuzzImpl.fuzz(cc);
    assertNotEquals(teststring, cc.getText());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(cc.hasId());
    codeableConceptFuzzImpl.fuzz(cc);
    assertTrue(cc.hasId());
    codeableConceptFuzzImpl.fuzz(cc);
    val teststring = fuzzerContext.getIdFuzzer().generateRandom();
    cc.setId(teststring);
    fuzzConfig.setPercentOfAll(0.00f);
    codeableConceptFuzzImpl.fuzz(cc);
    assertNotEquals(teststring, cc.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzCoding() {
    assertFalse(cc.hasCoding());
    codeableConceptFuzzImpl.fuzz(cc);
    assertTrue(cc.hasCoding());
    val codings =
        List.of(
            fuzzerContext
                .getTypeFuzzerFor(Coding.class, () -> new CodingTypeFuzzerImpl(fuzzerContext))
                .generateRandom());
    val teststring = codings.get(0).getId();
    cc.setCoding(codings);
    fuzzConfig.setPercentOfAll(0.00f);
    codeableConceptFuzzImpl.fuzz(cc);
    assertNotEquals(teststring, cc.getCoding().get(0).getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzExtension() {
    assertFalse(cc.hasExtension());
    codeableConceptFuzzImpl.fuzz(cc);
    assertTrue(cc.hasExtension());
    codeableConceptFuzzImpl.fuzz(cc);
    assertFalse(cc.hasExtension());
  }
}
