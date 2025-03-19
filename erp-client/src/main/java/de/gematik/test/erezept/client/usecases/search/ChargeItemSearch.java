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

package de.gematik.test.erezept.client.usecases.search;

import de.gematik.test.erezept.client.rest.param.*;
import de.gematik.test.erezept.client.usecases.ChargeItemGetCommand;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ChargeItemSearch {

  public static ChargeItemGetCommand getChargeItems(SortOrder order) {
    return searchFor().sortedByEnteredDate(order);
  }

  public static ChargeItemGetCommand getLatestChargeItems() {
    return getChargeItems(SortOrder.ASCENDING);
  }

  public static ChargeItemGetCommand getLastUpdated(LocalDate date, SearchPrefix searchPrefix) {
    return searchFor().sortedByLastUpdatedDate(date, searchPrefix);
  }

  public static Builder searchFor() {
    return new Builder();
  }

  public static class Builder {
    List<IQueryParameter> searchParams = new LinkedList<>();

    public ChargeItemGetCommand build() {
      return new ChargeItemGetCommand(searchParams);
    }

    public Builder withQuery(List<IQueryParameter> queryParameter) {
      searchParams.addAll(queryParameter);
      return this;
    }

    public ChargeItemGetCommand sortedByEnteredDate(SortOrder order) {
      searchParams.add(new SortParameter("entered-date", order));
      return build();
    }

    public ChargeItemGetCommand sortedByLastUpdatedDate(LocalDate date, SearchPrefix searchPrefix) {
      searchParams.add(
          new QueryParameter(
              "lastUpdated", searchPrefix.value() + date.format(DateTimeFormatter.ISO_LOCAL_DATE)));
      return build();
    }
  }
}
