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

package de.gematik.test.cardterminal;

import de.gematik.test.cardterminal.exceptions.NoAppropriateSlotException;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorType;
import de.gematik.test.konnektor.commands.GetCardsCommand;
import de.gematik.test.smartcard.Smartcard;
import de.gematik.test.smartcard.SmartcardType;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.*;

import static java.text.MessageFormat.format;

@Slf4j
public class CardTerminalManager {

  private final Konnektor konnektor;
  private final Set<CardTerminalClient> cardTerminalClients;
  private final Set<CardTerminal> cardTerminals = new HashSet<>();

  public CardTerminalManager(
      @NonNull Konnektor konnektor, @NonNull Collection<CardTerminalClient> cardTerminalClients) {
    this.konnektor = konnektor;
    this.cardTerminalClients = new HashSet<>(cardTerminalClients);
  }

  public void refresh() {
    if (konnektor.getType() == KonnektorType.LOCAL) {
      return;
    }

    cardTerminals.forEach(CardTerminal::resetSlots);

    val resp = konnektor.execute(new GetCardsCommand());
    val cards =
        resp.getPayload().getCards().getCard().stream().map(CardInfo::fromCardInfoType).toList();

    cards.forEach(
        card -> {
          val ct = getCardTerminal(card.getCtId());
          ct.addCard(card);
        });
  }

  private CardTerminal getCardTerminal(@NonNull String ctId) {
    return cardTerminals.stream()
        .filter(it -> ctId.equals(it.getCtId()))
        .findFirst()
        .orElseGet(
            () -> {
              val ct = new CardTerminal(ctId);
              cardTerminals.add(ct);
              return ct;
            });
  }

  private Optional<CardTerminalClient> getCardTerminalClient(@NonNull CardTerminal ct) {
    return cardTerminalClients.stream().filter(it -> ct.getCtId().equalsIgnoreCase(it.getCtId())).findFirst();
  }

  public boolean insertCard(@NonNull Smartcard card) {
    if (konnektor.getType() == KonnektorType.LOCAL) {
      return false;
    }

    refresh();

    if (cardTerminals.isEmpty()) {
      log.warn("No card terminals connected. Please run refresh() first.");
      return false;
    }

    val ctSlot =
        // if the card is already inserted, get occupied slot
        getOccupiedSlot(card)
            // get a free slot of any connected and activated card terminal
            .or(this::getFreeSlot)
            // otherwise get any slot from any card terminal that
            // corresponds to a preferred card type (e.g. swap an earlier plugged egk card to
            // another egk card)
            // or is not occupied with a smb-kt card
            .or(this::getAnySlotForProfessionalCard)
            .orElseThrow(() -> new NoAppropriateSlotException(card));

    // If it is not a physical card terminal and a card terminal client is known, then use your
    // power.
    // For a physical card terminal, the SOAP operation requestCard could be called
    val ctClient = getCardTerminalClient(ctSlot.getCt());
    ctClient.ifPresentOrElse(
        client -> client.insertCard(card, ctSlot.getSlot()),
        () ->
            log.warn(
                format(
                    "no card terminal client with id {0} has matched. Card terminal client ids: {1}",
                    ctSlot.getCt().getCtId(), cardTerminalClients)));

    return true;
  }

  private Optional<CardTerminalSlot> getOccupiedSlot(Smartcard card) {
    return this.cardTerminals.stream().flatMap(it -> it.getSlots().stream())
            .filter(it -> it.getCard().getIccsn().equals(card.getIccsn()))
            .findFirst();
  }

  private Optional<CardTerminalSlot> getFreeSlot() {
    return cardTerminals.stream()
        .map(CardTerminal::getFreeSlot)
        .filter(Optional::isPresent)
        .map(Optional::get)
        .findFirst();
  }

  private Optional<CardTerminalSlot> getAnySlotForProfessionalCard() {
    Comparator<CardTerminalSlot> comparatorAsc =
        Comparator.comparing(slot -> slot.getCard().getInsertTime());

    return cardTerminals.stream()
        .flatMap(
            ct ->
                ct.getSlots().stream()
                    .filter(CardTerminalSlot::isOccupied)
                    .filter(slot -> !slot.getCard().getType().equals(SmartcardType.SMC_KT)))
        .min(comparatorAsc);
  }
}
