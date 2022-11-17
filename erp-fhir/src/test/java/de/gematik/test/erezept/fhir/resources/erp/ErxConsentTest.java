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

package de.gematik.test.erezept.fhir.resources.erp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import de.gematik.test.erezept.fhir.valuesets.ConsentScope;
import de.gematik.test.erezept.fhir.valuesets.ConsentType;
import lombok.val;
import org.hl7.fhir.r4.model.Consent;
import org.junit.Before;
import org.junit.Test;

public class ErxConsentTest {

  private final String BASE_PATH = "fhir/valid/erp/1.1.1/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void shouldEncodeSingleConsent() {
    val fileName = "Consent_01.xml";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val consent = parser.decode(ErxConsent.class, content);
    assertNotNull("Valid ErxConsent must be parseable", consent);

    assertEquals(Consent.ConsentState.ACTIVE, consent.getStatus());
    assertEquals(ConsentType.CHARGCONS, consent.getConsentType());
    assertEquals(ConsentScope.PATIENT_PRIVACY, consent.getConsentScope());
    assertEquals("X123456789", consent.getPatientKvid());
  }
}
