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

package de.gematik.test.erezept.eml.fhir;

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.validation.DummyValidator;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.Test;

@SuppressWarnings("java:S110")
class ErpFhirFactoryTest {

  @Test
  void shouldReadProvidePrescriptionCorrect() {
    val codec = EpaFhirFactory.create();
    val providePrescAsStringFromFd =
        ResourceLoader.readFileFromResource("fhir/forunittests/provPrescFromFD.json");
    val ressource = codec.decode(providePrescAsStringFromFd);
    assertInstanceOf(EpaOpProvidePrescription.class, ressource);
    val provPresc = (EpaOpProvidePrescription) ressource;
    val medReq = assertDoesNotThrow(provPresc::getEpaMedicationRequest);
    val mOrg = assertDoesNotThrow(provPresc::getEpaOrganisation);
    val med = assertDoesNotThrow(provPresc::getEpaMedication);
    val mPre = assertDoesNotThrow(provPresc::getEpaPrescriptionId);
    val mA = assertDoesNotThrow(provPresc::getEpaAuthoredOn);
  }

  @Test
  void validateWithCustomFhirValidatorShouldWork() {
    val medicationAsString2 =
        ResourceLoader.readFileFromResource("fhir/valid/medication/epaMockResponse.json");

    val codec = EpaFhirFactory.create(new DummyValidator(new FhirContext()));
    assertTrue(codec.isValid(medicationAsString2));
  }

  @Test
  void validateWithDefaultFhirValidatorShouldWork() {
    val medicationAsString =
        ResourceLoader.readFileFromResource(
            "fhir/forunittests/Medication-SumatripanMedication.json");

    val codec = EpaFhirFactory.create();
    val epaMedic = codec.decode(Medication.class, medicationAsString);
    assertTrue(codec.validate(medicationAsString).isSuccessful());
    assertTrue(codec.isValid(codec.encode(epaMedic, EncodingType.XML)));
  }

  @Test
  void validationShouldFailWithManipulatedMedicAndDefaultFhirValidator() {

    val codec = EpaFhirFactory.create();
    val invalidMed =
        ResourceLoader.readFileFromResource(
            "fhir/invalid/medication/Manipulated_Medication-SumatripanMedication.json");
    val epaMedic = codec.decode(Medication.class, invalidMed);
    assertFalse(codec.validate(invalidMed).isSuccessful());
    assertFalse(codec.isValid(codec.encode(epaMedic, EncodingType.XML)));
    assertEquals(3, codec.validate(invalidMed).getMessages().size());
  }
}
