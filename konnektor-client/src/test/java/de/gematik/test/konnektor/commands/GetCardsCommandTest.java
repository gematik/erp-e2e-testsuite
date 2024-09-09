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

package de.gematik.test.konnektor.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.test.konnektor.util.CardsUtil;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import de.gematik.ws.conn.eventservice.wsdl.v7.FaultMessage;
import de.gematik.ws.tel.error.v2.Error;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GetCardsCommandTest {

  private ContextType ctx;
  private ServicePortProvider mockProvider;
  private EventServicePortType mockEventService;

  @BeforeEach
  void setUp() {
    ctx = new ContextType();
    ctx.setClientSystemId("cs1");
    ctx.setMandantId("m1");
    ctx.setUserId("u1");
    ctx.setWorkplaceId("w1");

    mockProvider = mock(ServicePortProvider.class);
    mockEventService = mock(EventServicePortType.class);
    when(mockProvider.getEventService()).thenReturn(mockEventService);
  }

  @Test
  @SneakyThrows
  void getRandomCards() {
    val status = new Status();
    status.setResult("OK");
    val mockResponse = CardsUtil.createGetCardsResponse(status, 10);

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    val getCardsCmd = new GetCardsCommand();
    val response = getCardsCmd.execute(ctx, mockProvider);
    assertEquals(status, response.getStatus());
    assertEquals(10, response.getCards().getCard().size());
  }

  @Test
  @SneakyThrows
  void shouldThrowSOAPRequestException() {
    val error = new Error();
    error.setMessageID("Message ID");
    val faulMessage = new FaultMessage("MockError", error);

    when(mockEventService.getCards(any())).thenThrow(faulMessage);
    val getCardsCmd = new GetCardsCommand();
    assertThrows(RuntimeException.class, () -> getCardsCmd.execute(ctx, mockProvider));
    // Note: see AbstractKonnektorCommand#executeSupplier
    //    assertThrows(SOAPRequestException.class, () -> getCardsCmd.execute(ctx, mockProvider));
  }
}
