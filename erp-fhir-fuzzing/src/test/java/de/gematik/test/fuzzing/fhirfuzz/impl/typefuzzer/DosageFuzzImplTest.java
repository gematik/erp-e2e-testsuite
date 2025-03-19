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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.DosageFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.RatioTypeFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.SimpleQuantityFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.TimingFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.Quantity;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Timing;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class DosageFuzzImplTest {

  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static DosageFuzzImpl dosageFuzzImpl;

  private Dosage dosage;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    dosageFuzzImpl = new DosageFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    dosage = new Dosage();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzSequence() {
    assertFalse(dosage.hasSequence());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasSequence());
    val testObject = fuzzerContext.getRandom().nextInt();
    dosage.setSequence(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject, dosage.getSequence());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(dosage.hasText());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasText());
    val testObject = fuzzerContext.getStringFuzz().generateRandom(100);
    dosage.setText(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject, dosage.getText());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzTiming() {
    assertFalse(dosage.hasTiming());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasTiming());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(Timing.class, () -> new TimingFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setTiming(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject, dosage.getTiming());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzAdditionalInstr() {
    assertFalse(dosage.hasAdditionalInstruction());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasAdditionalInstruction());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(
                CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setAdditionalInstruction(List.of(testObject.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject, dosage.getAdditionalInstructionFirstRep());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzPatientInstr() {
    assertFalse(dosage.hasPatientInstruction());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasPatientInstruction());
    val testObject = fuzzerContext.getStringFuzz().generateRandom(100);
    dosage.setPatientInstruction(testObject);
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject, dosage.getPatientInstruction());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzSite() {
    assertFalse(dosage.hasSite());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasSite());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(
                CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setSite(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getCodingFirstRep(), dosage.getSite().getCodingFirstRep());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzRoute() {
    assertFalse(dosage.hasRoute());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasRoute());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(
                CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setRoute(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getCodingFirstRep(), dosage.getRoute().getCodingFirstRep());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMethod() {
    assertFalse(dosage.hasMethod());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasMethod());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(
                CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setMethod(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getCodingFirstRep(), dosage.getMethod().getCodingFirstRep());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMaxDosePeriod() {
    assertFalse(dosage.hasMaxDosePerPeriod());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasMaxDosePerPeriod());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(Ratio.class, () -> new RatioTypeFuzzerImpl(fuzzerContext))
            .generateRandom();
    dosage.setMaxDosePerPeriod(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getNumerator(), dosage.getMaxDosePerPeriod().getNumerator());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMaxDosePerAdmin() {
    assertFalse(dosage.hasMaxDosePerAdministration());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasMaxDosePerAdministration());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(Quantity.class, () -> new SimpleQuantityFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setMaxDosePerAdministration(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getSystem(), dosage.getMaxDosePerAdministration().getSystem());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMaxDPerLifetime() {
    assertFalse(dosage.hasMaxDosePerLifetime());
    dosageFuzzImpl.fuzz(dosage);
    Assertions.assertTrue(dosage.hasMaxDosePerLifetime());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(Quantity.class, () -> new SimpleQuantityFuzzImpl(fuzzerContext))
            .generateRandom();
    dosage.setMaxDosePerLifetime(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    dosageFuzzImpl.fuzz(dosage);
    assertNotEquals(testObject.getSystem(), dosage.getMaxDosePerLifetime().getSystem());
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertTrue(dosageFuzzImpl.generateRandom().hasText());
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(dosageFuzzImpl.getContext().getFuzzConfig().getPercentOfEach());
  }
}
