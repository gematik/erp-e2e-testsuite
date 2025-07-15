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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle.BundleEntryComponent;
import org.hl7.fhir.r4.model.ResourceType;
import org.junit.jupiter.api.Test;

class KbvPatientTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_1_0 = "fhir/valid/kbv/1.1.0/bundle/";

  @Test
  void shouldDecodeWithoutExpectedType() {
    val expectedID = "3a1c45f8-d959-43f0-8ac4-9959be746188";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
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

  @Test
  void shouldGetKvnr() {
    val expectedID = "1f339db0-9e55-4946-9dfa-f1b30953be9b";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val bundle = parser.decode(KbvErpBundle.class, content);
    val patient = bundle.getPatient();
    assertEquals("K220635158", patient.getKvnr().getValue());
  }

  @Test
  void shouldThrowOnMissingIdentifiers() {
    val expectedID = "baac1cdd-1313-468f-9c9e-bde74acb308e";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_1_0 + fileName);
    val bundle = parser.decode(KbvErpBundle.class, content);
    val patient = bundle.getPatient();

    patient.setIdentifier(List.of()); // remove all identifier

    assertThrows(MissingFieldException.class, patient::getKvnr);
    assertThrows(MissingFieldException.class, patient::getInsuranceType);
  }

  @Test
  void shouldDetectGkv() {
    val patient = KbvPatientFaker.builder().withInsuranceType(InsuranceTypeDe.GKV).fake();
    assertEquals(InsuranceTypeDe.GKV, patient.getInsuranceType());
  }

  @Test
  void shouldDetectPkv() {
    val patient = KbvPatientFaker.builder().withInsuranceType(InsuranceTypeDe.PKV).fake();
    if (KbvItaErpVersion.getDefaultVersion().compareTo(KbvItaErpVersion.V1_1_0) <= 0) {
      assertEquals(InsuranceTypeDe.PKV, patient.getInsuranceType());

    } else {
      assertEquals(InsuranceTypeDe.GKV, patient.getInsuranceType());
    }
  }
}
