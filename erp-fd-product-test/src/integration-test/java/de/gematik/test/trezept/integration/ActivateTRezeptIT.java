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

package de.gematik.test.trezept.integration;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Activate T-Rezept")
@Tag("TRezept")
class ActivateTRezeptIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Peter Kleinschmidt")
  private DoctorActor psychotherapist;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Test
  @TestcaseId("ERP_ACTIVATE_TREZEPT_01")
  @DisplayName("Aktivieren eines T-Rezepts mit gültigem AccessCode")
  void activateTRezept() {
    val kbvBundleNew =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        doctor.performs(
            IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()));

    doctor.attemptsTo(
        Verify.that(task).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());
  }

  @Test
  @TestcaseId("ERP_ACTIVATE_TREZEPT_02")
  @DisplayName("Aktivieren eines T-Rezepts mit ungültiger Medication")
  void activateTRezeptWithInvalidMedication() {
    val kbvBundleNew =
        KbvErpBundleFaker.builder()
            .withMedication(
                KbvErpMedicationPZNFaker.builder()
                    .withCategory(MedicationCategory.C_00)
                    .withVaccine(false)
                    .fake());

    val task =
        doctor.performs(
            IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()));

    doctor.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                OperationOutcomeVerifier.hasAnyOfDetailsText(
                    ErpAfos.A_27813,
                    "Für diesen Workflowtypen sind nur T-Rezept Verordnungen zulässig"))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_ACTIVATE_TREZEPT_03")
  @DisplayName("Aktivieren eines T-Rezepts mit nicht berechtigter Rolle")
  void activateTRezeptWithUnauthorizedRole() {
    val kbvBundleNew =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    val task =
        psychotherapist.performs(
            IssuePrescription.forPatient(sina).asTPrescription(kbvBundleNew.toBuilder()));

    psychotherapist.attemptsTo(
        Verify.that(task)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400, ErpAfos.A_27812))
            .isCorrect());
  }
}
