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

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class KbvPatientTest extends ParsingTest {

  private final String BASE_PATH_1_0_2 = "fhir/valid/kbv/1.0.2/bundle/";
  private final String BASE_PATH_1_1_0 = "fhir/valid/kbv/1.1.0/bundle/";
  
  @Test
  void shouldDecodeWithoutExpectedType() {
    val expectedID = "sdf6s75f-d959-43f0-8ac4-sd6f7sd6";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val bundle = parser.decode(KbvErpBundle.class, content);

    val patient =
        bundle.getEntry().stream()
            .map(BundleEntryComponent::getResource)
            .filter(resource -> resource.getResourceType().equals(ResourceType.Patient))
            .findFirst()
            .orElseThrow();
    assertEquals(KbvPatient.class, patient.getClass());
    assertEquals(patient, bundle.getPatient()); // don't copy the object!
  }

  @ParameterizedTest
  @ValueSource(strings = {BASE_PATH_1_0_2, BASE_PATH_1_1_0})
  void shouldGetKvnr(String basePath) {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(basePath + fileName);
    val bundle = parser.decode(KbvErpBundle.class, content);
    val patient = bundle.getPatient();
    assertEquals("K220635158", patient.getKvnr().getValue());
  }
  
  @Test
  void shouldThrowOnMissingIdentifiers() {
    val expectedID = "sdf6s75f-d959-43f0-8ac4-sd6f7sd6";
    val fileName = expectedID + ".xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH_1_0_2 + fileName);
    val bundle = parser.decode(KbvErpBundle.class, content);
    val patient = bundle.getPatient();
    
    patient.setIdentifier(List.of()); // remove all identifier
    
    assertThrows(MissingFieldException.class, patient::getKvnr);
    assertThrows(MissingFieldException.class, patient::getInsuranceKind);
  }
}
