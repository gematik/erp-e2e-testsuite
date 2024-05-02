/*
 * Copyright 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.references.kbv.RequesterReference;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.IKNR;
import lombok.val;
import org.junit.jupiter.api.Test;

class AssignerOrganizationFakerTest extends ParsingTest {
  @Test
  void buildFakerAssignerOrganizationWithVersion() {
    val fakeOrganization =
        AssignerOrganizationFaker.builder()
            .withVersion(KbvItaForVersion.getDefaultVersion())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, fakeOrganization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerAssignerOrganizationWithAddress() {
    val fakeOrganization =
        AssignerOrganizationFaker.builder()
            .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val fakeOrganization2 =
        AssignerOrganizationFaker.builder()
            .withAddress(fakerCountry(), fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, fakeOrganization);
    val result2 = ValidatorUtil.encodeAndValidate(parser, fakeOrganization2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerAssignerOrganizationWithIknr() {
    val iknr = IKNR.random();
    val fakeOrganization = AssignerOrganizationFaker.builder().withIknr(iknr).fake();
    val fakeOrganization2 = AssignerOrganizationFaker.builder().withIknr(iknr.getValue()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, fakeOrganization);
    val result2 = ValidatorUtil.encodeAndValidate(parser, fakeOrganization2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void buildFakerAssignerOrganizationWithReference() {
    val fakerOrganization =
        AssignerOrganizationFaker.builder()
            .withReference(
                new RequesterReference(PractitionerFaker.builder().fake().getId()).asReference())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, fakerOrganization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakerAssignerOrganizationWithPhone() {
    val fakerOrganization = AssignerOrganizationFaker.builder().withPhone(fakerPhone()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, fakerOrganization);
    assertTrue(result.isSuccessful());
  }
}
