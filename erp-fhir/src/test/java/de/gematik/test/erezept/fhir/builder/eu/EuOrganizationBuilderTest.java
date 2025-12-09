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

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class EuOrganizationBuilderTest extends ErpFhirParsingTest {
  EuOrganizationBuilder orgBuilder =
      EuOrganizationBuilder.builder("TestOrg")
          .identifier(new Identifier().setValue("value"))
          .address(
              new Address()
                  .addLine(GemFaker.fakerStreetName())
                  .setCity(GemFaker.fakerCity())
                  .setState(GemFaker.getFaker().address().state())
                  .setPostalCode(GemFaker.fakerBsnr())
                  .setCountry(GemFaker.fakerCountry().name()));

  @Test
  void shouldBuildOrganizationWithName() {
    val org = orgBuilder.build();
    assertEquals("TestOrg", org.getNameOptional().orElseThrow());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithCountry() {
    val org = orgBuilder.ncpehCountry(IsoCountryCode.DE).build();
    assertTrue(org.getEuCountry().isPresent());
    assertEquals(IsoCountryCode.DE, org.getEuCountry().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithTelematikID() {
    var telematikId = TelematikID.from("TestTelematikID");
    val org = orgBuilder.identifier(telematikId).build();
    assertTrue(org.getTelematikID().isPresent());
    assertEquals(telematikId, org.getTelematikID().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithBSNR() {
    var bsnr = BSNR.from("123456789");
    val org = orgBuilder.identifier(bsnr).build();
    assertTrue(org.getBsnr().isPresent());
    assertEquals(bsnr, org.getBsnr().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithKZVA() {
    var kzva = KZVA.from("987654321");
    val org = orgBuilder.identifier(kzva).build();
    assertTrue(org.getKzva().isPresent());
    assertEquals(kzva, org.getKzva().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithIKNR() {
    var iknr = IKNR.asArgeIknr("123456789");
    val org = orgBuilder.identifier(iknr).build();
    assertTrue(org.getIknr().isPresent());
    assertEquals(iknr, org.getIknr().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithUnknownIdentifier() {
    var identifier = new Identifier().setSystem("http://test.system").setValue("TestValue");
    val org = orgBuilder.identifier(identifier).build();
    assertFalse(org.getUnknownIdentifier().isEmpty());
    assertEquals("TestValue", org.getUnknownIdentifier().get(0).getValue());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithProviderType() {
    var providerType = EuHealthcareFacilityType.getDefault();
    val org = orgBuilder.providerType(providerType).build();
    assertTrue(org.getProviderType().isPresent());
    assertEquals(providerType.getCode(), org.getProviderType().get().getCode());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithProfession() {
    val org = orgBuilder.profession("Arzt").build();
    assertTrue(org.getProfession().isPresent());
    assertEquals("Arzt", org.getProfession().get());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldBuildOrganizationWithAddress() {
    val org = orgBuilder.build();
    assertTrue(org.getEuAddress().isPresent());
    assertNotNull(org.getEuAddress().get().getCity());
    assertNotNull(org.getEuAddress().get().getLine());
    assertNotNull(org.getEuAddress().get().getState());
    assertNotNull(org.getEuAddress().get().getCountry());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldGetFromEuOrganization() {
    val org = orgBuilder.build();
    val res = EuOrganization.fromOrganisation(org);
    assertFalse(res.isEmpty());
    assertEquals(org.getName(), res.getName());
  }

  @Test
  void shouldBuildOrganizationWithDescription() {
    val org = orgBuilder.build();
    assertNotNull(org.getDescription());
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }
}
