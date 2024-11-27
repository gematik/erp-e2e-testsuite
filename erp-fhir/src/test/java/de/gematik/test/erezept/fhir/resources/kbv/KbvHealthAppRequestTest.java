/*
 * Copyright 2024 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import java.util.UUID;
import lombok.val;
import org.hl7.fhir.r4.model.DeviceRequest;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.Test;

class KbvHealthAppRequestTest extends ParsingTest {

  private static final String BASE_PATH_1_0_0 = "fhir/valid/kbv_evdga/1.0.0/";

  @Test
  void shouldGetHealthAppRequestFromBundle() {
    val expectedID = "EVDGA_Bundle";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_0_0 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);
    val appRequest = bundle.getHealthAppRequest();
    assertNotNull(appRequest);

    assertEquals("Vantis KHK und Herzinfarkt 001", appRequest.getName());
    assertEquals("19205615", appRequest.getPzn().getValue());
    assertFalse(appRequest.getDescription().isEmpty());

    assertFalse(appRequest.relatesToSocialCompensationLaw());
    assertFalse(appRequest.hasAccidentExtension());
  }

  @Test
  void shouldGetHealthAppRequestForWorkAccident() {
    val expectedID = "EVDGA_Bundle_BG_Arbeitsunfall_3";
    val fileName = expectedID + ".xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_0_0 + fileName);
    val bundle = parser.decode(KbvEvdgaBundle.class, content);
    val appRequest = bundle.getHealthAppRequest();
    assertNotNull(appRequest);

    assertEquals("companion patella", appRequest.getName());
    assertEquals("17850263", appRequest.getPzn().getValue());

    assertTrue(appRequest.getAccidentCause().isPresent());
    assertEquals(AccidentCauseType.ACCIDENT_AT_WORK, appRequest.getAccidentCause().get());

    assertTrue(appRequest.getAccidentWorkplace().isPresent());
    assertEquals("Dummy-Betrieb", appRequest.getAccidentWorkplace().get());

    assertTrue(appRequest.getAccidentDate().isPresent());
    assertEquals(
        DateConverter.getInstance().dateFromIso8601("2023-03-26"),
        appRequest.getAccidentDate().get());
  }

  @Test
  void shouldCopyFromDeviceRequest() {
    val originalId = UUID.randomUUID().toString();
    val original = new DeviceRequest();
    original.setId(originalId);

    val healthAppRequest = KbvHealthAppRequest.fromDeviceRequest((Resource) original);
    assertEquals(originalId, healthAppRequest.getId());
    assertNotEquals(original, healthAppRequest);
  }

  @Test
  void shouldNotCopyFromHealtAppRequest() {
    val originalId = UUID.randomUUID().toString();
    val original = new KbvHealthAppRequest();
    original.setId(originalId);

    val healthAppRequest = KbvHealthAppRequest.fromDeviceRequest((Resource) original);
    assertEquals(originalId, healthAppRequest.getId());
    assertEquals(original, healthAppRequest);
  }
}
