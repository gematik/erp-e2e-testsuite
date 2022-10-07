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

package de.gematik.test.erezept.fhir.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.DecimalType;
import org.hl7.fhir.r4.model.Money;

@Getter
@AllArgsConstructor
public enum Currency {
  EUR("EUR", "€", "Euro");

  private final String code;
  private final String symbol;
  private final String name;

  public Money asMoney(float value) {
    val money = new Money();
    money.setValueElement(asDecimalType(value));
    money.setCurrency(this.getCode());
    return money;
  }

  /**
   * Currency value are always formatted with 2 decimal places
   *
   * @param value to format
   * @return formatted DecimalType
   */
  public static DecimalType asDecimalType(float value) {
    val formatted = String.format(java.util.Locale.US, "%.02f", value);
    return new DecimalType(formatted);
  }
}
