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

package de.gematik.test.erezept.fhir.values;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;

import java.util.Objects;
import java.util.regex.Pattern;

import static java.text.MessageFormat.format;

/** <a href="https://de.wikipedia.org/wiki/Institutionskennzeichen">Institutionskennzeichen</a> */
public class IKNR extends Value<String> implements WithChecksum {
  private static final Pattern IKNR_PATTERN = Pattern.compile("^\\d{9}$");

  public IKNR(String iknr) {
    super( DeBasisNamingSystem.IKNR, iknr);
  }

  public static IKNR from(String value) {
    return new IKNR(value);
  }

  public static IKNR random() {
    val faker = GemFaker.getFaker();
    val numbers = faker.regexify("\\d{8}");
    int checksum = calcChecksum(numbers);
    val value = format("{0}{1}", numbers,checksum);
    return from(value);
  }

  public boolean isValid() {
    val iknr = getValue();
    if (iknr == null) return false;
    val matcher = IKNR_PATTERN.matcher(iknr);
    if (!matcher.matches()) return false;
    val calcChecksum = calcChecksum(iknr);
    return getChecksum() == calcChecksum;
  }

  private int getChecksum() {
    val value = getValue();
    return Character.getNumericValue(value.charAt(value.length()-1));
  }

  private static int calcChecksum(String number) {
    var sum = 0;
    for(int i=7;  i>=2 ; i--) {
      var value = Character.getNumericValue(number.charAt(i));
      if(i % 2 == 0) value *=2;
      sum += WithChecksum.crossSum(value);
    }
    return sum % 10;
  }

  @Override
  public Identifier asIdentifier() {
    return this.asIdentifier(getSystem());
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
    return new Identifier().setSystem(system.getCanonicalUrl()).setValue(getValue());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    IKNR iknr = (IKNR) o;
    return Objects.equals(getValue(), iknr.getValue());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getValue());
  }
}
