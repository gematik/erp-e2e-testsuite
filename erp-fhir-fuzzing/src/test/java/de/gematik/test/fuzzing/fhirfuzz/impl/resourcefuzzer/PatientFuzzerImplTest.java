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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.CodeableConceptFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.HumanNameFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.NarrativeTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class PatientFuzzerImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static PatientFuzzerImpl patientFuzzer;
  private Patient patient;

  public static final int test = 1;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    patientFuzzer = new PatientFuzzerImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    patient = new Patient();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(patient.hasId());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasId());
    patientFuzzer.fuzz(patient);
    val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
    patient.setId(teststring);
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(teststring, patient.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMeta() {
    assertFalse(patient.hasMeta());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasMeta());
    patientFuzzer.fuzz(patient);
    val meta =
        fuzzerContext
            .getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext))
            .generateRandom();
    patient.setMeta(meta.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(meta.getProfile(), patient.getMeta().getProfile());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifier() {
    assertFalse(patient.hasIdentifier());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasIdentifier());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.hasIdentifier());
    val identiList =
        List.of(
            fuzzerContext
                .getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext))
                .generateRandom());
    val input = identiList.get(0).getValue();
    patient.setIdentifier(identiList);
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(input, patient.getIdentifier().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzLang() {
    assertFalse(patient.hasLanguage());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasLanguage());
    fuzzConfig.setPercentOfAll(100.0f);
    patientFuzzer.fuzz(patient);
    val lang = fuzzerContext.getStringFuzz().generateRandom(150);
    patient.setLanguage(lang);
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(lang, patient.getLanguage());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzActive() {
    assertFalse(patient.hasActive());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasActive());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.getActive());
    patient.setActive(true);
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertFalse(patient.getActive());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(patient.hasText());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasText());
    patientFuzzer.fuzz(patient);
    val text =
        fuzzerContext
            .getTypeFuzzerFor(Narrative.class, () -> new NarrativeTypeFuzzImpl(fuzzerContext))
            .generateRandom();
    patient.setText(text.copy());
    assertTrue(patient.hasText());
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(text.getId(), patient.getText().getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzName() {
    HumanName humanName = new HumanName();
    assertFalse(patient.hasName());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasName());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.hasName());
    val hName =
        fuzzerContext
            .getTypeFuzzerFor(HumanName.class, () -> new HumanNameFuzzerImpl(fuzzerContext))
            .generateRandom();
    patient.setName(List.of(hName.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(hName.getFamily(), patient.getNameFirstRep().getFamily());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzAddress() {
    Address address =
        fuzzerContext
            .getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext))
            .generateRandom();
    assertFalse(patient.hasAddress());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasAddress());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.hasAddress());
    patient.setAddress(List.of(address.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(address.getCity(), patient.getAddress().get(0).getCity());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzExtension() {
    assertFalse(patient.hasExtension());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasExtension());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.hasExtension());
    val ext =
        fuzzerContext
            .getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext))
            .generateRandom();
    patient.setExtension(List.of(ext.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(ext.getUrl(), patient.getExtension().get(0).getUrl());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMartialStatus() {
    assertFalse(patient.hasMaritalStatus());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasMaritalStatus());
    patientFuzzer.fuzz(patient);
    val m =
        fuzzerContext
            .getTypeFuzzerFor(
                CodeableConcept.class, () -> new CodeableConceptFuzzImpl(fuzzerContext))
            .generateRandom();
    patient.setMaritalStatus(m.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(m, patient.getMaritalStatus());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzBithday() {
    assertFalse(patient.hasBirthDate());
    patientFuzzer.fuzz(patient);
    assertTrue(patient.hasBirthDate());
    patientFuzzer.fuzz(patient);
    assertFalse(patient.hasBirthDate());
    val bDay = fuzzerContext.getRandomDate();
    patient.setBirthDate(bDay);
    fuzzConfig.setPercentOfAll(0.00f);
    patientFuzzer.fuzz(patient);
    assertNotEquals(bDay.getTime(), patient.getBirthDate().getTime());
  }
}
