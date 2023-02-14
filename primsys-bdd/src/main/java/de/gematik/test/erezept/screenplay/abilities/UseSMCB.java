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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.SmcB;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import net.serenitybdd.screenplay.Ability;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UseSMCB implements Ability {

  @Getter private final SmcB smcB;

  public String getTelematikID() {
    return this.smcB.getTelematikId();
  }

  public static UseSMCB itHasAccessTo(SmcB smcb) {
    return new UseSMCB(smcb);
  }

  @Override
  public String toString() {
    return format("SMC-B (ICCSN {0}) für Telematik-ID {1}", smcB.getIccsn(), smcB.getTelematikId());
  }
}
