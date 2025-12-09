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

package de.gematik.test.erezept.client.usecases.eu;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.eu.*;
import de.gematik.test.erezept.fhir.r4.eu.*;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EuCloseInputPostCommandTest {

  static EuCloseOperationInput euCloseOperationInput;

  @BeforeAll
  static void setUp() {
    EuMedication euMedication =
        EuMedicationBuilder.builder().pzn(PZN.from("pzn-Code"), "Test Name").build();
    EuMedicationDispense medicationDispense =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId("169.123.456.789.122")
            .withKvnr(KVNR.random())
            .withMedication(euMedication)
            .fake();
    val euGetPrescriptionBundle =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
            .kvnr(KVNR.randomGkv())
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .build();

    euCloseOperationInput =
        EuCloseOperationInputBuilder.builder(medicationDispense, euMedication)
            .requestDataFromGetPrescriptionInput(euGetPrescriptionBundle)
            .organization(EuOrganizationFaker.faker().fake())
            .practitioner(EuPractitionerBuilder.buildSimplePractitioner())
            .practitionerRole(EuPractitionerRoleBuilder.getSimplePractitionerRole())
            .build();
  }

  @Test
  void shouldBuildCommandCorrectly() {
    val command = new EuCloseInputPostCommand(euCloseOperationInput);
    val body = command.getRequestBody().orElseThrow();
    assertFalse(body.isEmpty());
    assertInstanceOf(EuCloseOperationInput.class, body);
  }

  @Test
  void shouldBuildCommandCorrectlyWithRequestLocatorCheck() {
    val command = new EuCloseInputPostCommand(euCloseOperationInput);
    assertEquals(de.gematik.bbriccs.rest.HttpRequestMethod.POST, command.getMethod());
    assertEquals("/Task/169.123.456.789.122/$eu-close", command.getRequestLocator());
    assertEquals(
        "https://erp.zentral.erp.splitdns.ti-dienste.de/Task/169.123.456.789.122/$eu-close",
        command.getFullUrl("https://erp.zentral.erp.splitdns.ti-dienste.de"));
  }
}
