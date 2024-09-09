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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import lombok.val;
import org.junit.jupiter.api.Test;

class MedicalOrganizationFakerTest extends ParsingTest {
  @Test
  void builderFakerMedicalOrganizationWithName() {
    val organization = MedicalOrganizationFaker.builder().withName(fakerName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithBsnr() {
    val organization = MedicalOrganizationFaker.builder().withBsnr(fakerBsnr()).fake();
    val organization2 = MedicalOrganizationFaker.builder().withBsnr(BSNR.random()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    val result2 = ValidatorUtil.encodeAndValidate(parser, organization2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithPhone() {
    val organization = MedicalOrganizationFaker.builder().withPhoneNumber(fakerPhone()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithEmail() {
    val organization = MedicalOrganizationFaker.builder().withEmail(fakerEMail()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithAddress() {
    val organization =
        MedicalOrganizationFaker.builder()
            .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }
}
