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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Period;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class IdentifierFuzzerImplTest {

  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;
  private static final String TESTSTRING = "TestSTRING";
  private static IdentifierFuzzerImpl fhirIdentifierFuzzer;
  private Identifier identifier;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    fhirIdentifierFuzzer = new IdentifierFuzzerImpl(fuzzerContext);
  }

  @BeforeEach
  void setupIdentifier() {
    identifier = new Identifier();
  }

  @RepeatedTest(REPETITIONS)
  void shouldGenerateRandom() {
    val result = fhirIdentifierFuzzer.generateRandom();
    assertNotNull(fhirIdentifierFuzzer.generateRandom());
    assertNotNull(result.getSystem());
    assertNotNull(result.getUse());
    assertNotNull(result.getValue());
    assertNotNull(result.getType());
    assertNotNull(result.getPeriod());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzValue() {
    identifier = fhirIdentifierFuzzer.generateRandom();
    val testObject = identifier.getValue();
    fhirIdentifierFuzzer.fuzz(identifier);
    assertNotEquals(testObject, identifier.getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzAndValidate() {
    val testobject = "123123123123345654676798890ßü+ß´0";
    identifier.setSystem(testobject);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertNotEquals(testobject, identifier.getSystem());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifierType() {
    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfAll(00.0f);
    var codingTypeFuzzer =
        fuzzerContext.getTypeFuzzerFor(
            CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext));
    assertFalse(identifier.hasType());
    val input = codingTypeFuzzer.generateRandom();
    identifier.setType(input);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasType());
    fuzzConfig.setPercentOfAll(100.0f);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertNotEquals(input, identifier.getType());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifierUse() {

    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfAll(00.0f);
    val use =
        fuzzerContext.getRandomOneOfClass(
            Identifier.IdentifierUse.class, Identifier.IdentifierUse.NULL);
    assertFalse(identifier.hasUse());
    identifier.setUse(use);
    assertTrue(identifier.hasUse());
    fuzzConfig.setPercentOfAll(100.0f);
    fhirIdentifierFuzzer.fuzz(identifier);

    fuzzConfig.setPercentOfAll(00.0f);
    identifier.setUse(use);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasUse());
    assertNotEquals(use, identifier.getUse());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifierSystem() {
    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfAll(00.0f);
    val system = TESTSTRING;
    assertFalse(identifier.hasSystem());
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasSystem());
    fuzzConfig.setPercentOfAll(100.0f);
    fhirIdentifierFuzzer.fuzz(identifier);
    fuzzConfig.setPercentOfAll(00.0f);
    identifier.setSystem(system);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasUse());
    assertNotEquals(system, identifier.getSystem());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifierValue() {
    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfAll(00.0f);
    val system = TESTSTRING;
    assertFalse(identifier.hasSystem());
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasSystem());
    fuzzConfig.setPercentOfAll(100.0f);
    fhirIdentifierFuzzer.fuzz(identifier);
    identifier.setSystem(system);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasUse());
    assertNotEquals(system, identifier.getSystem());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifierPeriod() {
    fuzzConfig.setUseAllMutators(true);
    fuzzConfig.setPercentOfAll(00.0f);
    val period = new Period();
    assertFalse(identifier.hasPeriod());
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasPeriod());
    fuzzConfig.setPercentOfAll(100.0f);
    fhirIdentifierFuzzer.fuzz(identifier);
    identifier.setPeriod(period);
    fhirIdentifierFuzzer.fuzz(identifier);
    assertTrue(identifier.hasPeriod());
    assertNotEquals(period, identifier.getPeriod());
  }
}
