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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ReferenceFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.HashMap;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Reference;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class ReferenceFuzzerImplTest {
  static ReferenceFuzzerImpl referenceFuzzer;

  static FuzzerContext fuzzerContext;

  Reference reference;

  @BeforeAll
  static void setup() {
    val fuzzConf = new FuzzConfig();
    fuzzConf.setUseAllMutators(true);
    fuzzConf.setPercentOfEach(100f);
    fuzzConf.setDetailSetup(new HashMap<>());
    fuzzerContext = new FuzzerContext(fuzzConf);
    referenceFuzzer = new ReferenceFuzzerImpl(fuzzerContext);
  }

  @BeforeEach
  void setupReference() {
    reference = new Reference();
  }

  @RepeatedTest(REPETITIONS)
  void shouldGenerateRandomRef() {
    val ref = referenceFuzzer.generateRandom();
    assertNotNull(ref.getReference());
    assertNotNull(ref.getType());
    assertNotNull(ref.getIdentifier());
    assertNotNull(ref.getDisplay());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzReference() {
    reference.setReference(null);
    assertNull(reference.getReference());
    referenceFuzzer.fuzz(reference);
    assertNotNull(reference.getReference());
    fuzzerContext.getFuzzConfig().setPercentOfAll(100.00f);
    referenceFuzzer.fuzz(reference);
    fuzzerContext.getFuzzConfig().setPercentOfAll(00.00f);
    val s = fuzzerContext.getStringFuzz().generateRandom(200);
    reference.setReference(s);
    referenceFuzzer.fuzz(reference);
    assertNotEquals(s, reference.getReference());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(reference.hasId());
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.hasId());
    referenceFuzzer.fuzz(reference);
    val teststring = fuzzerContext.getIdFuzzer().generateRandom();
    reference.setId(teststring);
    fuzzerContext.getFuzzConfig().setPercentOfAll(0.00f);
    referenceFuzzer.fuzz(reference);
    assertNotEquals(teststring, reference.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzType() {
    reference.setType(null);
    assertNull(reference.getType());
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.hasType());
    fuzzerContext.getFuzzConfig().setPercentOfAll(100.00f);
    referenceFuzzer.fuzz(reference);
    fuzzerContext.getFuzzConfig().setPercentOfAll(00.00f);
    val s = fuzzerContext.getStringFuzz().generateRandom(100);
    reference.setType(s);
    referenceFuzzer.fuzz(reference);
    assertNotEquals(s, reference.getType());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifier() {
    reference.setIdentifier(null);
    assertFalse(reference.hasIdentifier());
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.hasIdentifier());
    fuzzerContext.getFuzzConfig().setPercentOfAll(100.00f);
    referenceFuzzer.fuzz(reference);
    fuzzerContext.getFuzzConfig().setPercentOfAll(00.00f);
    val ident =
        fuzzerContext
            .getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext))
            .generateRandom();
    reference.setIdentifier(ident.copy());
    referenceFuzzer.fuzz(reference);
    assertNotEquals(ident.getValue(), reference.getIdentifier().getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzDisplay() {
    reference.setDisplay(null);
    assertFalse(reference.hasDisplay());
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.hasDisplay());
    fuzzerContext.getFuzzConfig().setPercentOfAll(100.00f);
    referenceFuzzer.fuzz(reference);
    fuzzerContext.getFuzzConfig().setPercentOfAll(00.00f);
    val s = fuzzerContext.getStringFuzz().generateRandom(150);
    reference.setDisplay(s);
    referenceFuzzer.fuzz(reference);
    assertNotEquals(s, reference.getDisplay());
  }

  @RepeatedTest(REPETITIONS)
  void shouldAcceptDetailSetupAndFuzzesCodeText() {
    assertFalse(reference.hasDisplay());
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.hasDisplay());
    reference.setDisplay("123");
    assertFalse(reference.getDisplay().length() > 50);
    fuzzerContext.getFuzzConfig().getDetailSetup().put("BreakRanges", "TRUE");
    referenceFuzzer.fuzz(reference);
    assertTrue(reference.getDisplay().length() > 50);
    fuzzerContext.getFuzzConfig().getDetailSetup().remove("BreakRanges");
  }
}
