/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.integration.idp;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadReadyTask;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.PatientActor;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Prüfung Zugriff mit ungültigem IDP-Token")
@Tag("INVALIDIDP")
public class InvalidIdpIT extends ErpTest {

  @Actor(name = "Fridolin Straßer")
  private PatientActor patient;

  @TestcaseId("ERP_INVALID_IDP_TOKEN_01")
  @DisplayName("Der Zugriff auf den FD mit ungültigem IDP-Token wird abgelehnt")
  @Test
  void shouldFailRequestWithInvalidIdpToken() {

    val manipPatient = new PatientActor(patient.getName());
    config.equipAsPatientWithInvalidIdpToken(manipPatient); // manipulate p2 != Fridolin

    val response = manipPatient.performs(DownloadReadyTask.asPatient(List.of()));

    manipPatient.attemptsTo(
        Verify.that(response).withOperationOutcome().hasResponseWith(returnCode(401)).isCorrect());
  }
}
