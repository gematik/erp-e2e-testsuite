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
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Create T-Rezept")
@Tag("TRezept")
class CreateTRezeptIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Test
  @TestcaseId("ERP_CREATE_TREZEPT_01")
  @DisplayName("Erstellung eines T-Rezepts")
  void shouldCreateTRezeptTask() {

    val interaction = doctor.performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_166));

    doctor.attemptsTo(
        Verify.that(interaction).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @Test
  @TestcaseId("ERP_CREATE_TREZEPT_02")
  @DisplayName("Erstellung eines T-Rezepts durch eine unbefugte Rolle")
  void shouldRejectTRezeptTaskForUnauthorizedRole() {

    val interaction = patient.performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_166));

    patient.attemptsTo(
        Verify.that(interaction)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403, ErpAfos.A_19018_01))
            .isCorrect());
  }
}
