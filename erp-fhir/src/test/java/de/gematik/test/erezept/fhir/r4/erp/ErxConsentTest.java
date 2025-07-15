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

package de.gematik.test.erezept.fhir.r4.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.valueset.ConsentScope;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.junit.jupiter.api.Test;

class ErxConsentTest extends ErpFhirParsingTest {

  private static final String BASE_PATH_1_4 = "fhir/valid/erp/1.4.0/consent/";

  @Test
  void shouldEncodeSingleConsent() {
    val fileName = "Consent_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val consent = parser.decode(ErxConsent.class, content);
    assertNotNull(consent, "Valid ErxConsent must be parseable");

    assertEquals(Consent.ConsentState.ACTIVE, consent.getStatus());
    assertEquals(ConsentType.CHARGCONS, consent.getConsentType());
    assertEquals(ConsentScope.PATIENT_PRIVACY, consent.getConsentScope());
    assertEquals("U136787649", consent.getPatientKvid());
  }

  @Test
  void shouldThrowOnMissingConsentType() {
    val fileName = "Consent_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val consent = parser.decode(ErxConsent.class, content);
    assertNotNull(consent, "Valid ErxConsent must be parseable");

    consent.setCategory(List.of()); // remove all categories with the given consent type
    assertThrows(MissingFieldException.class, consent::getConsentType);
  }

  @Test
  void shouldThrowInvalidConsentTypeSystems() {
    val fileName = "Consent_01.xml";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val consent = parser.decode(ErxConsent.class, content);
    assertNotNull(consent, "Valid ErxConsent must be parseable");

    consent
        .getCategory()
        .forEach(
            category ->
                category
                    .getCoding()
                    .forEach(
                        coding -> {
                          val s = coding.getSystem();
                          coding.setSystem(s.replace("https://gematik.de", "https://abc.de"));
                        }));
    assertThrows(MissingFieldException.class, consent::getConsentType);
  }

  @Test
  void shouldGetConsentTypes() {
    val fileName = "bundle_6daaade4-6523-4136-94bf-cbc5a247cc7b.json";

    val content = ResourceLoader.readFileFromResource(BASE_PATH_1_4 + fileName);
    val consentBundle = parser.decode(ErxConsentBundle.class, content);
    assertNotNull(consentBundle, "Valid ErxConsentBundle must be parseable");

    assertTrue(consentBundle.getConsent().isPresent());
    val consent = consentBundle.getConsent().orElseThrow();

    assertEquals(Consent.ConsentState.ACTIVE, consent.getStatus());
    assertEquals(ConsentType.CHARGCONS, consent.getConsentType());
    assertEquals(ConsentScope.PATIENT_PRIVACY, consent.getConsentScope());
    assertEquals("X110465770", consent.getPatientKvid());
  }
}
