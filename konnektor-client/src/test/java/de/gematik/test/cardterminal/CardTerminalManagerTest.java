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
 */

package de.gematik.test.cardterminal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.cardterminal.cats.CatsClient;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorContextConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorType;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.KonnektorImpl;
import de.gematik.test.konnektor.soap.ServicePortProvider;
import de.gematik.test.konnektor.util.CardsUtil;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import de.gematik.ws.conn.connectorcommon.v5.Status;
import de.gematik.ws.conn.eventservice.wsdl.v7.EventServicePortType;
import java.time.ZonedDateTime;
import java.util.GregorianCalendar;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardTerminalManagerTest {

  private SmartcardArchive smartcards;
  private EventServicePortType mockEventService;
  private CardTerminalManager cardTerminalManager;
  private KonnektorContextConfiguration context;
  private ServicePortProvider mockProvider;

  private final List<CardInfoType> defaultCards =
      List.of(
          CardsUtil.builder().type(CardTypeType.EGK).slot(1).build(),
          CardsUtil.builder()
              .type(CardTypeType.HBA)
              .insertTime(GregorianCalendar.from(ZonedDateTime.now().minusHours(2)))
              .slot(2)
              .build(),
          CardsUtil.builder().type(CardTypeType.SMC_B).slot(3).build(),
          CardsUtil.builder()
              .type(CardTypeType.SMC_KT)
              .insertTime(GregorianCalendar.from(ZonedDateTime.now().minusHours(3)))
              .slot(4)
              .build());
  private CatsClient cats1Client;

  @SneakyThrows
  @BeforeEach
  void setUp() {
    smartcards = SmartcardArchive.fromResources();

    context = new KonnektorContextConfiguration();
    context.setMandantId("Mandant1");
    context.setClientSystemId("CS1");
    context.setWorkplaceId("WP1");

    mockProvider = mock(ServicePortProvider.class);
    mockEventService = mock(EventServicePortType.class);
    when(mockProvider.getEventService()).thenReturn(mockEventService);

    val status = new Status();
    status.setResult("OK");

    val hannaEgk = smartcards.getEgkByICCSN("80276883110000108142");
    val hannaEgkCard =
        CardsUtil.builder().type(CardTypeType.EGK).ctId("CT1").iccsn(hannaEgk.getIccsn()).build();
    val mockResponse = CardsUtil.createGetCardsResponse(status, hannaEgkCard);

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    cats1Client = mock(CatsClient.class);
    when(cats1Client.getCtId()).thenReturn("CT1");
    Konnektor konnektor =
        new KonnektorImpl(context, "KONSIM", KonnektorType.REMOTE, mockProvider, cats1Client);
    cardTerminalManager = konnektor.getCardTerminalManager();
  }

  @SneakyThrows
  @Test
  void getSlotForProfessionalCard() {
    val status = new Status();
    status.setResult("OK");
    val mockResponse = CardsUtil.createGetCardsResponse(status);
    defaultCards.forEach(it -> mockResponse.getCards().getCard().add(it));

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    assertDoesNotThrow(cardTerminalManager::refresh);
    val egk = smartcards.getEgkByICCSN("80276883110000113298");
    assertTrue(cardTerminalManager.insertCard(egk));
  }

  @Test
  void insertCardIntoFreeSlotSuccessful() {
    assertDoesNotThrow(cardTerminalManager::refresh);
    val egk = smartcards.getEgkByICCSN("80276883110000113298");
    assertTrue(cardTerminalManager.insertCard(egk));
  }

  @Test
  void softKonnShouldAlsoWork() {
    val konnektor = new KonnektorImpl(context, "KONSIM", KonnektorType.LOCAL, mockProvider);
    assertDoesNotThrow(() -> konnektor.getCardTerminalManager().refresh());
    val egk = smartcards.getEgkByICCSN("80276883110000113298");
    assertDoesNotThrow(() -> konnektor.getCardTerminalManager().insertCard(egk));
  }

  @Test
  void noCardTerminalClientsAvailable() {
    val konnektor = new KonnektorImpl(context, "KONSIM", KonnektorType.REMOTE, mockProvider);
    assertDoesNotThrow(() -> konnektor.getCardTerminalManager().refresh());
    val egk = smartcards.getEgkByICCSN("80276883110000113298");
    assertDoesNotThrow(() -> konnektor.getCardTerminalManager().insertCard(egk));
  }

  @SneakyThrows
  @Test
  void switchCards() {
    val status = new Status();
    status.setResult("OK");
    val mockResponse = CardsUtil.createGetCardsResponse(status);
    defaultCards.forEach(it -> mockResponse.getCards().getCard().add(it));

    when(mockEventService.getCards(any())).thenReturn(mockResponse);

    assertDoesNotThrow(cardTerminalManager::refresh);
    val egk = smartcards.getEgkByICCSN("80276883110000113298");
    assertTrue(cardTerminalManager.insertCard(egk));

    verify(cats1Client, times(1)).insertCard(egk, 2);
  }
}
