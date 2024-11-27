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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBsnr;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerCity;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerEMail;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPhone;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerStreetName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerZipCode;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationFaker.OrganizationFakerType;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
  void shouldFakeHospitalMedicalOrganizationWithIknr() {
    val organization = MedicalOrganizationFaker.hospital().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
    assertTrue(DeBasisNamingSystem.IKNR_SID.match(organization.getIdentifierFirstRep()));
  }

  @Test
  void shouldFakeHospitalMedicalOrganizationWithKsn() {
    val organization = MedicalOrganizationFaker.builder(OrganizationFakerType.HOSPITAL_KSN).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
    assertTrue(DeBasisNamingSystem.STANDORTNUMMER.match(organization.getIdentifierFirstRep()));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldBuildDentalOrganizationForPractitioner(KbvItaForVersion kbvItaForVersion) {
    val practitioner =
        PractitionerFaker.builder().withQualificationType(QualificationType.DENTIST).fake();
    val organization =
        MedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvItaForVersion).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());

    if (kbvItaForVersion == KbvItaForVersion.V1_0_3) {
      assertTrue(
          DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER.match(organization.getIdentifierFirstRep()));
    } else {
      assertTrue(
          DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER_SID.match(
              organization.getIdentifierFirstRep()));
    }
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldBuildOrganizationForPractitioner(KbvItaForVersion kbvItaForVersion) {
    val practitioner =
        PractitionerFaker.builder().withQualificationType(QualificationType.DOCTOR).fake();
    val organization =
        MedicalOrganizationFaker.forPractitioner(practitioner).withVersion(kbvItaForVersion).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());

    assertFalse(
        DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER.match(organization.getIdentifierFirstRep()));
    assertFalse(
        DeBasisNamingSystem.KZVA_ABRECHNUNGSNUMMER_SID.match(organization.getIdentifierFirstRep()));
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
