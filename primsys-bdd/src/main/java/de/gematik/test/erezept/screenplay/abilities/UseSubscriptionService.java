/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.screenplay.abilities;

import de.gematik.test.erezept.client.websocket.WebSocketClient;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.serenitybdd.screenplay.Ability;

@Slf4j
public class UseSubscriptionService implements Ability {

  @Setter @Getter private String authorization;

  @Setter @Getter private String subscriptionId;

  @Getter private WebSocketClient websocket;

  private UseSubscriptionService() {}

  public void setWebsocket(@NonNull WebSocketClient websocketClient) {
    this.websocket = websocketClient;
  }

  public static UseSubscriptionService use() {
    return new UseSubscriptionService();
  }
}
