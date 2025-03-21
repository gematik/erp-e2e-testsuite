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

package de.gematik.test.erezept.fhir.r4.kbv;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.Enumerations;
import org.hl7.fhir.r4.model.Patient;
import org.junit.jupiter.api.Test;

class KbvBasePatientTest extends ErpFhirParsingTest {

  private static final String BASE_PATH = "fhir/valid/kbv/1.0.2/patient/";

  @Test
  void encodeErwinFischer() {
    val fileExtension = ".xml";
    val fileName = "erwin_fleischer" + fileExtension;

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val erwin = parser.decode(KbvPatient.class, content);
    assertNotNull(erwin);

    val expectedID = "9774f67f-a238-4daf-b4e6-679deeef3811";
    assertEquals(expectedID, erwin.getLogicalId());

    val expectedGender = Enumerations.AdministrativeGender.MALE;
    assertEquals(expectedGender, erwin.getGender());

    assertTrue(erwin.hasGkvKvnr());
    val expectedGkvKvnr = "M234567890";
    assertEquals(expectedGkvKvnr, erwin.getGkvId().orElseThrow().getValue());

    assertEquals("Fleischer, Erwin", erwin.getFullname());
    assertNotNull(erwin.getDescription());
  }

  @Test
  void shouldGetKbvPatientFromResource() {
    val fileExtension = ".xml";
    val fileName = "erwin_fleischer" + fileExtension;

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val erwin = parser.decode(content);
    assertNotNull(erwin);
    assertEquals(Patient.class, erwin.getClass());

    val kbvPatient = KbvPatient.fromPatient(erwin);
    assertNotNull(kbvPatient);
    assertEquals(KbvPatient.class, kbvPatient.getClass());
  }
}
