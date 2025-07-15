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

package de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import jakarta.ws.rs.WebApplicationException;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.websockets.ProtocolHandler;
import org.glassfish.grizzly.websockets.WebSocketListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GrizzlyWsServerAppTest {
  @Test
  void shouldNotSend() {
    GrizzlyWsServerApp grizzlyWsServerApp = new GrizzlyWsServerApp();
    var mockWsClient = mock(WebSocketListener.class);
    var mockHandler = mock(ProtocolHandler.class);
    var httpRequestPacke = HttpRequestPacket.builder().uri("/ClientTestId").build();
    grizzlyWsServerApp.createSocket(mockHandler, httpRequestPacke, mockWsClient);
    grizzlyWsServerApp.send("123TestId", "TestMessage");
    verify(mockHandler, times(0)).send(anyString());
  }

  @Test
  void shouldSend() {
    GrizzlyWsServerApp grizzlyWsServerApp = new GrizzlyWsServerApp();
    var mockWsClient = mock(WebSocketListener.class);
    var mockHandler = mock(ProtocolHandler.class);
    var httpRequestPacke = HttpRequestPacket.builder().uri("/ClientTestId").build();
    var socket = grizzlyWsServerApp.createSocket(mockHandler, httpRequestPacke, mockWsClient);
    socket.onConnect();
    grizzlyWsServerApp.send("ClientTestId", "TestMessage");
    verify(mockHandler, times(1)).send(anyString());
  }

  @ParameterizedTest
  @CsvSource({
    " , '123456897TestBody', '123TransAction' ",
    "'123TestId', '', '123TransAction'  ",
    "'123TestId', 123456897TestBody, '123TransAction'  ",
    " , '123456897TestBody', '123TransAction'   ",
    "'123TestId', '', '123TransAction' ",
    "'123TestId', 123456897TestBody,  ",
  })
  void shouldThrowException(String telemId, String bodyContent, String transactionID) {
    GrizzlyWsServerApp grizzlyWsServerApp = new GrizzlyWsServerApp();
    var byteBody = bodyContent.getBytes();
    PspMessage pspMessage =
        PspMessage.create(DeliveryOption.ON_PREMISE, "123", transactionID, byteBody);
    assertThrows(
        WebApplicationException.class,
        () -> {
          grizzlyWsServerApp.send(telemId, pspMessage);
        });
  }
}
