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
 */

package de.gematik.test.erezept.integration;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeBetween;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actions.TheTask;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Hello World Testscenario")
@Tag("Demo")
@Disabled("For Demonstration purpose only!")
class HelloWorldFeature extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @Test
  @TestcaseId("ERP_RC_HELLOWORLD_01")
  @DisplayName("Task mit FlowType 160 erstellen")
  @Tag("Positivtest")
  @Disabled("For Demonstration purpose only!")
  void createTask() {
    val creation = doctor.performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_160));

    doctor.attemptsTo(
        Verify.that(creation)
            .withExpectedType(ErpAfos.A_19018)
            .hasResponseWith(returnCode(201))
            .isCorrect());
  }

  @Test
  @TestcaseId("ERP_RC_HELLOWORLD_05")
  @DisplayName("Screenplay Beispiel")
  @Tag("Negativtest")
  @Disabled("For Demonstration purpose only!")
  void testScreenplay() {
    // no erp-client for now; simply mock some responses
    val taskGetInteraction = sina.asksFor(TheTask.withId(TaskId.from("123123")));

    sina.attemptsTo(
        Verify.that(taskGetInteraction)
            .withOperationOutcome()
            .hasResponseWith(returnCodeBetween(400, 410))
            .isCorrect());
  }
}
