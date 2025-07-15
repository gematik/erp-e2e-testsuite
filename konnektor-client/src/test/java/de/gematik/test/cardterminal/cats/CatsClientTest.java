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

package de.gematik.test.cardterminal.cats;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.cardterminal.CardTerminalClientFactory;
import de.gematik.test.erezept.config.dto.konnektor.CardTerminalClientConfiguration;
import kong.unirest.core.Config;
import kong.unirest.core.HttpResponse;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
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
    smartcards = SmartcardArchive.fromResources();
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
    val client = CardTerminalClientFactory.createCatsClient("CT1", "http://localhost");
    client.insertCard(smartcards.getEgkByICCSN("80276883110000108142"), 1);
  }

  @Test
  void insertCardError() {
    prepareMock(400, "invalid access");
    val ctConfig = new CardTerminalClientConfiguration("CT1", "http://localhost");
    val client = CardTerminalClientFactory.createClient(ctConfig);
    val smartCard = smartcards.getEgkByICCSN("80276883110000108142");
    assertThrows(RuntimeException.class, () -> client.insertCard(smartCard, 1));
    // Note: see CatsClient#request
    //    assertThrows(CardTerminalClientException.class, () -> client.insertCard(smartCard, 1));
  }

  @Test
  void equals() {
    val client1 = CardTerminalClientFactory.createCatsClient("CT1", "http://localhost");
    val client2 = CardTerminalClientFactory.createCatsClient("CT1", "http://localhost");
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
