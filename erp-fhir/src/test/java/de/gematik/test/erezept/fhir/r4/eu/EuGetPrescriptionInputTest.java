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

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.builder.eu.EuGetPrescriptionInputBuilder;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuGetPrescriptionInputTest {
  private EuGetPrescriptionInputBuilder euGetPrescriptionBundleBuilder =
      EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
          .kvnr(KVNR.randomGkv())
          .accessCode(EuAccessCode.random())
          .countryCode(IsoCountryCode.AT)
          .practitionerName("Practitioners Name")
          .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
          .pointOfCare("carePoint")
          .healthcareFacilityType(EuHealthcareFacilityType.getDefault());

  @Test
  void shouldGetCountryCodeCorrect() {
    val euGetPrescription = euGetPrescriptionBundleBuilder.countryCode(IsoCountryCode.FR).build();
    assertEquals(IsoCountryCode.FR, euGetPrescription.getIsoCountyCode());
  }

  @Test
  void shouldFailWhileGetCountryCode() {
    val euGetPrescription = euGetPrescriptionBundleBuilder.countryCode(IsoCountryCode.DE).build();
    assertNotEquals(IsoCountryCode.FR, euGetPrescription.getIsoCountyCode());
  }

  @Test
  void shouldGetAccessCodeCorrect() {
    val accessCode = EuAccessCode.random();
    val euGetPrescription = euGetPrescriptionBundleBuilder.accessCode(accessCode).build();
    assertEquals(accessCode.getValue(), euGetPrescription.getAccessCode());
  }

  @Test
  void shouldFailWhileGetAccessCode() {
    val accessCode = EuAccessCode.from("123ABC");
    val euGetPrescription = euGetPrescriptionBundleBuilder.accessCode(accessCode).build();
    assertNotEquals("1a2b3c", euGetPrescription.getIsoCountyCode());
  }

  @Test
  void shouldGetRequestTypeCorrect() {
    val euGetPrescription = euGetPrescriptionBundleBuilder.build();
    assertEquals(EuRequestType.DEMOGRAPHICS, euGetPrescription.getEuRequestType());
  }

  @Test
  void shouldGetPractitionerNameCorrect() {
    val practitionerName = "Dr. John Doe";
    val euGetPrescription =
        euGetPrescriptionBundleBuilder.practitionerName(practitionerName).build();
    assertEquals(practitionerName, euGetPrescription.getPractitionerName());
  }

  @Test
  void shouldGetKvnrCorrect() {
    val kvnr = KVNR.randomGkv();
    val euGetPrescription = euGetPrescriptionBundleBuilder.kvnr(kvnr).build();
    assertEquals(kvnr, euGetPrescription.getKvnr());
  }

  @Test
  void shouldGetPractitionerRoleCorrect() {
    val prectRole = new EuOrganizationProfession("System", "code", "display");
    val euGetPrescription = euGetPrescriptionBundleBuilder.practitionerRole(prectRole).build();
    val pracRoleFromEuObject = euGetPrescription.getPractitionerRole();
    assertEquals(prectRole.getCode(), pracRoleFromEuObject.getCode());
    assertEquals(prectRole.getCanonicalUrl(), pracRoleFromEuObject.getCanonicalUrl());
    assertEquals(prectRole.getDisplay(), pracRoleFromEuObject.getDisplay());
  }
}
