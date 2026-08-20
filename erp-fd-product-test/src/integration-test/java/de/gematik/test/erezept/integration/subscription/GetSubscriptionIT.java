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

package de.gematik.test.erezept.integration.subscription;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.erezept.tasks.RegisterNewSubscription;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Subscription Service: Prüfung Bearer-Token bei GET /Subscription")
@Tag("SUBSCRIPTION")
public class GetSubscriptionIT extends ErpTest {

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @BeforeEach
  void setupSubscriptionService() {
    pharmacy.can(UseSubscriptionService.use());
  }

  @TestcaseId("ERP_GET_SUBSCRIPTION_01")
  @DisplayName("Die Apotheke darf die Subscription mit gültigem Bearer-Token abrufen")
  @Test
  void shouldGetSubscriptionSuccessfully() {

    assertTrue(
        pharmacy.asksFor(
            RegisterNewSubscription.forNewCommunications(
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl())));

    val websocket = SafeAbility.getAbility(pharmacy, UseSubscriptionService.class).getWebsocket();

    assertTrue(websocket.isBound(), "WebSocket muss verbunden sein");
    assertFalse(websocket.isClosed(), "WebSocket darf nicht geschlossen sein");
  }

  @TestcaseId("ERP_GET_SUBSCRIPTION_02")
  @DisplayName("Die Apotheke darf die Subscription mit ungültigem Bearer-Token nicht abrufen")
  @Test
  void shouldFailGetSubscriptionWithInvalidBearerToken() {

    assertFalse(
        pharmacy.asksFor(
            RegisterNewSubscription.forCriteria(
                "Communication",
                config.getActiveEnvironment().getTi().getSubscriptionServiceUrl(),
                token -> "Bearer invalid-text")),
        "Ungültiger Bearer-Token");
  }
}
