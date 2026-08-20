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

package de.gematik.test.erezept.tasks;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.websocket.WebSocketClient;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Subscription;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class RegisterNewSubscriptionTest {

  private static final String SUBSCRIPTION_SERVICE_URL = "wss://subscription.example.com";

  private Actor actor;
  private UseSubscriptionService useSubscriptionService;
  private UseSMCB useSMCB;
  private UseTheErpClient erpClient;

  private MockedStatic<SafeAbility> safeAbilityMock;

  @BeforeEach
  void setUp() {
    actor = mock(Actor.class);
    useSubscriptionService = mock(UseSubscriptionService.class);
    useSMCB = mock(UseSMCB.class);
    erpClient = mock(UseTheErpClient.class);

    safeAbilityMock = mockStatic(SafeAbility.class);
    safeAbilityMock
        .when(() -> SafeAbility.getAbility(actor, UseSubscriptionService.class))
        .thenReturn(useSubscriptionService);
    safeAbilityMock.when(() -> SafeAbility.getAbility(actor, UseSMCB.class)).thenReturn(useSMCB);
    safeAbilityMock
        .when(() -> SafeAbility.getAbility(actor, UseTheErpClient.class))
        .thenReturn(erpClient);

    when(useSMCB.getTelematikID()).thenReturn("3-SMC-B-Testkarte-883110000116873");
  }

  @AfterEach
  void tearDown() {
    safeAbilityMock.close();
  }

  private Subscription buildSubscriptionResource(String id, String bearerToken) {
    val subscription = new Subscription();
    subscription.setId(new IdType("Subscription", id));

    val channel = new Subscription.SubscriptionChannelComponent();
    channel.addHeader("Authorization: Bearer " + bearerToken);
    subscription.setChannel(channel);

    return subscription;
  }

  @Test
  void shouldReturnTrueWhenWebSocketConnectsAndBindsSuccessfully() {
    val subscriptionId = "12345";
    val bearerToken = "raw-token";
    val subscription = buildSubscriptionResource(subscriptionId, bearerToken);

    val response = mock(FhirBResponse.class);
    when(response.getExpectedResource()).thenReturn(subscription);
    when(erpClient.request(any())).thenReturn(response);

    when(useSubscriptionService.getSubscriptionId()).thenReturn(subscriptionId);
    when(useSubscriptionService.getAuthorization()).thenReturn("Bearer " + bearerToken);

    try (MockedConstruction<WebSocketClient> wsConstruction =
        mockConstruction(
            WebSocketClient.class,
            (mockWs, context) -> {
              when(mockWs.connectBlocking(30, TimeUnit.SECONDS)).thenReturn(true);

              val bindLatch = mock(CountDownLatch.class);
              when(mockWs.bind(subscriptionId)).thenReturn(bindLatch);

              when(mockWs.isBound()).thenReturn(true);
            })) {

      val question = RegisterNewSubscription.forNewCommunications(SUBSCRIPTION_SERVICE_URL);
      val result = question.answeredBy(actor);

      assertTrue(result);
      assertEquals(1, wsConstruction.constructed().size());

      verify(useSubscriptionService).setSubscriptionId(subscriptionId);
      verify(useSubscriptionService).setAuthorization("Bearer " + bearerToken);
    }
  }

  @Test
  void shouldReturnFalseWhenWebSocketConnectionTimesOut() {
    val subscriptionId = "12345";
    val subscription = buildSubscriptionResource(subscriptionId, "raw-token");

    val response = mock(FhirBResponse.class);
    when(response.getExpectedResource()).thenReturn(subscription);
    when(erpClient.request(any())).thenReturn(response);

    try (MockedConstruction<WebSocketClient> wsConstruction =
        mockConstruction(
            WebSocketClient.class,
            (mockWs, context) ->
                when(mockWs.connectBlocking(30, TimeUnit.SECONDS)).thenReturn(false))) {

      val question = RegisterNewSubscription.forNewCommunications(SUBSCRIPTION_SERVICE_URL);
      val result = question.answeredBy(actor);

      assertFalse(result);
    }
  }

  @Test
  void shouldReturnFalseWhenWebSocketCannotBindToSubscription() {
    val subscriptionId = "12345";
    val bearerToken = "raw-token";
    val subscription = buildSubscriptionResource(subscriptionId, bearerToken);

    val response = mock(FhirBResponse.class);
    when(response.getExpectedResource()).thenReturn(subscription);
    when(erpClient.request(any())).thenReturn(response);

    when(useSubscriptionService.getSubscriptionId()).thenReturn(subscriptionId);
    when(useSubscriptionService.getAuthorization()).thenReturn("Bearer " + bearerToken);

    try (MockedConstruction<WebSocketClient> wsConstruction =
        mockConstruction(
            WebSocketClient.class,
            (mockWs, context) -> {
              when(mockWs.connectBlocking(30, TimeUnit.SECONDS)).thenReturn(true);

              val bindLatch = mock(CountDownLatch.class);
              when(mockWs.bind(subscriptionId)).thenReturn(bindLatch);
              when(mockWs.isBound()).thenReturn(false);
            })) {

      val question = RegisterNewSubscription.forNewCommunications(SUBSCRIPTION_SERVICE_URL);

      val result = question.answeredBy(actor);

      assertFalse(result);
      assertEquals(1, wsConstruction.constructed().size());

      verify(wsConstruction.constructed().get(0)).bind(subscriptionId);
      verify(wsConstruction.constructed().get(0)).isBound();
    }
  }
}
