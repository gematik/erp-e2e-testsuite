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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuHealthcareFacilityType;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganizationProfession;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuGetPrescriptionInputBuilderTest extends ErpFhirParsingTest {

  private final EuGetPrescriptionInputBuilder euGetPrescriptionBundleBuilder =
      EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
          .kvnr(KVNR.randomGkv())
          .accessCode(EuAccessCode.random())
          .countryCode(IsoCountryCode.AT)
          .practitionerName("Practitioners Name")
          .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
          .pointOfCare("carePoint")
          .healthcareFacilityType(EuHealthcareFacilityType.getDefault());

  @Test
  void shouldBuildWithValidAccessCode() {
    val euGetPrescription =
        euGetPrescriptionBundleBuilder.accessCode(EuAccessCode.random()).build();

    assertNotNull(euGetPrescription);
    val result = ValidatorUtil.encodeAndValidate(parser, euGetPrescription);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithUncheckedAccessCode() {
    val euGetPrescription =
        euGetPrescriptionBundleBuilder
            .countryCode(IsoCountryCode.DE)
            .accessCode(EuAccessCode.random())
            .build();

    assertNotNull(euGetPrescription);
    val result = ValidatorUtil.encodeAndValidate(parser, euGetPrescription);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldSetCountryCodeCorrect() {
    val euGetPrescription = euGetPrescriptionBundleBuilder.countryCode(IsoCountryCode.FR).build();
    assertEquals(IsoCountryCode.FR, euGetPrescription.getIsoCountyCode());
  }

  @Test
  void shouldSetVersionCorrect() {
    val euGetPrescription =
        euGetPrescriptionBundleBuilder.version(EuVersion.getDefaultVersion()).build();
    assertTrue(euGetPrescription.getMeta().getProfile().get(0).asStringValue().contains("|1.1"));
  }

  @Test
  void shouldFailWhileGetCountryCodeCorrect() {
    val euGetPrescription = euGetPrescriptionBundleBuilder.countryCode(IsoCountryCode.DE).build();
    assertNotEquals(IsoCountryCode.FR, euGetPrescription.getIsoCountyCode());
  }

  @Test
  void shouldSetAccessCodeCorrect() {
    val accessCode = EuAccessCode.random();
    val euGetPrescription = euGetPrescriptionBundleBuilder.accessCode(accessCode).build();

    assertEquals(accessCode.getValue(), euGetPrescription.getAccessCode());
    val result = ValidatorUtil.encodeAndValidate(parser, euGetPrescription);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithRequestTypeDemographics() {
    val builder =
        euGetPrescriptionBundleBuilder
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.DE);
    val prescription = builder.build();
    assertNotNull(prescription);
    assertEquals(EuRequestType.DEMOGRAPHICS, prescription.getEuRequestType());
    val result = ValidatorUtil.encodeAndValidate(parser, prescription);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithRequestTypePrescriptionRetrieval() {
    val prescription =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.PRESCRIPTION_RETRIEVAL)
            .kvnr(KVNR.randomGkv())
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .prescriptionId(PrescriptionId.random())
            .build();

    assertNotNull(prescription);
    val result = ValidatorUtil.encodeAndValidate(parser, prescription);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildWithRequestTypePrescriptionList() {
    val prescription =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.PRESCRIPTION_LIST)
            .kvnr(KVNR.randomGkv())
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .build();
    assertNotNull(prescription);
    assertEquals(EuRequestType.PRESCRIPTION_LIST, prescription.getEuRequestType());
    assertTrue(ValidatorUtil.encodeAndValidate(parser, prescription).isSuccessful());
  }

  @Test
  void shouldSetPointOfCareCorrect() {
    val testsString = "testPointOfCare";
    val prescription = euGetPrescriptionBundleBuilder.pointOfCare(testsString).build();
    assertNotNull(prescription);
    assertEquals(testsString, prescription.getPointOfCare());
    assertTrue(ValidatorUtil.encodeAndValidate(parser, prescription).isSuccessful());
  }

  @Test
  void shouldSetHealthCareFacilityTypeCareCorrect() {
    val hCFT = new EuHealthcareFacilityType("1.2.276.0.76.4.54", "Care Facility");
    val prescription = euGetPrescriptionBundleBuilder.healthcareFacilityType(hCFT).build();
    assertNotNull(prescription);
    assertEquals(
        hCFT.asCoding().getCode(), prescription.getHealthcareFacilityType().asCoding().getCode());
    val res = ValidatorUtil.encodeAndValidate(parser, prescription);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldSetPrescriptionIdCorrect() {
    val prescriptionId = PrescriptionId.random();
    val prescription = euGetPrescriptionBundleBuilder.prescriptionId(prescriptionId).build();
    assertNotNull(prescription);
    assertEquals(prescriptionId, prescription.getFirstPrescriptionId());
    val res = ValidatorUtil.encodeAndValidate(parser, prescription);
    assertTrue(res.isSuccessful());
  }

  @Test
  void shouldSetListOfPrescriptionIdsCorrect() {
    val prescIds =
        List.of(PrescriptionId.random(), PrescriptionId.random(), PrescriptionId.random());
    val prescription = euGetPrescriptionBundleBuilder.prescriptionIds(prescIds).build();
    assertNotNull(prescription);
    assertEquals(prescIds.get(1), prescription.getPrescriptionIds().get(1));
    assertTrue(ValidatorUtil.encodeAndValidate(parser, prescription).isSuccessful());
  }
}
