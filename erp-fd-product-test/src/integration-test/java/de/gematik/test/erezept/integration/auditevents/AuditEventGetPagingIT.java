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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.integration.auditevents;

import static de.gematik.test.core.expectations.verifier.AuditEventVerifier.hasAuditEventAtPosition;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static org.junit.Assert.assertTrue;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.arguments.PagingArgumentComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("AuditEvents PagingTests")
@Tag("AuditEvent_Paging")
public class AuditEventGetPagingIT extends ErpTest {
  private static final int REQUIRED_TOTAL_COUNT_0 = 0;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  public static Stream<Arguments> auditEventQueryComposer() {
    return PagingArgumentComposer.queryComposerBigValues().create();
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_01")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei AuditEvents funktioniert. Speziell der next"
          + " Link ")
  void shouldGetAuditEvent() {

    val auditEventQueryParam =
        IQueryParameter.search()
            .withOffset(10)
            .withCount(5)
            .sortedBy("date", SortOrder.ASCENDING)
            .createParameter();
    val firstAuditEventBundle =
        sina.performs(DownloadAuditEvent.withQueryParams(auditEventQueryParam))
            .getExpectedResponse();
    assertTrue("firstEventBundle has to have a next Link", firstAuditEventBundle.hasNextRelation());
    val secondAuditEventBundleByRelationLinkCall =
        sina.performs(DownloadBundle.nextFor(firstAuditEventBundle));
    assertTrue(
        "secondAuditEventBundle has to have a next Link",
        secondAuditEventBundleByRelationLinkCall.getExpectedResponse().hasNextRelation());

    val secondAuditEventBundleAsDirectCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(14)
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    sina.attemptsTo(
        Verify.that(secondAuditEventBundleAsDirectCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());
    sina.attemptsTo(
        Verify.that(secondAuditEventBundleByRelationLinkCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                hasSameEntryIds(
                    secondAuditEventBundleAsDirectCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_02")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei AuditEvents funktioniert. Speziell der"
          + " __offset URL-Parameter")
  void shouldGetAuditEventWith_offset() {
    val auditEventQueryParam =
        IQueryParameter.search()
            .withOffset(5)
            .sortedBy("date", SortOrder.ASCENDING)
            .createParameter();
    val firstCall = sina.performs(DownloadAuditEvent.withQueryParams(auditEventQueryParam));
    val secondCall = sina.performs(DownloadAuditEvent.withQueryParams(auditEventQueryParam));

    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(hasSameEntryIds(secondCall.getExpectedResponse(), ErpAfos.A_24441))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_03")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei AuditEvents funktioniert. Speziell der"
          + " __offset URL-Parameter mit unterschiedlichen Werten")
  void shouldGetAuditEventWithDifferent_offset() {
    val firstCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    val secondCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(10)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));

    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                hasAuditEventAtPosition(
                    secondCall.getExpectedResponse().getAuditEvents().get(0), 5))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_04")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei AuditEvents funktioniert. Speziell der"
          + " _offset und _count URL-Parameter in Kombination")
  void shouldGetAuditEventWithDifferent_offsetAnd_count() {
    val firstCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withCount(10)
                    .withOffset(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));

    val secondCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withCount(5)
                    .withOffset(10)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                hasAuditEventAtPosition(
                    secondCall.getExpectedResponse().getAuditEvents().get(4), 9))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_05")
  @Test
  @DisplayName("Es muss sichergestellt werden,dass alle 4 Link-Relations enthalten sind")
  void shouldHaveAllRequiredRelationLinks() {
    val auditEventQueryParam =
        IQueryParameter.search()
            .withCount(5)
            .withOffset(15)
            .sortedBy("date", SortOrder.ASCENDING)
            .createParameter();
    val firstCall = sina.performs(DownloadAuditEvent.withQueryParams(auditEventQueryParam));

    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(containsCountOfGivenLinks(List.of("next", "prev", "self", "first"), 4L))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_06")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei AuditEvents funktioniert. Speziell der"
          + " Default Parameter (__offset=0)")
  void shouldGetAuditEventWith_count() {
    val firstAuditEventBundleInteraction =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter()));

    sina.attemptsTo(Verify.that(firstAuditEventBundleInteraction).withExpectedType().isCorrect());

    val secondAuditEventBundleInteraction =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    sina.attemptsTo(
        Verify.that(secondAuditEventBundleInteraction)
            .withExpectedType()
            .and(containsEntriesOfCount(50))
            .and(
                hasAuditEventAtPosition(
                    firstAuditEventBundleInteraction.getExpectedResponse().getAuditEvents().get(9),
                    4))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_07")
  @ParameterizedTest
  @ValueSource(ints = {1, 5, 100})
  @DisplayName("Es muss sichergestellt werden, dass der Total-Count immer 0 ist")
  void shouldHaveTotalCount0(int quantity) {
    val firstCall =
        sina.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withCount(quantity)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(containsTotalCountOf(REQUIRED_TOTAL_COUNT_0))
            .isCorrect());
  }

  @TestcaseId("ERP_AUDIT_EVENT_PAGING_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von AuditEventBundles als Patient mit URL-Parameter {1}, erwartet"
              + " im RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss sichergestellt werden, dass in den Link-Relation die Clientseitig verwendeten Filter"
          + " und Suchkriterien verwendet werden")
  @MethodSource("auditEventQueryComposer")
  void backendShouldUseClientsParams(
      List<IQueryParameter> queryParam,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {
    val firstCall = sina.performs(DownloadAuditEvent.withQueryParams(queryParam));

    sina.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }
}
