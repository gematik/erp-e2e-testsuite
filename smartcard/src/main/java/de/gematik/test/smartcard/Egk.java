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

import java.util.Objects;
import lombok.Getter;
import lombok.Setter;

public class Egk extends Smartcard {

  @Setter @Getter private String kvnr;

  public Egk(String icssn) {
    super(icssn, SmartcardType.EGK);
  }

  @Override
  public void destroy() {
    // nothing to be done!
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    if (!super.equals(o)) return false;
    final Egk egk = (Egk) o;
    return Objects.equals(getKvnr(), egk.getKvnr());
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), getKvnr());
  }
}
