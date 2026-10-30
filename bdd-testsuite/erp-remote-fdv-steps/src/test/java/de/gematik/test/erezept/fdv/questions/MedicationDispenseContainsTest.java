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

package de.gematik.test.erezept.fdv.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.erezept.remotefdv.api.model.MedicationDispense;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.fdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import java.util.Date;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MedicationDispenseContainsTest {
  protected String userName;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheMockRemoteFdVClient = mock(UseTheRemoteFdVClient.class);

    givenThat(theAppUser).can(useTheMockRemoteFdVClient);
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldGetMedicationDispense() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val prescriptionId = PrescriptionId.random();

    val receivedDrugs = actor.abilityTo(ReceiveDispensedDrugs.class);
    receivedDrugs.append(prescriptionId, new Date().toInstant());

    val response = new FdVResponse<MedicationDispense>();
    val medDispense = new MedicationDispense();
    medDispense.setPrescriptionId(prescriptionId.getValue());
    response.setResourcesList(List.of(medDispense));

    when(remoteFdV.sendRequest(PatientRequests.getMedicationDispense(any()))).thenReturn(response);
    assertTrue(
        actor.asksFor(
            MedicationDispenseContains.forPatient()
                .andPrescription("letzte")
                .numberOfMedicationDispenses(1)));
    assertTrue(
        actor.asksFor(
            MedicationDispenseContains.forPatient()
                .andPrescription(DequeStrategy.FIFO)
                .numberOfMedicationDispenses(1)));
  }
}
