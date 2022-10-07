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

package de.gematik.test.konnektor.soap.mock;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.Smartcard;
import de.gematik.test.smartcard.SmartcardType;
import de.gematik.test.smartcard.exceptions.InvalidSmartcardTypeException;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import lombok.Getter;
import lombok.val;

public class SmartcardWrapper {

  @Getter private final Smartcard smartcard;
  @Getter private final CardInfoType infoType;

  protected SmartcardWrapper(Smartcard smartcard) {
    this.smartcard = smartcard;
    this.infoType = createCardInfoType(smartcard);
  }

  protected SmartcardWrapper(Egk egkSmartcard) {
    this.smartcard = egkSmartcard;
    this.infoType = createCardInfoType(egkSmartcard);
  }

  public String getCardHandle() {
    return infoType.getCardHandle();
  }

  private static CardInfoType createCardInfoType(Egk egk) {
    val cit = createCardInfoType((Smartcard) egk);
    cit.setKvnr(egk.getKvnr());
    return cit;
  }

  private static CardInfoType createCardInfoType(Smartcard smartcard) {
    val cit = new CardInfoType();
    cit.setCardType(mapSmartcardType(smartcard.getType()));
    cit.setIccsn(smartcard.getIccsn());
    cit.setCardHandle(createCardHandleString(smartcard));

    return cit;
  }

  private static CardTypeType mapSmartcardType(SmartcardType type) {
    CardTypeType cardTypeType;
    switch (type) {
      case EGK:
        cardTypeType = CardTypeType.EGK;
        break;
      case SMC_B:
        cardTypeType = CardTypeType.SMC_B;
        break;
      case HBA:
        cardTypeType = CardTypeType.HBA;
        break;
      default:
        throw new InvalidSmartcardTypeException(type.toString());
    }
    return cardTypeType;
  }

  private static String createCardHandleString(Smartcard smartcard) {
    return format(
        "{0}_{1}_{2}",
        smartcard.getType(), smartcard.getAlgorithm().getAlgorithm(), smartcard.getIccsn());
  }
}
