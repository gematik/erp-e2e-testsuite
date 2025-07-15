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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

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
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept als Apotheke zurückgeben")
@Tag("UseCase:Reject")
public class TaskRejectUsecase extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor apoAmFlughafen;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @TestcaseId("ERP_TASK_REJECT_USECASE_01")
  @Test
  @DisplayName("Löschen der Telematik-ID in Task.owner beim FD nach Reject durch Apotheke")
  void withTaskIdAndAccessCodeWillDeleteOwnerId() {
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    val response = apoAmFlughafen.performs(AcceptPrescription.forTheTask(task));
    apoAmFlughafen.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());
    val rejectResp =
        apoAmFlughafen.performs(TaskReject.acceptedTask(response.getExpectedResponse()));

    apoAmFlughafen.attemptsTo(
        Verify.that(rejectResp).withoutBody().hasResponseWith(returnCode(204)).isCorrect());

    val responseOfGetTaskAfterReject =
        apoAmFlughafen.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));
    apoAmFlughafen.attemptsTo(
        Verify.that(responseOfGetTaskAfterReject)
            .withOperationOutcome(ErpAfos.A_24175)
            .hasResponseWith(returnCode(412))
            .isCorrect());
  }
}
