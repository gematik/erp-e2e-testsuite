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

package de.gematik.test.erezept.lei.integration;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.cardterminal.CardTerminal;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.PrimSysBddFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;

@EnabledIfSystemProperty(named = "tests.integration", matches = "true")
class RemoteCardTerminalTest {

  private Konnektor konnektor;
  private SmartcardArchive smartcardArchive;

  @BeforeEach
  void setUp() {
    smartcardArchive = SmartcardArchive.fromResources();
    val cfg =
        ConfigurationReader.forPrimSysConfiguration()
            .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, smartcardArchive));
    val remoteCfg =
        cfg.getDto().getKonnektors().stream()
            .filter(k -> k.getName().equals("KOCO@kon7"))
            .findFirst()
            .orElseThrow();
    konnektor = Konnektor.create(remoteCfg);
  }

  @Test
  void shouldKnowAllCardTerminals() {
    konnektor
        .getCardTerminalOperator()
        .getCardTerminals()
        .forEach(
            ct -> {
              System.out.println("Terminal: " + ct.getCtId());

              ct.getAllSlots()
                  .forEach(
                      slot ->
                          System.out.println(
                              "Slot: " + slot.getSlotId() + ", " + slot.isOccupied()));
            });
    assertEquals(2, konnektor.getCardTerminalOperator().getCardTerminals().size());
  }

  @Test
  void reset() {
    konnektor.getCardTerminalOperator().getCardTerminals().forEach(CardTerminal::resetSlots);
    assertFalse(konnektor.getCardTerminalOperator().getCardTerminals().isEmpty());
    konnektor.getCardTerminalOperator().getCardTerminals().stream()
        .flatMap(ct -> ct.getAllSlots().stream())
        .forEach(slot -> assertFalse(slot.isOccupied(), "All slots should be empty after reset"));
  }

  @Test
  void shouldNotInsertCardTwice() {
    // reset first
    konnektor.getCardTerminalOperator().getCardTerminals().forEach(CardTerminal::resetSlots);

    // ICCSN of the card to be inserted
    String iccsn = "80276883110000170943";

    // Retrieve card with this ICCSN from SmartcardArchive
    val hba = smartcardArchive.getHbaByICCSN(iccsn);
    assertNotNull(hba, "HBA with ICCSN " + iccsn + " should exist");

    // First insertion - should be successful
    konnektor.insertCard(hba);

    // Second insertion with the same card
    konnektor.insertCard(hba);

    // Verify that the card is only active once across all CTs
    val allTerminals = konnektor.getCardTerminalOperator().getCardTerminals();

    // Count how many times the card with this ICCSN is active
    long cardCount =
        allTerminals.stream()
            .flatMap(ct -> ct.getAllSlots().stream())
            .filter(slot -> slot.getIccsn().isPresent() && iccsn.equals(slot.getIccsn().get()))
            .count();

    // The card should only be present once, even after second insert
    assertEquals(
        1, cardCount, "Card with ICCSN " + iccsn + " should only be active once across all CTs");
  }

  @Test
  void shouldInsertAllCardsAndVerifyPresence() {
    // reset first
    konnektor.getCardTerminalOperator().getCardTerminals().forEach(CardTerminal::resetSlots);

    val cards =
        List.of(
            smartcardArchive.getSmcbByICCSN("80276883110000116873"),
            smartcardArchive.getSmcbByICCSN("80276883110000116872"),
            smartcardArchive.getSmcbByICCSN("80276883110000163973"),
            smartcardArchive.getSmcbByICCSN("80276883110000163972"),
            smartcardArchive.getHbaByICCSN("80276883110000161759"),
            smartcardArchive.getHbaByICCSN("80276883110000170943"));

    cards.forEach(card -> konnektor.insertCard(card));

    // Alle gesteckten ICCSNs aus den belegten Slots sammeln
    val insertedIccsns =
        konnektor.getCardTerminalOperator().getCardTerminals().stream()
            .flatMap(ct -> ct.getAllSlots().stream())
            .filter(slot -> slot.isOccupied() && slot.getIccsn().isPresent())
            .map(slot -> slot.getIccsn().get())
            .toList();

    System.out.println("Gesteckte Karten (ICCSNs): " + insertedIccsns);

    cards.forEach(
        card ->
            assertTrue(
                insertedIccsns.contains(card.getIccsn()),
                "Karte mit ICCSN "
                    + card.getIccsn()
                    + " sollte gesteckt sein, ist aber nicht in: "
                    + insertedIccsns));
  }
}
