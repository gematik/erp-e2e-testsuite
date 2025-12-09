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
import de.gematik.test.core.expectations.verifier.TaskVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.ReadDiGATaskAsAcceptBundle;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.KtrActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.questions.ResponseOfGetTaskById;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.task.IssueDiGAPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("EVGDA E-Rezept abrufen")
@Tag("GetEvgdaRezept")
class TaskGetDiGAUseCaseIT extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private static PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "AOK Bremen")
  private KtrActor ktr;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @TestcaseId("ERP_DIGA_RETRIEVE_01")
  @Test
  @DisplayName(
      "Als Kostenträger rufe ich ein zuvor akzeptiertes EVGDA E-Rezept ab, um das Secret erneut zu"
          + " erhalten")
  void shouldRetrieveDiGAPrescriptionAsKostentraeger() {
    doctor.attemptsTo(IssueDiGAPrescription.forPatient(patient));

    val dmc = SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).getDmcs().getFirst();

    val taskId = dmc.getTaskId();
    val accessCode = dmc.getAccessCode();

    val acceptInteraction = ktr.performs(AcceptPrescription.with(taskId, accessCode));
    ktr.attemptsTo(Verify.that(acceptInteraction).isFromExpectedType());

    val acceptedPrescription = acceptInteraction.getExpectedResponse();
    SafeAbility.getAbility(ktr, ManagePharmacyPrescriptions.class)
        .appendAcceptedPrescription(acceptedPrescription);

    val response = ktr.asksFor(ResponseOfGetTaskById.asKtr(DequeStrategy.FIFO));

    ktr.attemptsTo(
        Verify.that(new ErpInteraction<>(response))
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(TaskVerifier.hasSecret())
            .isCorrect());
  }

  @TestcaseId("ERP_DIGA_RETRIEVE_02")
  @Test
  @DisplayName("Als Apotheke rufe ich ein EVGDA E-Rezept ab und erhalte keinen Zugriff")
  void shouldRejectEvGdaRetrievalByPharmacy() {
    doctor.attemptsTo(IssueDiGAPrescription.forPatient(patient));

    val dmc = SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).getDmcs().getFirst();

    val response = pharmacy.asksFor(ReadDiGATaskAsAcceptBundle.fromDmc(dmc));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_24177)
            .hasResponseWith(returnCode(412))
            .isCorrect());
  }

  @TestcaseId("ERP_DIGA_RETRIEVE_03")
  @Test
  @DisplayName(
      "Als Kostenträger rufe ich ein EVGDA E-Rezept ohne dieses vorher akzeptiert zu haben")
  void shouldRejectEvGdaRetrievalWithoutAcceptOrWrongKostentraeger() {

    doctor.attemptsTo(IssueDiGAPrescription.forPatient(patient));

    val dmc = SafeAbility.getAbility(patient, ManageDataMatrixCodes.class).getDmcs().getFirst();

    val response = ktr.asksFor(ReadDiGATaskAsAcceptBundle.fromDmc(dmc));

    ktr.attemptsTo(
        Verify.that((response))
            .withOperationOutcome(ErpAfos.A_24177)
            .hasResponseWith(returnCode(412))
            .isCorrect());
  }
}
