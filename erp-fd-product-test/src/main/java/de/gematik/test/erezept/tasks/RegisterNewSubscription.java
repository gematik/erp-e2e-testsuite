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

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.client.usecases.SubscriptionPostCommand;
import de.gematik.test.erezept.client.websocket.WebSocketClient;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Subscription;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class RegisterNewSubscription implements Question<Boolean> {

  private final String criteria;
  private final String subscriptionServiceUrl;
  private final Function<String, String> idpManipulator;

  @SneakyThrows
  @Override
  public Boolean answeredBy(Actor actor) {

    val useSubscriptionService = SafeAbility.getAbility(actor, UseSubscriptionService.class);

    val useSMCB = SafeAbility.getAbility(actor, UseSMCB.class);

    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val subscriptionCmd =
        new SubscriptionPostCommand(
            criteria + "?received=null&recipient=" + useSMCB.getTelematikID());

    val subscriptionResponse = erpClientAbility.request(subscriptionCmd);

    val subscription = subscriptionResponse.getExpectedResource();

    val subscriptionId = subscription.getIdElement().getIdPart();

    useSubscriptionService.setSubscriptionId(subscriptionId);

    val authorization =
        subscription.getChannel().getHeader().stream()
            .map(PrimitiveType::getValue)
            .filter(header -> header.startsWith("Authorization"))
            .map(header -> header.replace("Authorization: ", ""))
            .findFirst()
            .orElseThrow(
                () -> new MissingFieldException(Subscription.class, "Bearer token is missing"));

    useSubscriptionService.setAuthorization(idpManipulator.apply(authorization));

    val websocket =
        new WebSocketClient(subscriptionServiceUrl, useSubscriptionService.getAuthorization());
    useSubscriptionService.setWebsocket(websocket);

    if (websocket.connectBlocking(30, TimeUnit.SECONDS)) {
      websocket.bind(useSubscriptionService.getSubscriptionId()).await();
      if (!websocket.isBound()) {
        log.info(
            "WebSocket could not be bound to subscription ID '{}'.",
            useSubscriptionService.getSubscriptionId());
        return false;
      }
    } else {
      log.info("WebSocket connection timed out after 30 seconds.");
      return false;
    }

    log.info(
        "Subscription registered successfully. ID={}, URL={}",
        subscriptionId,
        subscriptionServiceUrl);
    return true;
  }

  public static RegisterNewSubscription forNewCommunications(String subscriptionServiceUrl) {
    return forCriteria("Communication", subscriptionServiceUrl);
  }

  public static RegisterNewSubscription forCriteria(
      String criteria, String subscriptionServiceUrl) {

    return forCriteria(criteria, subscriptionServiceUrl, idpToken -> idpToken);
  }

  public static RegisterNewSubscription forCriteria(
      String criteria, String subscriptionServiceUrl, UnaryOperator<String> idpManipulator) {

    return new RegisterNewSubscription(criteria, subscriptionServiceUrl, idpManipulator);
  }
}
