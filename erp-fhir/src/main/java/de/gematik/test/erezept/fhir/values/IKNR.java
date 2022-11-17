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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import java.util.Objects;
import lombok.Getter;
import org.hl7.fhir.r4.model.Identifier;

/** https://de.wikipedia.org/wiki/Institutionskennzeichen */
public class IKNR {

  @Getter private final String value;

  @Getter private static final DeBasisNamingSystem system = DeBasisNamingSystem.IKNR;

  public IKNR(String iknr) {
    this.value = iknr;
  }

  public static IKNR from(String value) {
    return new IKNR(value);
  }

  public Identifier asIdentifier() {
    return this.asIdentifier(system);
  }

  /**
   * Note: DeBasisNamingSystem.IKNR is the default NamingSystem for IKNR, however on some newer
   * profiles DeBasisNamingSystem.IKNR_SID is required. This method will become obsolete once all
   * profiles are using a common naming system for IKNR
   *
   * @param system to use for encoding the IKNR value
   * @return the IKNR as Identifier
   */
  public Identifier asIdentifier(DeBasisNamingSystem system) {
    return new Identifier().setSystem(system.getCanonicalUrl()).setValue(value);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IKNR iknr = (IKNR) o;
    return Objects.equals(value, iknr.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }
}
