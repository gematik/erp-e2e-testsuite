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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.test.konnektor.util.CardsUtil;
import de.gematik.ws.conn.cardservice.v8.CardInfoType;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class CardInfoTest {

  @Test
  void createCardHandleWithBuilder() {
    val smartCardTypes =
        List.of(SmartcardType.SMC_B, SmartcardType.EGK, SmartcardType.EGK, SmartcardType.SMC_KT);

    smartCardTypes.forEach(
        sct -> {
          val ch =
              new CardInfo.CardInfoBuilder()
                  .handle("cardhandle")
                  .iccsn("80276001011699910102")
                  .ctId("Ct01")
                  .type(sct)
                  .build();
          assertNotNull(ch);
          assertEquals("cardhandle", ch.getHandle());
          assertEquals("80276001011699910102", ch.getIccsn());
          assertEquals("Ct01", ch.getCtId());
          assertEquals(sct, ch.getType());
          assertTrue(ch.toString().contains("80276001011699910102"));
        });
  }

  @Test
  void createCardHandleFromCardInfoType() {
    val smartCardTypes = List.of(CardTypeType.EGK, CardTypeType.HBA, CardTypeType.SMC_B);

    smartCardTypes.forEach(
        sct -> {
          val cit = new CardInfoType();
          cit.setCardHandle("cardhandle");
          cit.setCardType(sct);
          cit.setCtId("Ct01");
          cit.setIccsn("80276001011699910102");

          val ch = CardInfo.fromCardInfoType(cit);
          assertNotNull(ch);
          assertEquals("cardhandle", ch.getHandle());
          assertEquals("80276001011699910102", ch.getIccsn());
          assertEquals("Ct01", ch.getCtId());
          assertEquals(SmartcardType.fromString(sct.value()), ch.getType());
          assertTrue(ch.toString().contains("80276001011699910102"));
        });
  }

  @Test
  void testEquals() {
    val card1 =
        CardInfo.fromCardInfoType(
            CardsUtil.builder()
                .type(CardTypeType.EGK)
                .ctId("CT1")
                .slot(1)
                .iccsn("X12345678")
                .build());
    val card2 =
        CardInfo.fromCardInfoType(
            CardsUtil.builder()
                .type(CardTypeType.EGK)
                .ctId("CT1")
                .slot(1)
                .iccsn("X12345678")
                .build());
    assertEquals(card1, card2);
    assertNotNull(card1.getCtId());
    assertEquals(card1.hashCode(), card1.hashCode());
    assertNotNull(card1.toString());
  }
}
