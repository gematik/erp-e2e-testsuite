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

package de.gematik.test.erezept.fhir.builder.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class EuOrganizationFakerTest extends ErpFhirParsingTest {

  @Test
  void shouldCreateOrganizationWithName() {
    var org = EuOrganizationFaker.faker().fake();
    assertNotNull(org.getNameOptional());
    assertFalse(org.getNameOptional().isEmpty());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldCreateOrganizationWithCountry() {
    var org = EuOrganizationFaker.faker().withNcpehCountry(IsoCountryCode.FR).fake();
    assertTrue(org.getEuCountry().isPresent());
    assertInstanceOf(IsoCountryCode.class, org.getEuCountry().get());
  }

  @Test
  void shouldCreateOrganizationWithTelematikID() {
    var org =
        EuOrganizationFaker.faker().withIdentifier(TelematikID.random().asIdentifier()).fake();
    assertTrue(org.getTelematikID().isPresent());
    assertInstanceOf(TelematikID.class, org.getTelematikID().get());
  }

  @Test
  void shouldCreateOrganizationWithBSNR() {
    var org = EuOrganizationFaker.faker().withIdentifier(BSNR.random().asIdentifier()).fake();
    assertTrue(org.getBsnr().isPresent());
    assertInstanceOf(BSNR.class, org.getBsnr().get());
  }

  @Test
  void shouldCreateOrganizationWithUnknownIdentifier() {
    var org = EuOrganizationFaker.faker().fake();
    assertNotNull(org.getUnknownIdentifier());
    assertFalse(org.getUnknownIdentifier().isEmpty());
    for (Identifier id : org.getUnknownIdentifier()) {
      assertNotNull(id.getValue());
    }
  }

  @Test
  void shouldCreateOrganizationWithProviderType() {
    var org = EuOrganizationFaker.faker().fake();
    assertTrue(org.getProviderType().isPresent());
    assertNotNull(org.getProviderType().get().getCode());
    assertNotNull(org.getProviderType().get().getDisplay());
  }

  @Test
  void shouldCreateOrganizationWithProfession() {
    var org = EuOrganizationFaker.faker().withProfession("Veterinärmediziner").fake();
    assertTrue(org.getProfession().isPresent());
    assertNotNull(org.getProfession().get());
  }

  @Test
  void shouldCreateOrganizationWithAddress() {
    var org = EuOrganizationFaker.faker().fake();
    assertTrue(org.getEuAddress().isPresent());
    assertNotNull(org.getEuAddress().get().getCity());
  }

  @Test
  void shouldCreateOrganizationWithSpecificName() {
    var org = EuOrganizationFaker.faker().withName("TestName").fake();
    assertNotNull(org.getNameOptional());
    assertEquals("TestName", org.getNameOptional().orElseThrow());
  }

  @Test
  void shouldCreateOrganizationWithSpecificVersion() {
    var org = EuOrganizationFaker.faker().withVersion(EuVersion.V1_0).fake();
    assertTrue(org.getMeta().getProfile().get(0).asStringValue().contains("|1.0"));
  }

  @Test
  void shouldCreateOrganizationWithDescription() {
    var org = EuOrganizationFaker.faker().fake();
    assertNotNull(org.getDescription());
    assertFalse(org.getDescription().isEmpty());
  }
}
