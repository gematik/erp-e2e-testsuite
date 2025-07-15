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

package de.gematik.test.erezept.eml.fhir;

import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.context.FhirContext;
import de.gematik.bbriccs.fhir.validation.DummyValidator;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import lombok.val;
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
    assertDoesNotThrow(provPresc::getEpaMedicationRequest);
    assertDoesNotThrow(provPresc::getEpaOrganisation);
    assertDoesNotThrow(provPresc::getEpaMedication);
    assertDoesNotThrow(provPresc::getEpaPrescriptionId);
    assertDoesNotThrow(provPresc::getEpaAuthoredOn);
  }

  @Test
  void validateWithCustomFhirValidatorShouldWork() {
    val medicationAsString2 =
        ResourceLoader.readFileFromResource("fhir/valid/parameters/epaMockResponse.json");

    val codec = EpaFhirFactory.create(new DummyValidator(FhirContext.forR4()));
    assertTrue(codec.isValid(medicationAsString2));
  }

  @Test
  void validateWithDefaultFhirValidatorShouldWork() {
    val medicationAsString =
        ResourceLoader.readFileFromResource(
            "fhir/forunittests/Medication-SumatripanMedication.json");

    val codec = EpaFhirFactory.create();
    val validationResult = codec.validate(medicationAsString);
    validationResult.getMessages().forEach(System.out::println);
    assertTrue(validationResult.isSuccessful());
  }

  @Test
  void validationShouldFailWithManipulatedMedicAndDefaultFhirValidator() {
    val codec = EpaFhirFactory.create();
    val invalidMed =
        ResourceLoader.readFileFromResource(
            "fhir/invalid/medication/Manipulated_Medication-SumatripanMedication.json");
    assertFalse(codec.validate(invalidMed).isSuccessful());
  }
}
