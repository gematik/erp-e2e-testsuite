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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

class MedicalOrganizationBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV MedicalOrganization with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildMedicalOrganizationWithFixedValues(KbvItaForVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        MedicalOrganizationBuilder.builder()
            .version(version)
            .setResourceId(organizationResourceId)
            .name("Arztpraxis Meyer")
            .bsnr(BSNR.from("757299999"))
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build KBV MedicalOrganization with Faker and KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildMedicalOrganizationWithFaker(KbvItaForVersion version) {
    val organization = MedicalOrganizationBuilder.faker().version(version).build();
    System.out.println(organization.getStreet());
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build empty KBV MedicalOrganization with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnEmptyMedicalOrganizationBuilder(KbvItaForVersion version) {
    val ob = MedicalOrganizationBuilder.builder().version(version);
    assertThrows(BuilderException.class, ob::build);
  }
}
