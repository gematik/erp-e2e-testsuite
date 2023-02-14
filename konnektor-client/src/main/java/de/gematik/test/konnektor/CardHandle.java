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

package de.gematik.test.konnektor;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.SmartcardType;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardHandle {

  private final String handle;
  private final String iccsn;
  private final SmartcardType type;

  @Override
  public String toString() {
    return format("CardHandle \"{0}\" for {1} with ICCSN {2}", handle, type, iccsn);
  }

  public static CardHandle fromCardInfoType(CardInfoType cit) {
    return new CardHandleBuilder()
        .handle(cit.getCardHandle())
        .iccsn(cit.getIccsn())
        .type(SmartcardType.fromString(cit.getCardType().value()))
        .build();
  }
}
