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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ErxConsentBundleTest extends ParsingTest {

  private static final String BASE_PATH = "fhir/valid/erp/1.2.0/consent/";

  @Test
  void shouldEncodeSingleConsentBundle() {
    val fileName = "bundle_6daaade4-6523-4136-94bf-cbc5a247cc7b.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH + fileName);
    val consentBundle = parser.decode(ErxConsentBundle.class, content);
    assertNotNull(consentBundle, "Valid ErxConsentBundle must be parseable");

    assertTrue(consentBundle.hasConsent());
    assertTrue(consentBundle.getConsent().isPresent());

    val consent = consentBundle.getConsent().orElseThrow();
    assertEquals("CHARGCONS-X110465770", consent.getConsentId());
  }
}
