/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.konnektor.commands;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.konnektor.exceptions.SmartcardMissmatchException;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.test.konnektor.util.CardsUtil;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.connectorcontext.v2.ContextType;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class GetCardHandleCommandTest {

  private ContextType ctx;
  private ServicePortProvider mockProvider;
  private EventServicePortType mockEventService;

  @Before
  public void setUp() {
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
  public void shouldFindExistingCardHandleByIccsn() {
    val iccsn = "80276001011699910102";
    val status = new Status();
    status.setResult("OK");
    val mockResponse = CardsUtil.createGetCardsResponse(status, 10);
    val cardOfInterest = CardsUtil.createHba(iccsn);
    mockResponse.getCards().getCard().add(cardOfInterest);

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    val getCardHandleCmd = GetCardHandleCommand.forIccsn(iccsn);
    val cardHandle = getCardHandleCmd.execute(ctx, mockProvider);
    assertEquals(iccsn, cardHandle.getIccsn());
    assertEquals(SmartcardType.HBA, cardHandle.getType());
    assertNotNull(cardHandle.getHandle());
  }

  @Test
  @SneakyThrows
  public void shouldThrowSmartcardMissmatchExceptionOnUnavailableIccsn() {
    val status = new Status();
    status.setResult("OK");
    val mockResponse = CardsUtil.createGetCardsResponse(status, 10);

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    val getCardHandleCmd = GetCardHandleCommand.forIccsn("80276001011699910102");
    assertThrows(
        SmartcardMissmatchException.class, () -> getCardHandleCmd.execute(ctx, mockProvider));
  }
}
