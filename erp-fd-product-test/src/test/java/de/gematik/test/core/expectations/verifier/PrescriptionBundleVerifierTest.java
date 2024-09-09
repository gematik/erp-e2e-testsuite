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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PZN;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrescriptionBundleVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(PrescriptionBundleVerifier.class));
  }

  @Test
  void shouldPassOnValidAccessCode() {
    val erxTask = new ErxTask();
    erxTask.addIdentifier(AccessCode.random().asIdentifier());
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    step.apply(prescriptionBundle);
  }

  @Test
  void shouldFailOnMissingAccessCode() {
    val erxTask = new ErxTask();
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }

  @Test
  void shouldFailOnInvalidAccessCode() {
    val erxTask = new ErxTask();
    erxTask.addIdentifier(new AccessCode("affe").asIdentifier());
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getTask()).thenReturn(erxTask);

    val step = bundleHasValidAccessCode();
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }

  @Test
  void shouldPassOnCorrectAccident() {
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);

    val date = new Date();
    val accident1 = AccidentExtension.accident(date);
    val accident2 = AccidentExtension.accident(date);

    when(medicationRequest.getAccident()).thenReturn(Optional.of(accident1));

    val step = bundleContainsAccident(accident2);
    step.apply(prescriptionBundle);
  }

  @Test
  void shouldFailOnMissingAccident() {
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.getAccident()).thenReturn(Optional.empty());

    val step = bundleContainsAccident(AccidentExtension.accident());
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }

  @Test
  void shouldFindPznInBundle() {
    val medication = mock(KbvErpMedication.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    val teststring = "12345678";
    val testPzn = PZN.from(teststring);
    val verifyPzn = PZN.from(teststring);
    when(medication.getPznOptional()).thenReturn(Optional.of(testPzn));

    val step = bundleContainsPzn(verifyPzn, ErpAfos.A_24034);
    step.apply(prescriptionBundle);
  }

  @Test
  void shouldNotFindPznInBundle() {
    val medication = mock(KbvErpMedication.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    val teststring = "12345678";
    val verifyPzn = PZN.from(teststring);

    val step = bundleContainsPzn(verifyPzn, ErpAfos.A_24034);
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }

  @Test
  void shouldFindNameInBundle() {
    val medication = mock(KbvErpMedication.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medIngrComp = mock(Medication.MedicationIngredientComponent.class);
    val codeConc = mock(CodeableConcept.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getIngredientFirstRep()).thenReturn(medIngrComp);
    when(medIngrComp.getItemCodeableConcept()).thenReturn(codeConc);

    val teststring = "Bauschaum";
    val testText = teststring;
    val verifyText = teststring;
    when(codeConc.getText()).thenReturn((testText));

    val step = bundleContainsNameInMedicationCompound(verifyText, ErpAfos.A_24034);
    step.apply(prescriptionBundle);
  }

  @Test
  void shouldNotFindNameInBundle() {
    val medication = mock(KbvErpMedication.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medIngrComp = mock(Medication.MedicationIngredientComponent.class);
    val codeConc = mock(CodeableConcept.class);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getIngredientFirstRep()).thenReturn(medIngrComp);
    when(medIngrComp.getItemCodeableConcept()).thenReturn(codeConc);

    val verifyText = "Bauschaum";
    val step = bundleContainsNameInMedicationCompound(verifyText, ErpAfos.A_24034);
    assertThrows(AssertionError.class, () -> step.apply(prescriptionBundle));
  }
}
