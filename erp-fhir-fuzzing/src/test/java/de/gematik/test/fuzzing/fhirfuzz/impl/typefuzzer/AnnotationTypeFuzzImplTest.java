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

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AnnotationTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import lombok.val;
import org.hl7.fhir.r4.model.Annotation;
import org.hl7.fhir.r4.model.StringType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class AnnotationTypeFuzzImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static AnnotationTypeFuzzImpl annotationTypeFuzz;
  private Annotation annotation;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    annotationTypeFuzz = new AnnotationTypeFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    annotation = new Annotation();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(annotation.hasText());
    annotationTypeFuzz.fuzz(annotation);
    assertTrue(annotation.hasText());
    annotationTypeFuzz.fuzz(annotation);
    val testObject = fuzzerContext.getStringFuzz().generateRandom(15);
    annotation.setText(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    annotationTypeFuzz.fuzz(annotation);
    assertNotEquals(testObject, annotation.getText());
  }

  @RepeatedTest(REPETITIONS)
  void fuzzTime() {
    assertFalse(annotation.hasTime());
    annotationTypeFuzz.fuzz(annotation);
    assertTrue(annotation.hasTime());
    annotationTypeFuzz.fuzz(annotation);
    val testObject = fuzzerContext.getRandomDate();
    annotation.setTime(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    annotationTypeFuzz.fuzz(annotation);
    assertNotEquals(testObject, annotation.getTime());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzAuth() {
    assertFalse(annotation.hasAuthor());
    annotationTypeFuzz.fuzz(annotation);
    assertTrue(annotation.hasAuthor());
    annotationTypeFuzz.fuzz(annotation);
    val testObject = fuzzerContext.getStringFuzz().generateRandom(15);
    annotation.setAuthor(new StringType(testObject));
    fuzzConfig.setPercentOfAll(0.00f);
    annotationTypeFuzz.fuzz(annotation);
    assertNotEquals(testObject, annotation.getAuthor());
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertNotNull(annotationTypeFuzz.generateRandom());
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(annotationTypeFuzz.getContext());
  }
}
