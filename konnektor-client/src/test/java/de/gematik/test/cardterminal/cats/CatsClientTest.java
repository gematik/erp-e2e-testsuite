/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.cardterminal.cats;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import de.gematik.test.cardterminal.cfg.CardTerminalClientConfiguration;
import de.gematik.test.cardterminal.exceptions.CardTerminalClientException;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import kong.unirest.Config;
import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockedStatic;

class CatsClientTest {

  private SmartcardArchive smartcards;
  private Egk hannaEgk;
  private MockedStatic<Unirest> unitRestMock;

  @BeforeEach
  void setUp() {
    smartcards = SmartcardFactory.getArchive();
    unitRestMock = mockStatic(Unirest.class, Answers.RETURNS_DEEP_STUBS);
    when(Unirest.primaryInstance()).thenReturn(new UnirestInstance(new Config()));
  }

  @SneakyThrows
  @SuppressWarnings("unchecked")
  private void prepareMock(int statusCode, String body) {
    val httpResponseMock = mock(HttpResponse.class);
    when(Unirest.post(any())
            .body(anyString())
            .contentType(anyString())
            .asString()
            .ifFailure(any())
            .ifSuccess(any()))
        .thenReturn(httpResponseMock);

    // mock response status code
    when(httpResponseMock.getStatus()).thenReturn(statusCode);
    when(httpResponseMock.getBody()).thenReturn(body);
  }

  @Test
  void insertCardSuccessfully() {
    prepareMock(200, "");
    val client = new CardTerminalClientConfiguration("CT1", "http://localhost").create();
    client.insertCard(smartcards.getEgkByICCSN("80276883110000108142"), 1);
  }

  @Test
  void insertCardError() {
    prepareMock(400, "invalid access");
    val client = new CardTerminalClientConfiguration("CT1", "http://localhost").create();
    val smartCard = smartcards.getEgkByICCSN("80276883110000108142");
    assertThrows(CardTerminalClientException.class, () -> client.insertCard(smartCard, 1));
  }

  @Test
  void equals() {
    val client1 = new CardTerminalClientConfiguration("CT1", "http://localhost").create();
    val client2 = new CardTerminalClientConfiguration("CT1", "http://localhost").create();
    assertEquals(client1, client2);
    assertNotNull(client1.getCtId());
    assertEquals(client1.hashCode(), client2.hashCode());
    assertNotNull(client1.toString());
  }

  @AfterEach
  void reset_mocks() {
    unitRestMock.closeOnDemand();
  }
}
