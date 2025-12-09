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

package de.gematik.test.erezept.integration.scenarios;

import static de.gematik.test.core.expectations.verifier.AcceptBundleVerifier.isInProgressStatus;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.prescriptionHasStatus;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept mehrfach akzeptieren, zurückgeben und anschließend löschen")
@Tag("Ticket:B_FD-1389")
class MultiAcceptRejectAbortIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @Test
  @TestcaseId("ERP_B_FD_1389")
  @DisplayName("Löschen eines E-Rezepts nach mehrfachem $accept und $reject")
  void performMultipleTimeAcceptReject() {
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .isCorrect());
    val task = activation.getExpectedResponse();

    // precondition: $accept and $reject the task multiple times
    for (var i = 0; i < 20; i++) {
      // Step 1: accept the prescription
      val acceptance = pharmacy.performs(AcceptPrescription.forTheTask(task));
      pharmacy.attemptsTo(
          Verify.that(acceptance)
              .withExpectedType(ErpAfos.A_19166)
              .hasResponseWith(returnCode(200))
              .and(isInProgressStatus())
              .isCorrect());

      // step 2: patient checks the status of the prescription
      val inProgressTask = patient.asksFor(TheTask.fromBackend(task));
      patient.attemptsTo(
          Verify.that(inProgressTask)
              .withExpectedType()
              .hasResponseWith(returnCodeIsBetween(200, 210))
              .and(prescriptionHasStatus(Task.TaskStatus.INPROGRESS, ErpAfos.A_19168))
              .isCorrect());

      // step 3: reject the prescription
      val rejectResp = pharmacy.performs(TaskReject.acceptedTask(acceptance.getExpectedResponse()));
      pharmacy.attemptsTo(
          Verify.that(rejectResp).withoutBody().hasResponseWith(returnCode(204)).isCorrect());

      // step 4: patient checks the status of the prescription
      val readyTask = patient.asksFor(TheTask.fromBackend(task));
      patient.attemptsTo(
          Verify.that(readyTask)
              .withExpectedType()
              .hasResponseWith(returnCodeIsBetween(200, 210))
              .and(prescriptionHasStatus(Task.TaskStatus.READY, ErpAfos.A_19168))
              .isCorrect());
    }

    // make sure the patient is able to delete the prescription after multiple $accept/$reject
    val abortResp = patient.performs(TaskAbort.asPatient(task));
    patient.attemptsTo(
        Verify.that(abortResp).withExpectedType().hasResponseWith(returnCode(204)).isCorrect());
  }
}
