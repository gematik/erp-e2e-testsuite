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

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CardTerminalSlot {

  private final CardTerminal ct;
  private final int slot;
  private CardInfo card;

  public CardTerminalSlot(CardTerminal ct, int slot) {
    this.ct = ct;
    this.slot = slot;
  }

  public CardTerminalSlot(CardTerminal ct, CardInfo card) {
    this.ct = ct;
    this.slot = card.getSlot().intValue();
    this.card = card;
  }

  public boolean isOccupied() {
    return card != null;
  }
}
