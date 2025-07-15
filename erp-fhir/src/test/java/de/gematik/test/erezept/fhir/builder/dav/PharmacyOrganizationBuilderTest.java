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

package de.gematik.test.erezept.fhir.builder.dav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.Country;
import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class PharmacyOrganizationBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(
      name = "[{index}] -> Build DAV DispensedMedication with E-Rezept FHIR Profiles {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#abdaErpPkvVersions")
  void buildPharmacyOrganizationWithFixedValues(AbdaErpPkvVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val pharmacy =
        PharmacyOrganizationBuilder.builder()
            .version(version)
            .setId(organizationResourceId)
            .name("Adler-Apotheke")
            .iknr(IKNR.asDefaultIknr("757299999"))
            // .phone("+490309876543")  // well, possible but not allowed by DAV profile
            // .email("info@adler.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
    assertTrue(result.isSuccessful());
    assertEquals("Adler-Apotheke", pharmacy.getName());
    assertEquals(IKNR.asDefaultIknr("757299999"), pharmacy.getIknr());
  }

  @Test
  void buildPharmacyOrganizationWithFaker() {
    for (var i = 0; i < 5; i++) {
      val pharmacy = PharmacyOrganizationFaker.builder().fake();
      val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
      assertTrue(result.isSuccessful());
    }
  }
}
