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

import de.gematik.test.smartcard.exceptions.CardNotFoundException;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;

public class SmartcardArchive {

  @Getter private final List<SmcB> smcbCards;

  @Getter private final List<Hba> hbaCards;

  @Getter private final List<Egk> egkCards;

  public SmartcardArchive(List<SmcB> smcbCards, List<Hba> hbaCards, List<Egk> egkCards) {
    this.smcbCards = smcbCards;
    this.hbaCards = hbaCards;
    this.egkCards = egkCards;
  }

  public List<Hba> getHbaCards(Crypto crypto) {
    return hbaCards.stream()
        .filter(hba -> hba.getAlgorithm() == crypto)
        .collect(Collectors.toList());
  }

  public Hba getHbaByICCSN(String iccsn, Crypto crypto) {
    return getHbaCards(crypto).stream()
        .filter(hba -> hba.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(SmartcardType.HBA, iccsn, crypto));
  }

  public List<SmcB> getSmcbCards(Crypto crypto) {
    return smcbCards.stream()
        .filter(smcb -> smcb.getAlgorithm() == crypto)
        .collect(Collectors.toList());
  }

  public SmcB getSmcbByICCSN(String iccsn, Crypto crypto) {
    return getSmcbCards(crypto).stream()
        .filter(smcb -> smcb.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(SmartcardType.SMC_B, iccsn, crypto));
  }

  public List<Egk> getEgkCards(Crypto crypto) {
    return egkCards.stream()
        .filter(egk -> egk.getAlgorithm() == crypto)
        .collect(Collectors.toList());
  }

  public Egk getEgkByICCSN(String iccsn, Crypto crypto) {
    return getEgkCards(crypto).stream()
        .filter(egk -> egk.getIccsn().equals(iccsn))
        .findFirst()
        .orElseThrow(() -> new CardNotFoundException(SmartcardType.EGK, iccsn, crypto));
  }

  public void destroy() {
    hbaCards.forEach(Hba::destroy);
  }
}
