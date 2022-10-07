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

import de.gematik.test.erezept.fhir.parser.profiles.ErpNamingSystem;
import java.util.Objects;
import lombok.Getter;
import org.hl7.fhir.r4.model.Identifier;

/** https://de.wikipedia.org/wiki/Institutionskennzeichen */
public class IKNR {

  @Getter private final String value;

  @Getter private static final ErpNamingSystem system = ErpNamingSystem.IKNR;

  public IKNR(String iknr) {
    this.value = iknr;
  }

  public static IKNR from(String value) {
    return new IKNR(value);
  }

  public Identifier asIdentifier() {
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
