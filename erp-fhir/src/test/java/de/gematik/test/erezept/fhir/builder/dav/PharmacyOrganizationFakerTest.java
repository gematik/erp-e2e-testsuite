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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCountry;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;

class PharmacyOrganizationFakerTest extends ErpFhirParsingTest {
  @Test
  void buildFakePharmacyOrganizationWithVersion() {
    val pharmacy =
        PharmacyOrganizationFaker.builder()
            .withVersion(AbdaErpPkvVersion.getDefaultVersion())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePharmacyOrganizationWithName() {
    val pharmacy = PharmacyOrganizationFaker.builder().withName(fakerName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakePharmacyOrganizationWithIknr() {
    val iknr = IKNR.randomSidIknr();
    val pharmacy = PharmacyOrganizationFaker.builder().withIknr(iknr).fake();
    val pharmacy2 = PharmacyOrganizationFaker.builder().withIknr(iknr.getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
    val result2 = ValidatorUtil.encodeAndValidate(parser, pharmacy2);
    assertEquals(iknr, pharmacy.getIknr());
    assertEquals(iknr, pharmacy2.getIknr());
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakePharmacyOrganizationWithAddress() {
    val pharmacy =
        PharmacyOrganizationFaker.builder()
            .withAddress(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val pharmacy2 =
        PharmacyOrganizationFaker.builder()
            .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, pharmacy);
    val result2 = ValidatorUtil.encodeAndValidate(parser, pharmacy2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }
}
