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

package de.gematik.test.erezept.remotefdv.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.config.dto.remotefdv.RemoteFdVActorConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.remotefdv.abilities.UseTheRemoteFdVClient;
import de.gematik.test.erezept.remotefdv.client.FdVResponse;
import de.gematik.test.erezept.remotefdv.client.PatientRequests;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.model.LoginSuccess;

class SetUpRemoteFdVTest {
  private String userName;
  private RemoteFdVActorConfiguration actorConfig;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(new Cast() {});
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    val useTheMockRemoteFdVClient = mock(UseTheRemoteFdVClient.class);

    actorConfig = new RemoteFdVActorConfiguration();
    actorConfig.setKvnr(KVNR.random().getValue());
    val response = new FdVResponse<String>();
    response.setExpectedResource(List.of("Success"));

    when(useTheMockRemoteFdVClient.sendRequest(PatientRequests.startFdV())).thenReturn(response);
    givenThat(theAppUser).can(useTheMockRemoteFdVClient);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldSuccessfullyLogin() {
    val actor = OnStage.theActorCalled(userName);
    val remoteFdV = actor.abilityTo(UseTheRemoteFdVClient.class);
    val successLogin = new LoginSuccess();
    val response = new FdVResponse<LoginSuccess>();

    response.setExpectedResource(List.of(successLogin));
    when(remoteFdV.sendRequest(PatientRequests.loginWithKvnr(any()))).thenReturn(response);

    val setUpRemoteFdV = SetUpRemoteFdV.forUser(actorConfig);
    assertDoesNotThrow(() -> setUpRemoteFdV.performAs(actor));
  }
}
