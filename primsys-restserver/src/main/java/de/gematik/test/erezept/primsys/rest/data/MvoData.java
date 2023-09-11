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

package de.gematik.test.erezept.primsys.rest.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.time.LocalDate;
import lombok.Data;
import lombok.val;

@Data
public class MvoData {
  private Integer numerator;
  private Integer denominator;

  private LocalDate startDate;

  private LocalDate endDate;

  @JsonIgnore
  public boolean isValid() {
    return !(numerator == null
        || denominator == null
        || startDate == null
        || numerator < 1
        || numerator > denominator
        || denominator < 2
        || numerator > 4
        || denominator > 4);
  }

  public MvoData fakeMvoNumeDenomAndDates() {
    val mvo = GemFaker.mvo(true);
    denominator = mvo.getDenominator();
    numerator = mvo.getNumerator();
    startDate = LocalDate.now();
    endDate = startDate.plusDays(100);
    return this;
  }

  public boolean hasEndDate() {
    return (endDate != null);
  }
}
