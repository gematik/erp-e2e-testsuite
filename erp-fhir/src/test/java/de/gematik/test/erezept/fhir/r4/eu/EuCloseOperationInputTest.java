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
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.eu.*;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.EuAccessCode;
import de.gematik.test.erezept.fhir.valuesets.IsoCountryCode;
import de.gematik.test.erezept.fhir.valuesets.eu.EuRequestType;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EuCloseOperationInputTest extends ErpFhirParsingTest {

  static EuCloseOperationInput closeInput;

  @BeforeAll
  static void setUp() {
    val kvnr = KVNR.random();
    EuMedication euMedication =
        EuMedicationBuilder.builder().pzn(PZN.from("pzn-Code"), "Test Name").build();
    EuMedicationDispense medicationDispense =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId("169.123.456.789.122")
            .withKvnr(kvnr)
            .withMedication(euMedication)
            .fake();
    EuMedication euMedication2 =
        EuMedicationBuilder.builder().pzn(PZN.from("pzn-Code2"), "Test Name2").build();
    EuMedicationDispense medicationDispense2 =
        EuMedicationDispenseFaker.builder()
            .withPrescriptionId("169.222.222.222.222")
            .withKvnr(kvnr)
            .withMedication(euMedication)
            .fake();

    val euGetPrescriptionBundle =
        EuGetPrescriptionInputBuilder.forRequestType(EuRequestType.DEMOGRAPHICS)
            .kvnr(kvnr)
            .accessCode(EuAccessCode.random())
            .countryCode(IsoCountryCode.AT)
            .practitionerName("Practitioners Name")
            .practitionerRole(EuOrganizationProfession.getDefaultPharmacist())
            .pointOfCare("carePoint")
            .healthcareFacilityType(EuHealthcareFacilityType.getDefault())
            .build();

    closeInput =
        EuCloseOperationInputBuilder.builder(medicationDispense, euMedication)
            .withOptionalRxDispensation(medicationDispense2, euMedication2)
            .requestDataFromGetPrescriptionInput(euGetPrescriptionBundle)
            .organization(EuOrganizationFaker.faker().fake())
            .practitioner(EuPractitionerBuilder.buildSimplePractitioner())
            .practitionerRole(EuPractitionerRoleBuilder.getSimplePractitionerRole())
            .build();
  }

  @Test
  void shouldGetPrescriptionId() {

    val prescriptionId = closeInput.getPrescriptionId();
    assertEquals("169.123.456.789.122", prescriptionId.orElseThrow().getValue());
    assertNotEquals("169.123.456.000.000", prescriptionId.orElseThrow().getValue());
  }

  @Test
  void shouldGetRxDispense() {
    val disp = closeInput.getFirstRxDispension();
    assertNotNull(disp);
  }

  @Test
  void shouldGetAllRxDispenses() {
    val disp = closeInput.getRxDispensions();
    assertEquals(2, disp.size());
  }

  @Test
  void shouldGetRequestData() {
    val res = closeInput.getRequestData();
    assertTrue(res.isPresent());
  }

  @Test
  void shouldGeOrganizationData() {
    val res = closeInput.getOrganizationData();
    assertTrue(res.hasId());
  }

  @Test
  void shouldGetPractitionerRoleData() {
    val res = closeInput.getPractitionerRoleData();
    assertTrue(res.hasId());
  }

  @Test
  void shouldGetPractitionerData() {
    val res = closeInput.getPractitionerData();
    assertTrue(res.hasId());
  }
}
