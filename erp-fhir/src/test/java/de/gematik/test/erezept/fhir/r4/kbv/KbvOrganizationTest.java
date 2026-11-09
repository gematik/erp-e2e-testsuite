/*
 * Copyright (Change Date see Readme), gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.BSNR;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvOrganizationTest extends ErpFhirParsingTest {

  private static final String PATH =
      "fhir/valid/kbv/1.4.4/Beispiel_70_TRp_Flag_true_Dos_existant_Positiv.xml";

  @Test
  void testEncodingOrganizationFromKbvBundle() {
    val expBsnr = new BSNR("723333300");
    val expName = "Praxis für Psychiatrie und Psychotherapie";
    val expPhone = "030369258147";
    val expMail = "therapiepraxis@e-mail.de";
    val expCity = "Berlin";
    val expPostal = "10623";
    val expCountry = Country.D;
    val expStreet = "Herbert-Lewin-Platz 2";

    val content = ResourceLoader.readFileFromResource(PATH);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val organization = kbvBundle.getMedicalOrganization();

    assertNotNull(organization);
    assertEquals(expBsnr, organization.getBsnrOrThrow());
    assertEquals(expName, organization.getName());
    assertEquals(expPhone, organization.getPhone());
    assertEquals(expMail, organization.getMail());
    assertEquals(expCity, organization.getCity());
    assertEquals(expPostal, organization.getPostalCode());
    assertEquals(expCountry, organization.getCountry());
    assertEquals(expStreet, organization.getStreet());
    assertNotNull(organization.getDescription());
  }
}
