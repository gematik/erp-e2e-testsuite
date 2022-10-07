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
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class KbvOrganizationTest {

  private final String BASE_PATH = "fhir/valid/kbv/bundle/";

  private FhirParser parser;

  @Before
  public void setUp() {
    this.parser = new FhirParser();
  }

  @Test
  public void testEncodingOrganizationFromKbvBundle() {
    val kbvId = "5a3458b0-8364-4682-96e2-b262b2ab16eb";
    val fileName = kbvId + ".xml";

    val expBsnr = new BSNR("724444400");
    val expName = "Hausarztpraxis";
    val expPhone = "030321654987";
    val expMail = "hausarztpraxis@e-mail.de";
    val expCity = "Berlin";
    val expPostal = "10623";
    val expCountry = Country.D;
    val expStreet = "Herbert-Lewin-Platz 2 Erdgeschoss";

    val content = ResourceUtils.readFileFromResource(BASE_PATH + fileName);
    val kbvBundle = parser.decode(KbvErpBundle.class, content);
    val organization = kbvBundle.getMedicalOrganization();

    assertNotNull(organization);
    assertEquals(expBsnr, organization.getBsnr());
    assertEquals(expName, organization.getName());
    assertEquals(expPhone, organization.getPhone());
    assertEquals(expMail, organization.getMail());
    assertEquals(expCity, organization.getCity());
    assertEquals(expPostal, organization.getPostalCode());
    assertEquals(expCountry, organization.getCountry());
    assertEquals(expStreet, organization.getStreet());
  }
}
