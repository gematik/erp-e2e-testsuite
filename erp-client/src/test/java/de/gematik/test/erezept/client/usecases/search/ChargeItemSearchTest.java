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

import static org.junit.Assert.assertTrue;

import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import java.time.LocalDate;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChargeItemSearchTest {

  @Test
  void shouldGetNew() {
    val cmd = ChargeItemSearch.getChargeItems(SortOrder.DESCENDING);
    assertTrue(cmd.getRequestLocator().contains("/ChargeItem?_sort=-entered-date"));
  }

  @Test
  void shouldGetLatest() {
    val cmd = ChargeItemSearch.getLatestChargeItems();
    assertTrue(cmd.getRequestLocator().contains("_sort=entered-date"));
  }

  @Test
  void shouldGetLastUpdatet() {
    val cmd = ChargeItemSearch.getLastUpdated(LocalDate.now(), SearchPrefix.EB);
    assertTrue(cmd.getRequestLocator().contains("lastUpdated=eb"));
  }

  @Test
  void shouldGetWithQuery() {
    val cmd =
        ChargeItemSearch.searchFor()
            .withQuery(IQueryParameter.search().withCount(2).createParameter())
            .build();
    assertTrue(cmd.getRequestLocator().contains("_count=2"));
  }
}
