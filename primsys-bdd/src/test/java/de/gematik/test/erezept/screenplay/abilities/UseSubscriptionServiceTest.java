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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.client.websocket.WebSocketClient;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseSubscriptionServiceTest {

  @Test
  void shouldThrowOnNullClient() {
    val ability = UseSubscriptionService.use();
    assertThrows(
        NullPointerException.class, () -> ability.setWebsocket(null)); // NOSONAR intentionally null
  }

  @Test
  void shouldAcceptClient() {
    // well, only for covering the setter
    val ability = UseSubscriptionService.use();
    val mockClient = mock(WebSocketClient.class);
    ability.setWebsocket(mockClient);
    assertEquals(mockClient, ability.getWebsocket());
  }
}
