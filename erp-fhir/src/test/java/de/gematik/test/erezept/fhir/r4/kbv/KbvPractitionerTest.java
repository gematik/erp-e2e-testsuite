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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.LANR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class KbvPractitionerTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/kbv/1.1.0/bundle/";

  @Test
  void shouldEncodePractitionerWithoutANRFromKbvBundle() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val expName = "Ben Schulz";
    val expQualification = QualificationType.DOCTOR;
    val expAdditionalQualification = "Facharzt für Allgemeinmedizin";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    assertNotNull(practitioner);
    assertEquals(expName, practitioner.getFullName());
    assertEquals(expQualification, practitioner.getQualificationType());
    assertEquals(1, practitioner.getAdditionalQualifications().size());
    assertEquals(expAdditionalQualification, practitioner.getAdditionalQualifications().get(0));
  }

  @Test
  void shouldEncodePractitionerWitLANRFromKbvBundle() {
    val kbvId = "5f66314e-459a-41e9-a3d7-65c935a8be2c";
    val fileName = kbvId + ".xml";

    val expLanr = new LANR("754236701");

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    assertNotNull(practitioner);

    val actualLanr = practitioner.getANR().orElseThrow();
    assertEquals(expLanr, actualLanr);
    assertEquals(BaseANR.ANRType.LANR, actualLanr.getType());

    // just for the sake of code-coverage
    assertNotNull(practitioner.getDescription());
  }

  @Test
  void shouldThrowOnMissingFields() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    practitioner.setQualification(List.of());
    practitioner.setIdentifier(List.of());
    assertThrows(MissingFieldException.class, practitioner::getQualificationType);
    val anrOptional = assertDoesNotThrow(practitioner::getANR);
    assertTrue(anrOptional.isEmpty());
  }

  @Test
  void shouldGetSamePractitioner() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    val practitioner2 = KbvPractitioner.fromPractitioner(practitioner);
    assertEquals(practitioner, practitioner2);
  }

  @Test
  void shouldReadAsvFachgruppennummer() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val practitioner = kbvBundle.getPractitioner();

    val qtOptional = practitioner.getAsvFachgruppennummer();
    assertNotNull(qtOptional);
    assertTrue(qtOptional.isPresent());
    assertEquals("555555801", qtOptional.orElseThrow().getValue());
  }

  @Test
  void shouldGetPractitionerFromResource() {
    val resource = new Practitioner();
    val practitioner = KbvPractitioner.fromPractitioner((Resource) resource);
    assertNotNull(practitioner);
    assertNotEquals(resource, practitioner);
  }
}
