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

package de.gematik.test.cardterminal;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import java.math.BigInteger;
import java.util.GregorianCalendar;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

@Getter
@Builder
public class CardInfo {

  @NonNull private final String handle;
  @NonNull private final String iccsn;

  private final BigInteger slot;
  @NonNull private final String ctId;
  @NonNull private final SmartcardType type;
  private final GregorianCalendar insertTime;

  @Override
  public String toString() {
    return format(
        "CardHandle \"{0}\" for {1} with ICCSN {2} in Slot {3}", handle, type, iccsn, slot);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    CardInfo that = (CardInfo) o;

    return iccsn.equals(that.iccsn);
  }

  @Override
  public int hashCode() {
    return iccsn.hashCode();
  }

  public static CardInfo fromCardInfoType(CardInfoType cit) {
    return new CardInfoBuilder()
        .handle(cit.getCardHandle())
        .iccsn(cit.getIccsn())
        .slot(cit.getSlotId())
        .ctId(cit.getCtId())
        .type(SmartcardType.fromString(cit.getCardType().value()))
        .insertTime(cit.getInsertTime() != null ? cit.getInsertTime().toGregorianCalendar() : null)
        .build();
  }
}
