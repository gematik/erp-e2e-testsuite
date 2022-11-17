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

package de.gematik.test.erezept.screenplay.abilities;

import static java.text.MessageFormat.format;

import de.gematik.test.smartcard.Egk;
import lombok.Getter;
import net.serenitybdd.screenplay.Ability;

public class ProvideEGK implements Ability {

  @Getter private final Egk egk;

  private ProvideEGK(Egk egk) {
    this.egk = egk;
  }

  public String getKvnr() {
    return egk.getKvnr();
  }

  public static ProvideEGK sheOwns(Egk egk) {
    return heOwns(egk);
  }

  public static ProvideEGK heOwns(Egk egk) {
    return new ProvideEGK(egk);
  }

  @Override
  public String toString() {
    return format(
        "Elektronische Gesundheitskarte (ICCSN {0}) für die KVID {1}",
        egk.getIccsn(), egk.getKvnr());
  }
}