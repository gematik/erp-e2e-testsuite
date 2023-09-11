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

package de.gematik.test.smartcard;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import java.util.List;
import lombok.Getter;
import lombok.val;

public class SmartcardArchive {

  @Getter private final List<SmcB> smcbCards;
  @Getter private final List<Hba> hbaCards;
  @Getter private final List<Egk> egkCards;

  protected SmartcardArchive(List<SmcB> smcbCards, List<Hba> hbaCards, List<Egk> egkCards) {
    this.smcbCards = smcbCards;
    this.hbaCards = hbaCards;
    this.egkCards = egkCards;
  }

  public Hba getHbaByICCSN(String iccsn) {
    return getSmartcardByIccsn(getHbaCards(), iccsn);
  }

  public SmcB getSmcbByICCSN(String iccsn) {
    return getSmartcardByIccsn(getSmcbCards(), iccsn);
  }

  public Egk getEgkByICCSN(String iccsn) {
    return getSmartcardByIccsn(getEgkCards(), iccsn);
  }

  public Egk getEgkByKvnr(String kvnr) {
    checkCards(getEgkCards());
    return getEgkCards().stream()
        .filter(smartcard -> smartcard.getKvnr().equals(kvnr))
        .findFirst()
        .orElseThrow(
            () ->
                new CardNotFoundException(
                    format("Card of type {0} with KVNR {1} not found", SmartcardType.EGK, kvnr)));
  }

  private <T extends Smartcard> T getSmartcardByIccsn(List<T> cards, String iccsn) {
    checkCards(cards);
    val type = cards.get(0).getType();
    return cards.stream()
        .filter(smartcard -> smartcard.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(type, iccsn));
  }

  private <T extends Smartcard> void checkCards(List<T> cards) {
    // simply make a pre-check to ensure we can fetch a template from the list
    if (cards.isEmpty()) {
      throw new CardNotFoundException("Cannot find smartcard in an empty list of given smartcards");
    }
  }
}
