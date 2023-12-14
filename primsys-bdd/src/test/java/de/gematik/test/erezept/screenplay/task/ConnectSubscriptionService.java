/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.client.websocket.WebSocketClient;
import de.gematik.test.erezept.exceptions.WebSocketException;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.concurrent.TimeUnit;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class ConnectSubscriptionService implements Task {

  private final String subscriptionServiceUrl;

  public ConnectSubscriptionService(String subscriptionServiceUrl) {
    this.subscriptionServiceUrl = subscriptionServiceUrl;
  }

  @SneakyThrows
  @Override
  public <T extends Actor> void performAs(T actor) {
    val useSubscriptionService = SafeAbility.getAbility(actor, UseSubscriptionService.class);

    val websocket =
        new WebSocketClient(subscriptionServiceUrl, useSubscriptionService.getAuthorization());
    useSubscriptionService.setWebsocket(websocket);

    if (websocket.connectBlocking(10, TimeUnit.SECONDS)) {
      websocket.bind(useSubscriptionService.getSubscriptionId()).await();
      if (!websocket.isBound()) {
        throw new WebSocketException("WebSocket isn't bound to subscription id");
      }
    } else {
      throw new WebSocketException("WebSocket isn't connected");
    }
  }

  public static ConnectSubscriptionService connect(String subscriptionServiceUrl) {
    return new ConnectSubscriptionService(subscriptionServiceUrl);
  }
}
