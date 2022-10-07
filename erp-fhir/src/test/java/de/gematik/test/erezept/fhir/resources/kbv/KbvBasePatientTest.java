/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.resources.kbv;

import static org.junit.Assert.*;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import lombok.val;
import org.hl7.fhir.r4.model.Enumerations;
import org.junit.Before;
import org.junit.Test;

public class KbvBasePatientTest {

  private final String BASE_PATH = "fhir/valid/kbv/patient/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void encodeErwinFischer() {
    val fileExtension = ".xml";
    val fileName = "erwin_fleischer" + fileExtension;

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val erwin = parser.decode(KbvPatient.class, content);
    assertNotNull(erwin);

    val expectedID = "9774f67f-a238-4daf-b4e6-679deeef3811";
    assertEquals(expectedID, erwin.getLogicalId());

    val expectedGender = Enumerations.AdministrativeGender.MALE;
    assertEquals(expectedGender, erwin.getGender());

    assertTrue(erwin.hasGkvId());
    val expectedKvid = "M234567890";
    assertEquals(expectedKvid, erwin.getKvid().orElseThrow());

    assertTrue(erwin.hasPkvId());
    val expectedPkvId = "2345678900";
    assertEquals(expectedPkvId, erwin.getPkvId().orElseThrow());
  }
}
