/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.arguments;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PagingArgumentComposer {

  private static final String QUERY_KEY_COUNT = "_count";
  private static final String QUERY_KEY_OFFSET = "__offset";
  private static final String QUERY_KEY_SORT = "_sort";
  private static final String RELATION_LINK_FIRST = "first";
  private static final String COUNT_EQUALS_5 = "_count=5";

  public static ArgumentComposer queryComposerBigValues() {
    return ArgumentComposer.composeWith()
        .arguments(
            IQueryParameter.search().withCount(50).createParameter(),
            "_count=50",
            QUERY_KEY_COUNT,
            "next",
            "50")
        .arguments(
            IQueryParameter.search().withCount(30).withOffset(30).createParameter(),
            "_count=30&__offset=30",
            QUERY_KEY_COUNT,
            "prev",
            "30")
        .arguments(
            IQueryParameter.search().withCount(10).withOffset(20).createParameter(),
            "_count=10",
            QUERY_KEY_COUNT,
            RELATION_LINK_FIRST,
            "10")
        .arguments(
            IQueryParameter.search().withCount(5).createParameter(),
            COUNT_EQUALS_5,
            QUERY_KEY_COUNT,
            "self",
            "5")
        .arguments(
            IQueryParameter.search().withOffset(30).createParameter(),
            "__offset=30",
            QUERY_KEY_COUNT,
            RELATION_LINK_FIRST,
            "50")
        .arguments(
            IQueryParameter.search().sortedBy("date", SortOrder.DESCENDING).createParameter(),
            "_sort=-date",
            QUERY_KEY_SORT,
            "next",
            "-date")
        .arguments(
            IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter(),
            "_sort=date",
            QUERY_KEY_SORT,
            "self",
            "date")
        .arguments(
            IQueryParameter.search().withCount(30).withOffset(5).createParameter(),
            "_count=30&__offset=5",
            QUERY_KEY_COUNT,
            "prev",
            "5")
        .arguments(
            IQueryParameter.search().withCount(30).withOffset(5).createParameter(),
            "_count=30&__offset=5",
            QUERY_KEY_COUNT,
            "next",
            "30");
  }

  public static ArgumentComposer queryComposerSmallValues() {
    return ArgumentComposer.composeWith()
        .arguments(
            IQueryParameter.search().withCount(2).createParameter(),
            "_count=2",
            QUERY_KEY_COUNT,
            "next",
            "2")
        .arguments(
            IQueryParameter.search().withCount(3).withOffset(3).createParameter(),
            "_count=3&__offset=3",
            QUERY_KEY_COUNT,
            "prev",
            "3")
        .arguments(
            IQueryParameter.search().withCount(1).createParameter(),
            "_count=1",
            QUERY_KEY_COUNT,
            RELATION_LINK_FIRST,
            "1")
        .arguments(
            IQueryParameter.search().withCount(5).createParameter(),
            COUNT_EQUALS_5,
            QUERY_KEY_COUNT,
            "self",
            "5")
        .arguments(
            IQueryParameter.search().withOffset(4).createParameter(),
            "__offset=4",
            QUERY_KEY_OFFSET,
            "self",
            "4")
        .arguments(
            IQueryParameter.search().withCount(6).withOffset(6).createParameter(),
            "_count=6&__offset=6",
            QUERY_KEY_OFFSET,
            RELATION_LINK_FIRST,
            "0")
        .arguments(
            IQueryParameter.search()
                .sortedBy("expiry-date", SortOrder.DESCENDING)
                .createParameter(),
            "_sort=-expiry-date",
            QUERY_KEY_SORT,
            "next",
            "-expiry-date")
        .arguments(
            IQueryParameter.search().sortedBy("accept-date", SortOrder.ASCENDING).createParameter(),
            "_sort=accept-date",
            QUERY_KEY_SORT,
            "self",
            "accept-date")
        .arguments(
            IQueryParameter.search().sortedBy("modified", SortOrder.ASCENDING).createParameter(),
            "_sort=modified",
            QUERY_KEY_SORT,
            "self",
            "modified")
        .arguments(
            IQueryParameter.search().withCount(5).withOffset(8).createParameter(),
            "_count=8&__offset=5",
            QUERY_KEY_COUNT,
            "prev",
            "5");
  }

  public static ArgumentComposer queryComposerSmallValuesForCommunication() {
    return ArgumentComposer.composeWith()
        .arguments(
            IQueryParameter.search().withCount(2).createParameter(),
            "_count=2",
            QUERY_KEY_COUNT,
            "next",
            "2")
        .arguments(
            IQueryParameter.search().withCount(3).withOffset(3).createParameter(),
            "_count=3&__offset=3",
            QUERY_KEY_COUNT,
            "prev",
            "3")
        .arguments(
            IQueryParameter.search().withCount(1).createParameter(),
            "_count=1",
            QUERY_KEY_COUNT,
            RELATION_LINK_FIRST,
            "1")
        .arguments(
            IQueryParameter.search().withCount(5).createParameter(),
            COUNT_EQUALS_5,
            QUERY_KEY_COUNT,
            "self",
            "5")
        .arguments(
            IQueryParameter.search().withOffset(4).createParameter(),
            "__offset=4",
            QUERY_KEY_OFFSET,
            "self",
            "4")
        .arguments(
            IQueryParameter.search().withCount(6).withOffset(6).createParameter(),
            "_count=6&__offset=6",
            QUERY_KEY_OFFSET,
            RELATION_LINK_FIRST,
            "0")
        .arguments(
            IQueryParameter.search().sortedBy("received", SortOrder.DESCENDING).createParameter(),
            "_sort=-received",
            QUERY_KEY_SORT,
            "next",
            "-received")
        .arguments(
            IQueryParameter.search().sortedBy("recipient", SortOrder.ASCENDING).createParameter(),
            "_sort=recipient",
            QUERY_KEY_SORT,
            "self",
            "recipient")
        .arguments(
            IQueryParameter.search().sortedBy("identifier", SortOrder.ASCENDING).createParameter(),
            "_sort=identifier",
            QUERY_KEY_SORT,
            "self",
            "identifier")
        .arguments(
            IQueryParameter.search().withCount(5).withOffset(8).createParameter(),
            "_count=8&__offset=5",
            QUERY_KEY_COUNT,
            "prev",
            "5");
  }
}
