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

package de.gematik.test.erezept.client.rest.param;

import static de.gematik.test.erezept.client.rest.param.IQueryParameter.queryListFromUrl;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.values.TelematikID;
import java.time.LocalDate;
import java.time.Month;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class IQueryParameterTest {

  @Test
  void shouldFindAllQueries() {
    val result =
        queryListFromUrl(
            "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/AuditEvent?_count=50&_offset=45&_id=gt01eb9373-789e-7db8-0000-000000000000");
    assertEquals(
        "50",
        result.stream()
            .filter(qp -> qp.parameter().equals("_count"))
            .findFirst()
            .orElseThrow()
            .value());
  }

  @Test
  void shouldFindNoQuery() {
    val result = queryListFromUrl("https://erp-dev.zentral.erp.splitdns.ti-dienste.de");
    assertEquals(0, result.size());
  }

  @Test
  void shouldFindAllQueries2() {
    val result =
        queryListFromUrl(
            "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/AuditEvent?_count=50&_offset=45&_id=gt01eb9373-789e-7db8-0000-000000000000");
    assertEquals(
        "50",
        result.stream()
            .filter(qp -> qp.parameter().equals("_count"))
            .findFirst()
            .orElseThrow()
            .value());
  }

  @Test
  void shouldFindAllQueries3() {
    val result =
        queryListFromUrl(
            "https://erp-dev.zentral.erp.splitdns.ti-dienste.de/AuditEvent?_count=50&_offset=45&_id=gt01eb9373-789e-7db8-0000-000000000000");
    assertEquals(
        "gt01eb9373-789e-7db8-0000-000000000000",
        result.stream()
            .filter(qp -> qp.parameter().equals("_id"))
            .findFirst()
            .orElseThrow()
            .value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithOffSet() {
    val iQP = IQueryParameter.search().withOffset(5).createParameter();
    assertTrue(iQP.get(0).parameter().contains("__offset"));
    assertTrue(iQP.get(0).value().contains("5"));
  }

  @Test
  void shouldBuildBundlePagingQueryWithCount() {
    val iQP = IQueryParameter.search().withCount(4).createParameter();
    assertTrue(iQP.get(0).parameter().contains("_count"));
    assertTrue(iQP.get(0).value().contains("4"));
  }

  @Test
  void shouldBuildBundlePagingQueryWithSort() {
    val iQP = IQueryParameter.search().sortedBy("date", SortOrder.DESCENDING).createParameter();
    Assertions.assertEquals("_sort", iQP.get(0).parameter());
    Assertions.assertEquals("-date", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQuerAuthoredOn() {
    val iQP =
        IQueryParameter.search()
            .withAuthoredOnAndFilter(LocalDate.of(1, Month.JANUARY, 1), SearchPrefix.EQ)
            .createParameter();
    Assertions.assertEquals("authored-on", iQP.get(0).parameter());
    Assertions.assertEquals("eq0001-01-01", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithSortedBy() {
    val iQP = IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter();
    Assertions.assertEquals("_sort", iQP.get(0).parameter());
    Assertions.assertEquals("date", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithWasSent() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val iQP = IQueryParameter.search().wasSent(testDate, SearchPrefix.GT).createParameter();
    Assertions.assertEquals("sent", iQP.get(0).parameter());
    Assertions.assertEquals("gt2024-07-30", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithReceived() {
    val testDate = LocalDate.of(2024, Month.JULY, 30);
    val iQP = IQueryParameter.search().wasReceived(testDate, SearchPrefix.GT).createParameter();
    Assertions.assertEquals("received", iQP.get(0).parameter());
    Assertions.assertEquals("gt2024-07-30", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithSender() {
    val iQP = IQueryParameter.search().hasSender("id").createParameter();
    Assertions.assertEquals("sender", iQP.get(0).parameter());
    Assertions.assertEquals("id", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithIdentifier() {
    val iQP =
        IQueryParameter.search()
            .identifier(new Identifier().setSystem("123").setValue("456"))
            .createParameter();
    Assertions.assertEquals("identifier", iQP.get(0).parameter());
    Assertions.assertEquals("123|456", iQP.get(0).value());
  }

  @Test
  void shouldBuildBundlePagingQueryWithRecipient() {
    val iQP = IQueryParameter.search().andRecipient("id").createParameter();
    Assertions.assertEquals("recipient", iQP.get(0).parameter());
    Assertions.assertEquals("id", iQP.get(0).value());
  }

  @Test
  void shouldBuildSearchQueryWithWhenHandedOver() {
    val iQP =
        IQueryParameter.search()
            .whenHandedOver(SearchPrefix.EQ, LocalDate.of(1, Month.JANUARY, 1))
            .createParameter();
    Assertions.assertEquals("whenhandedover", iQP.get(0).parameter());
    Assertions.assertEquals("eq0001-01-01", iQP.get(0).value());
  }

  @Test
  void shouldBuildSearchQueryWithWhenPrepared() {
    val iQP =
        IQueryParameter.search()
            .whenPrepared(SearchPrefix.EQ, LocalDate.of(1, Month.JANUARY, 1))
            .createParameter();
    Assertions.assertEquals("whenprepared", iQP.get(0).parameter());
    Assertions.assertEquals("eq0001-01-01", iQP.get(0).value());
  }

  @Test
  void shouldBuildSearchQueryWithPerformer() {
    val iQP = IQueryParameter.search().fromPerformer(TelematikID.from("1")).createParameter();
    Assertions.assertEquals("performer", iQP.get(0).parameter());
    Assertions.assertEquals("1", iQP.get(0).value());
  }
}
