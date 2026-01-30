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

package de.gematik.test.erezept.fhir.r4.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.eu.EuOrganizationBuilder;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.KZVA;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Address;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class EuOrganizationTest extends ErpFhirParsingTest {

  private final EuOrganizationBuilder builder =
      EuOrganizationBuilder.builder("Test Pharmacy")
          .version(EuVersion.getDefaultVersion())
          .ncpehCountry(IsoCountryCode.BE)
          .identifier(TelematikID.from("12345678901234567890"))
          .identifier(
              new Identifier()
                  .setValue("nobodyKnowsWhatWillHappen")
                  .setSystem("http://local-identifier.esp/pharmacia-id"))
          .identifier(BSNR.random())
          .identifier(KZVA.random())
          .identifier(IKNR.random())
          .providerType(EuHealthcareFacilityType.getDefault())
          .profession("pharmacist")
          .address(
              new Address()
                  .addLine("137 Nowhere Street")
                  .setCity("Elsewhere in Nowhere")
                  .setState("NoMansLand")
                  .setPostalCode("123-B-friendly")
                  .setCountry("Zimbabwe"));

  @Test
  void shouldGetADescription() {
    val orga = builder.build();
    assertNotNull(orga.getDescription());
  }

  @Test
  void shouldGetEuNameCorrect() {
    val org = builder.build();
    assertEquals("Test Pharmacy", org.getNameOptional().orElseThrow());
  }

  @Test
  void shouldGetIdentifierBsnrCorrect() {
    val testdata = BSNR.random();
    val org = builder.identifier(testdata).build();
    assertEquals(Optional.of(testdata), org.getBsnr());
  }

  @Test
  void shouldGetIdentifierTelematikIdCorrect() {
    val testdata = TelematikID.random();
    val org = builder.identifier(testdata).build();
    assertEquals(testdata, org.getTelematikID().orElseThrow());
  }

  @Test
  void shouldGetIdentifierKzvaCorrect() {
    val testdata = KZVA.random();
    val org = builder.identifier(testdata).build();
    assertEquals(testdata, org.getKzva().orElseThrow());
  }

  @Test
  void shouldGetIdentifierIknrCorrect() {
    val testdata = IKNR.random();
    val org = builder.identifier(testdata).build();
    assertEquals(testdata, org.getIknr().orElseThrow());
  }

  @Test
  void shouldGetIdentifierUnknownCorrect() {
    val testdata = new Identifier().setValue("value").setSystem("international.whatEver/System");
    val org = builder.identifier(testdata).build();
    assertEquals(testdata.getSystem(), org.getUnknownIdentifier().get(0).getSystem());
    assertEquals(testdata.getValue(), org.getUnknownIdentifier().get(0).getValue());
  }

  @Test
  void shouldGetIdentifierUnknownCorrectWithoutSystem() {
    val testdata = new Identifier().setValue("TestValue");
    val org = builder.identifier(testdata).build();
    assertEquals(testdata.getValue(), org.getUnknownIdentifier().get(0).getValue());
    assertNull(org.getUnknownIdentifier().get(0).getSystem());
  }

  @Test
  void shouldGetProviderCorrect() {
    val org =
        builder
            .providerType(new EuHealthcareFacilityType("org-456", "Countryside Apothecary"))
            .build();
    assertEquals("org-456", org.getProviderType().orElseThrow().getCode());
    assertEquals("Countryside Apothecary", org.getProviderType().orElseThrow().getDisplay());
    assertTrue(
        ErpWorkflowCodeSystem.PROFESSION_OID.matches(
            org.getProviderType().orElseThrow().getSystem()));
  }

  @Test
  void shouldGetCountryCodeCorrect() {
    val org = builder.ncpehCountry(IsoCountryCode.FR).build();
    assertEquals(IsoCountryCode.FR, org.getEuCountry().orElseThrow());
  }

  @Test
  void shouldGetProfessionCorrect() {
    val org = builder.profession("profession").build();
    assertEquals(Optional.of("profession"), org.getProfession());
  }

  @Test
  void shouldGetAddressCorrect() {
    val org = builder.build();
    assertEquals(
        "137 Nowhere Street", org.getEuAddress().orElseThrow().getLine().get(0).getValue());
  }

  @Test
  void shouldGetNameOptional() {
    assertEquals("Test Pharmacy", builder.build().getNameOptional().orElseThrow());
  }

  @Test
  void shouldBeValid() {
    val org = builder.build();
    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }

  @Test
  void minimalOrganizationShouldBeValid() {
    val org =
        EuOrganizationBuilder.builder("TestNmae")
            .identifier(new Identifier().setValue("value"))
            .address(
                new Address()
                    .addLine(GemFaker.fakerStreetName())
                    .setCity(GemFaker.fakerCity())
                    .setState(GemFaker.getFaker().address().state())
                    .setPostalCode(GemFaker.fakerBsnr())
                    .setCountry(GemFaker.fakerCountry().name()))
            .build();

    val res = ValidatorUtil.encodeAndValidate(parser, org);
    assertTrue(res.isSuccessful());
  }
}
