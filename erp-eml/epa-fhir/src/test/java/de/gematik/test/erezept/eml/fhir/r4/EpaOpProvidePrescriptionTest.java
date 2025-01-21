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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.hl7.fhir.r4.model.MedicationRequest.MedicationRequestStatus.ACTIVE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class EpaOpProvidePrescriptionTest {

  private static EpaOpProvidePrescription epaOpProvidePrescription;
  private static final FhirCodec fhir = EpaFhirFactory.create();

  @BeforeAll
  static void setup() {

    val BASE_PATH =
        "fhir/valid/medication/Parameters-example-epa-op-provide-prescription-erp-input-parameters-2.json";
    val content = ResourceLoader.readFileFromResource(BASE_PATH);
    epaOpProvidePrescription = fhir.decode(EpaOpProvidePrescription.class, content);
  }

  @Test
  void shouldGetEpaPrescriptionId() {
    assertEquals(
        PrescriptionId.from("160.153.303.257.459"),
        epaOpProvidePrescription.getEpaPrescriptionId());
  }

  @Disabled("does not work properly") // todo reactivate and check why the two errors are not fails:
  @Test
  // SingleValidationMessage[col=3801,row=1,locationString=Parameters.parameter[0].part[3].resource/*Medication/acdb370c-d1c5-4baf-8dbb-a875dd6fe602*/.contained[0]/*Medication/2cb4d470-d851-4a10-911c-b4624ce5d438*/,message=2 profiles found for contained resource. More than one is not supported at this time. (Type Medication: https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pharmaceutical-product, https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-pzn-ingredient),BUNDLE_BUNDLE_ENTRY_MULTIPLE_PROFILES,severity=error]
  // SingleValidationMessage[col=3770,row=1,locationString=Parameters.parameter[0].part[3].resource/*Medication/acdb370c-d1c5-4baf-8dbb-a875dd6fe602*/.contained[0]/*Medication/2cb4d470-d851-4a10-911c-b4624ce5d438*/.code.coding[0],message=This module has no support for code system http://fhir.de/CodeSystem/ifa/pzn for 'http://fhir.de/CodeSystem/ifa/pzn#49669571',Terminology_PassThrough_TX_Message,severity=warning]
  void shuoldValidateAsError() {
    val content =
        ResourceLoader.readFileFromResource(
            "fhir/invalid/provideprescription/InvalidProvidePrescription.json");
    val res = fhir.validate(content);
    assertFalse(res.isSuccessful());
  }

  @Test
  void shouldGetEpaAuthoredOn() {
    assertEquals(
        new Date(2025 - 1900, Calendar.JANUARY, 22), epaOpProvidePrescription.getEpaAuthoredOn());
  }

  @Test
  void shouldGetEpaMedicationRequest() {
    assertNotNull(epaOpProvidePrescription.getEpaMedicationRequest());
    assertEquals(ACTIVE, epaOpProvidePrescription.getEpaMedicationRequest().getStatus());
  }

  @Test
  void shouldGetEpaMedication() {
    val medication = epaOpProvidePrescription.getEpaMedication();
    assertNotNull(medication);
    assertEquals(
        Optional.of(PZN.from("10019621")), epaOpProvidePrescription.getEpaMedication().getPzn());
  }

  @Test
  void shouldGetEpaOrganisation() {
    val organization = epaOpProvidePrescription.getEpaOrganisation();
    assertEquals(TelematikID.from("9-2.58.00000040"), organization.getTelematikId());
  }

  @Test
  void shouldGetEpaPractitioner() {
    val practitioner = epaOpProvidePrescription.getEpaPractitioner();
    assertEquals(TelematikID.from("1-1.58.00000040"), practitioner.getTelematikId());
  }
}
