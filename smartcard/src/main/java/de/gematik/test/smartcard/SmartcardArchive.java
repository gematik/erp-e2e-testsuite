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

package de.gematik.test.smartcard;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import java.util.List;
import lombok.Getter;
import lombok.val;

@Getter
public class SmartcardArchive {

  private final List<SmcB> smcbCards;
  private final List<Hba> hbaCards;
  private final List<Egk> egkCards;

  protected SmartcardArchive(List<SmcB> smcbCards, List<Hba> hbaCards, List<Egk> egkCards) {
    this.smcbCards = smcbCards;
    this.hbaCards = hbaCards;
    this.egkCards = egkCards;
  }

  public List<Hba> getHbaCards(Crypto crypto) {
    return filter(hbaCards, crypto);
  }

  public Hba getHbaByICCSN(String iccsn, Crypto crypto) {
    return getSmartcardByIccsn(getHbaCards(crypto), iccsn);
  }

  public List<SmcB> getSmcbCards(Crypto crypto) {
    return filter(smcbCards, crypto);
  }

  public SmcB getSmcbByICCSN(String iccsn, Crypto crypto) {
    return getSmartcardByIccsn(getSmcbCards(crypto), iccsn);
  }

  public List<Egk> getEgkCards(Crypto crypto) {
    return filter(egkCards, crypto);
  }

  public Egk getEgkByICCSN(String iccsn, Crypto crypto) {
    return getSmartcardByIccsn(getEgkCards(crypto), iccsn);
  }

  private <T extends Smartcard> List<T> filter(List<T> cards, Crypto crypto) {
    return cards.stream().filter(smartcard -> smartcard.getAlgorithm() == crypto).toList();
  }

  private <T extends Smartcard> T getSmartcardByIccsn(List<T> cards, String iccsn) {
    // simply make a pre-check to ensure we can fetch a template from the list
    if (cards.isEmpty()) {
      throw new CardNotFoundException(
          format(
              "Cannot find smartcard with ICCSN {0} in an empty list of given smartcards", iccsn));
    }
    val template = cards.get(0);
    val type = template.getType();
    val crypto = template.getAlgorithm();
    return cards.stream()
        .filter(smartcard -> smartcard.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(type, iccsn, crypto));
  }
}
