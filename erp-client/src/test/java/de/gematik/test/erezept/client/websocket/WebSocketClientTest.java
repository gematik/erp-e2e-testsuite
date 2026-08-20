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

package de.gematik.test.erezept.client.websocket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.java_websocket.handshake.ServerHandshake;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WebSocketClientTest {

  private WebSocketClient client;

  @BeforeEach
  void setUp() {
    client = spy(new WebSocketClient("wss://localhost:8443/subscription", "Bearer token"));
  }

  private void setField(String fieldName, Object value) throws Exception {
    Field field = WebSocketClient.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(client, value);
  }

  @Test
  void shouldCountDownLatchOnClose() throws Exception {
    CountDownLatch latch = new CountDownLatch(1);

    setField("countDownLatch", latch);

    client.onClose(1000, "Normal Closure", false);

    assertEquals(0, latch.getCount());
  }

  @Test
  void shouldNotFailWhenCloseWithoutLatch() {
    assertDoesNotThrow(() -> client.onClose(1000, "Normal Closure", false));
  }

  @Test
  void shouldHandleOpenWithContent() {
    ServerHandshake handshake = mock(ServerHandshake.class);

    when(handshake.getHttpStatus()).thenReturn((short) 101);
    when(handshake.getContent()).thenReturn("connected".getBytes(StandardCharsets.UTF_8));

    assertDoesNotThrow(() -> client.onOpen(handshake));
  }

  @Test
  void shouldHandleOpenWithoutContent() {
    ServerHandshake handshake = mock(ServerHandshake.class);

    when(handshake.getHttpStatus()).thenReturn((short) 101);
    when(handshake.getContent()).thenReturn(null);

    assertDoesNotThrow(() -> client.onOpen(handshake));
  }

  @Test
  void shouldHandleError() {
    assertDoesNotThrow(() -> client.onError(new RuntimeException("failure")));
  }

  @Test
  void shouldThrowExceptionWhenBindWithoutConnection() {
    assertThrows(WebsocketNotConnectedException.class, () -> client.bind("subscription123"));
  }
}
