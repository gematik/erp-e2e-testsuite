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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.konnektor.util.CardsUtil;
import de.gematik.ws.conn.cardservicecommon.v2.CardTypeType;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CardTerminalTest {

  @BeforeEach
  void setUp() {}

  @Test
  void testEquals() {
    val ct1 = new CardTerminal("CT1");
    val ct2 = new CardTerminal("CT1");
    assertEquals(ct1, ct2);
    assertNotNull(ct1.getCtId());
    assertEquals(ct1.hashCode(), ct1.hashCode());
    assertNotNull(ct1.toString());
  }

  @Test
  void resetCards() {
    val ct1 = new CardTerminal("CT1");
    ct1.addCard(
        CardInfo.fromCardInfoType(CardsUtil.builder().type(CardTypeType.EGK).slot(1).build()));
    assertDoesNotThrow(ct1::resetSlots);
  }
}
