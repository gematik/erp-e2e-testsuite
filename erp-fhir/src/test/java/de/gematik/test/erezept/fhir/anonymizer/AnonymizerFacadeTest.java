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

package de.gematik.test.erezept.fhir.anonymizer;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.LinkedList;
import java.util.stream.Stream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

class AnonymizerFacadeTest extends ParsingTest {

  @ParameterizedTest
  @MethodSource
  void shouldAnonymizeCompleteBundle(String path, AnonymizationType type, MaskingStrategy blacker) {
    val content = ResourceUtils.readFileFromResource(path);
    val bundle = parser.decode(KbvErpBundle.class, content);

    val anonymizer = new AnonymizerFacade(type, blacker);

    val anonymization = assertDoesNotThrow(() -> anonymizer.anonymize(bundle));
    assertTrue(anonymization);
  }

  static Stream<Arguments> shouldAnonymizeCompleteBundle() {
    return Stream.of(
        Arguments.of(
            "fhir/valid/kbv/1.0.2/bundle/sdf6s75f-d959-43f0-8ac4-sd6f7sd6.xml",
            AnonymizationType.REPLACING,
            new CharReplacementStrategy()),
        Arguments.of(
            "fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml",
            AnonymizationType.REPLACING,
            new CharReplacementStrategy()),
        Arguments.of(
            "fhir/valid/kbv/1.0.2/bundle/sdf6s75f-d959-43f0-8ac4-sd6f7sd6.xml",
            AnonymizationType.BLACKING,
            new CharReplacementStrategy()),
        Arguments.of(
            "fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml",
            AnonymizationType.BLACKING,
            new CharReplacementStrategy()),
        Arguments.of(
            "fhir/valid/kbv/1.0.2/bundle/sdf6s75f-d959-43f0-8ac4-sd6f7sd6.xml",
            AnonymizationType.REPLACING,
            new BlackingStrategy()),
        Arguments.of(
            "fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml",
            AnonymizationType.REPLACING,
            new BlackingStrategy()));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/sdf6s75f-d959-43f0-8ac4-sd6f7sd6.xml",
        "fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml"
      })
  void shouldAnonymizePatient(String path) {
    val content = ResourceUtils.readFileFromResource(path);
    val bundle = parser.decode(KbvErpBundle.class, content);

    val patient =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
            .map(resource -> (KbvPatient) resource)
            .findFirst()
            .orElseThrow();

    val anonymizer = new AnonymizerFacade();

    val originalName = patient.getFullname();
    val originalKvnr = patient.getKvnr();
    assertTrue(anonymizer.anonymize(patient));

    val anonymizedName = patient.getFullname();
    assertNotEquals(originalName, anonymizedName);
    assertEquals(originalName.length(), anonymizedName.length());

    val anonymizedKvnr = patient.getKvnr();
    assertNotEquals(originalKvnr.getValue(), anonymizedKvnr.getValue());
    assertEquals(originalKvnr.getValue().length(), anonymizedKvnr.getValue().length());
    assertEquals(originalKvnr.getSystemAsString(), anonymizedKvnr.getSystemAsString());
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/sdf6s75f-d959-43f0-8ac4-sd6f7sd6.xml",
        "fhir/valid/kbv/1.1.0/bundle/3a1c45f8-d959-43f0-8ac4-9959be746188.xml"
      })
  void shouldAnonymizeWithoutFamilyNameExtensions(String path) {
    val content = ResourceUtils.readFileFromResource(path);
    val bundle = parser.decode(KbvErpBundle.class, content);

    val patient =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
            .map(resource -> (KbvPatient) resource)
            .findFirst()
            .orElseThrow();
    patient.getName().forEach(hm -> hm.getFamilyElement().setExtension(new LinkedList<>()));

    val anonymizer = new AnonymizerFacade();
    assertDoesNotThrow(() -> anonymizer.anonymize(patient));
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "fhir/valid/kbv/1.0.2/bundle/15da065c-5b75-4acf-a2ba-1355de821d6e.xml",
        "fhir/valid/kbv/1.1.0/bundle/5f66314e-459a-41e9-a3d7-65c935a8be2c.xml"
      })
  void shouldAnonymizePractitioner(String path) {
    val content = ResourceUtils.readFileFromResource(path);
    val bundle = parser.decode(KbvErpBundle.class, content);

    val practitioner =
        bundle.getEntry().stream()
            .map(Bundle.BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Practitioner))
            .map(resource -> (KbvPractitioner) resource)
            .findFirst()
            .orElseThrow();

    val anonymizer = new AnonymizerFacade();
    val originalANR = practitioner.getANR();

    anonymizer.anonymize(practitioner);
    val anonymizedANR = practitioner.getANR();

    assertNotEquals(originalANR.getValue(), anonymizedANR.getValue());
    assertEquals(originalANR.getValue().length(), anonymizedANR.getValue().length());
    assertEquals(originalANR.getCodeSystemUrl(), anonymizedANR.getCodeSystemUrl());
  }
}
