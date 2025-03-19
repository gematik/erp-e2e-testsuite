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

package de.gematik.test.fuzzing.fhirfuzz.impl.resourcefuzzer;

import static de.gematik.test.fuzzing.fhirfuzz.CentralIterationSetupForTests.REPETITIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.AddressFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ContactPointFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.ExtensionFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.HumanNameFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.IdentifierFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.MetaFuzzerImpl;
import de.gematik.test.fuzzing.fhirfuzz.impl.typesfuzzer.NarrativeTypeFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.ContactPoint;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.HumanName;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Practitioner;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;

class PractitionerFuzzImplTest {
  private static FuzzConfig fuzzConfig;
  private static FuzzerContext fuzzerContext;

  private static PractitionerFuzzImpl practitionerFuzz;

  private Practitioner practitioner;

  @BeforeAll
  static void setUpConf() {
    fuzzConfig = new FuzzConfig();
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    fuzzConfig.setUseAllMutators(true);
    fuzzerContext = new FuzzerContext(fuzzConfig);
    practitionerFuzz = new PractitionerFuzzImpl(fuzzerContext);
  }

  @BeforeEach
  void setupComp() {
    fuzzConfig.setPercentOfEach(100.0f);
    fuzzConfig.setPercentOfAll(100.0f);
    practitioner = new Practitioner();
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzId() {
    assertFalse(practitioner.hasId());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasId());
    practitionerFuzz.fuzz(practitioner);
    val teststring = fuzzerContext.getStringFuzz().generateRandom(150);
    practitioner.setId(teststring);
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(teststring, practitioner.getId());
    assertNotNull(practitioner.getId());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzLanguage() {
    assertFalse(practitioner.hasLanguage());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasLanguage());
    practitionerFuzz.fuzz(practitioner);
    val teststring = "123.345.5678";
    practitioner.setLanguage((teststring));
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(teststring, practitioner.getLanguage());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzMeta() {
    assertFalse(practitioner.hasMeta());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasMeta());
    practitionerFuzz.fuzz(practitioner);
    val meta =
        fuzzerContext
            .getTypeFuzzerFor(Meta.class, () -> new MetaFuzzerImpl(fuzzerContext))
            .generateRandom();
    practitioner.setMeta(meta.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(meta.getProfile(), practitioner.getMeta().getProfile());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzText() {
    assertFalse(practitioner.hasText());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasText());
    val testObject =
        fuzzerContext
            .getTypeFuzzerFor(Narrative.class, () -> new NarrativeTypeFuzzImpl(fuzzerContext))
            .generateRandom();
    practitioner.setText(testObject.copy());
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(testObject, practitioner.getText());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzIdentifier() {
    assertFalse(practitioner.hasIdentifier());
    val identiList =
        List.of(
            fuzzerContext
                .getTypeFuzzerFor(Identifier.class, () -> new IdentifierFuzzerImpl(fuzzerContext))
                .generateRandom());
    val input = identiList.get(0).getValue();
    practitioner.setIdentifier(identiList);
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(input, practitioner.getIdentifier().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzActive() {
    assertFalse(practitioner.hasActive());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasActive());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.getActive());
    practitioner.setActive(true);
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.getActive());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzName() {
    HumanName humanName = new HumanName();
    assertFalse(practitioner.hasName());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasName());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.hasName());
    val hName =
        fuzzerContext
            .getTypeFuzzerFor(HumanName.class, () -> new HumanNameFuzzerImpl(fuzzerContext))
            .generateRandom();
    practitioner.setName(List.of(hName.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(hName.getFamily(), practitioner.getNameFirstRep().getFamily());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzAddress() {
    Address address =
        fuzzerContext
            .getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext))
            .generateRandom();
    assertFalse(practitioner.hasAddress());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasAddress());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.hasAddress());
    practitioner.setAddress(List.of(address.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(address.getCity(), practitioner.getAddress().get(0).getCity());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzBithday() {
    assertFalse(practitioner.hasBirthDate());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasBirthDate());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.hasBirthDate());
    val bDay = fuzzerContext.getRandomDate();
    practitioner.setBirthDate(bDay);
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(bDay.getTime(), practitioner.getBirthDate().getTime());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzExtension() {
    assertFalse(practitioner.hasExtension());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasExtension());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.hasExtension());
    val ext =
        fuzzerContext
            .getTypeFuzzerFor(Extension.class, () -> new ExtensionFuzzerImpl(fuzzerContext))
            .generateRandom();
    practitioner.setExtension(List.of(ext.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(ext.getUrl(), practitioner.getExtension().get(0).getUrl());
  }

  @RepeatedTest(REPETITIONS)
  void shouldFuzzTelcom() {
    assertFalse(practitioner.hasTelecom());
    practitionerFuzz.fuzz(practitioner);
    assertTrue(practitioner.hasTelecom());
    practitionerFuzz.fuzz(practitioner);
    assertFalse(practitioner.hasTelecom());
    val ext =
        fuzzerContext
            .getTypeFuzzerFor(ContactPoint.class, () -> new ContactPointFuzzImpl(fuzzerContext))
            .generateRandom();
    practitioner.setTelecom(List.of(ext.copy()));
    fuzzConfig.setPercentOfAll(0.00f);
    practitionerFuzz.fuzz(practitioner);
    assertNotEquals(ext.getValue(), practitioner.getTelecom().get(0).getValue());
  }

  @RepeatedTest(REPETITIONS)
  void shouldRespectDetailSetup() throws JsonProcessingException {
    Map details = new HashMap<>();
    details.put("KBV", "True");
    fuzzConfig.setDetailSetup(details);
    val pract = practitionerFuzz.generateRandom();
    pract.setAddress(
        List.of(
            fuzzerContext
                .getTypeFuzzerFor(Address.class, () -> new AddressFuzzerImpl(fuzzerContext))
                .generateRandom()));
    pract.setActive(true);
    val testAddress = pract.getAddress().get(0).getCity();
    val testIsActive = pract.getActive();
    practitionerFuzz.fuzz(pract);
    assertEquals(testAddress, pract.getAddress().get(0).getCity());
    assertEquals(testIsActive, pract.getActive());

    val om = new ObjectMapper();
    val jsonFile = om.writeValueAsString(fuzzConfig);
    fuzzConfig.setDetailSetup(null);
    practitionerFuzz.fuzz(pract);
    assertTrue(pract.getAddress().isEmpty());
    assertNotEquals(testIsActive, pract.getActive());
  }

  @RepeatedTest(REPETITIONS)
  void generateRandom() {
    assertNotNull(practitionerFuzz.generateRandom().getAddress());
  }

  @RepeatedTest(REPETITIONS)
  void getContext() {
    assertNotNull(practitionerFuzz.getContext());
  }
}
