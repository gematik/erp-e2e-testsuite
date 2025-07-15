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

import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker.OrganizationFakerType;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvMedicalOrganizationFakerTest extends ErpFhirParsingTest {

  @Test
  void builderFakerMedicalOrganizationWithName() {
    val organization = KbvMedicalOrganizationFaker.builder().withName(fakerName()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithBsnr() {
    val organization = KbvMedicalOrganizationFaker.builder().withBsnr(fakerBsnr()).fake();
    val organization2 = KbvMedicalOrganizationFaker.builder().withBsnr(BSNR.random()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    val result2 = ValidatorUtil.encodeAndValidate(parser, organization2);
    assertTrue(result.isSuccessful());
    assertTrue(result2.isSuccessful());
  }

  @Test
  void shouldFakeHospitalMedicalOrganizationWithIknr() {
    val organization = KbvMedicalOrganizationFaker.hospital().fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
    assertTrue(DeBasisProfilNamingSystem.IKNR_SID.matches(organization.getIdentifierFirstRep()));
  }

  @Test
  void shouldFakeHospitalMedicalOrganizationWithKsn() {
    val organization =
        KbvMedicalOrganizationFaker.builder(OrganizationFakerType.HOSPITAL_KSN).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
    assertTrue(
        DeBasisProfilNamingSystem.STANDORTNUMMER.matches(organization.getIdentifierFirstRep()));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldBuildDentalOrganizationForPractitioner(KbvItaForVersion kbvItaForVersion) {
    val practitioner =
        KbvPractitionerFaker.builder()
            .withVersion(kbvItaForVersion)
            .withQualificationType(QualificationType.DENTIST)
            .fake();
    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner)
            .withVersion(kbvItaForVersion)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());

    assertTrue(
        DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID.matches(
            organization.getIdentifierFirstRep()));
  }

  @ParameterizedTest
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldBuildOrganizationForPractitioner(KbvItaForVersion kbvItaForVersion) {
    val practitioner =
        KbvPractitionerFaker.builder()
            .withVersion(kbvItaForVersion)
            .withQualificationType(QualificationType.DOCTOR)
            .fake();
    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner)
            .withVersion(kbvItaForVersion)
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());

    assertFalse(
        DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER.matches(
            organization.getIdentifierFirstRep()));
    assertFalse(
        DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID.matches(
            organization.getIdentifierFirstRep()));
  }

  @Test
  void builderFakerMedicalOrganizationWithPhone() {
    val organization = KbvMedicalOrganizationFaker.builder().withPhoneNumber(fakerPhone()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithEmail() {
    val organization = KbvMedicalOrganizationFaker.builder().withEmail(fakerEMail()).fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }

  @Test
  void builderFakerMedicalOrganizationWithAddress() {
    val organization =
        KbvMedicalOrganizationFaker.builder()
            .withAddress(fakerCity(), fakerZipCode(), fakerStreetName())
            .fake();
    val result = ValidatorUtil.encodeAndValidate(parser, organization);
    assertTrue(result.isSuccessful());
  }
}
