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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.Country;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
      name = "[{index}] -> Build KBV Dental MedicalOrganization with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildDentalMedicalOrganizationWithFixedValues(KbvItaForVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        MedicalOrganizationBuilder.builder()
            .version(version)
            .setResourceId(organizationResourceId)
            .name("Zahnarztpraxis Meyer")
            .kzva("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build KBV Hospital MedicalOrganization with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildHospitalMedicalOrganizationWithFixedValues(KbvItaForVersion version) {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        MedicalOrganizationBuilder.builder()
            .version(version)
            .setResourceId(organizationResourceId)
            .name("Zahnarztpraxis Meyer")
            .iknr("757299999")
            .phone("+490309876543")
            .email("info@praxis.de")
            .address(Country.D, "Berlin", "10623", "Wegelystraße 3")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildHospitalKsnMedicalOrganizationWithFixedValues() {
    val organizationResourceId = "d55c6c01-057b-483d-a1eb-2bd1e182551f";
    val organization =
        MedicalOrganizationBuilder.builder()
            .version(KbvItaForVersion.V1_1_0)
            .setResourceId(organizationResourceId)
            .name("Zahnarztpraxis Meyer")
            .ksn("757299999")
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
    val organization = MedicalOrganizationFaker.medicalPractice().withVersion(version).fake();
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
