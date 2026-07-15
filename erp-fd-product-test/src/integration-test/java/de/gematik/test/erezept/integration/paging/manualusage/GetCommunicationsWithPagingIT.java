/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.integration.paging.manualusage;

import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actions.communication.GetMessages;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.arguments.PagingArgumentComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.CommunicationGetCommand;
import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@net.jcip.annotations.NotThreadSafe
@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Communication PagingTests")
@Tag("ManualPagingTests")
class GetCommunicationsWithPagingIT extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  private static final String QUERY_KEY_SORT = "_sort";

  private static Stream<Arguments> actorComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "Patient",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPatientNamed("Hanna Bäcker"))
        .arguments(
            "Apotheke",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPharmacyNamed("Am Flughafen"))
        .create();
  }

  public static Stream<Arguments> communicationBundleQueryComposer() {
    return PagingArgumentComposer.queryComposerSmallValuesForCommunication()
        .arguments(
            IQueryParameter.search().withOffset(4).withCount(4).createParameter(),
            "_count=4&__offset=4",
            "_count",
            "last",
            "4")
        .arguments(
            IQueryParameter.search()
                .sortedBy("received", SortOrder.DESCENDING)
                .withOffset(10)
                .withCount(4)
                .createParameter(),
            "_sort=-received",
            QUERY_KEY_SORT,
            "next",
            "-received")
        .arguments(
            IQueryParameter.search()
                .sortedBy("recipient", SortOrder.ASCENDING)
                .withOffset(10)
                .withCount(4)
                .createParameter(),
            "_sort=recipient",
            QUERY_KEY_SORT,
            "self",
            "recipient")
        .arguments(
            IQueryParameter.search()
                .sortedBy("identifier", SortOrder.ASCENDING)
                .withOffset(10)
                .withCount(4)
                .createParameter(),
            "_sort=identifier",
            QUERY_KEY_SORT,
            "self",
            "identifier")
        .create();
  }

  private static Stream<Arguments> actorComposerWithCount() {
    return ArgumentComposer.composeWith()
        .arguments(
            2,
            "Patient",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPatientNamed("Hanna Bäcker"))
        .arguments(
            1,
            "Patient",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPatientNamed("Hanna Bäcker"))
        .arguments(
            4,
            "Patient",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPatientNamed("Hanna Bäcker"))
        .create();
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe bei Communications, dass beim Paging als {0} der RelationLink self"
              + " funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei Communications funktioniert. Speziell der"
          + " self Link ")
  @MethodSource("actorComposer")
  void getSelfLinkInCommunications(String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);

    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getAllCommunications(SortOrder.DESCENDING)));
    assertTrue(
        firstCall.getExpectedResponse().hasSelfRelation(),
        "given CommunicationBundle has to have a Self-Relation-Link");
    val secondCall = actor.performs(DownloadBundle.selfFor(firstCall.getExpectedResponse()));

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(secondCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe bei Communications, dass das Paging als {0} alle 5 Relation-Links zur"
              + " verfügung stehen")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei Communications funktioniert. Speziell, dass"
          + " für Abrufende alle fünf RelationLinks vorhanden sind ")
  @MethodSource("actorComposer")
  void checkInCommunicationBundleForRelationLinksAsActor(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);

    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 6);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 6);
    }

    val queries =
        IQueryParameter.search()
            .withOffset(3)
            .withCount(2)
            .sortedByDate(SortOrder.ASCENDING)
            .createParameter();
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(CommunicationSearch.withAdditionalQuery(queries)));

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(containsAll5Links())
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_Communication_PAGING_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von Communications als Apotheke mit URL-Parameter {1}, erwartet im"
              + " RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss sichergestellt werden, dass in den Link-Relation für Apotheken die Clientseitig"
          + " verwendeten Filter und Suchkriterien wiederverwendet werden")
  @MethodSource("communicationBundleQueryComposer")
  void backendShouldUseClientsParamsAsPharmacy(
      List<IQueryParameter> iQueryParameters,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {
    val task = doctor.prescribeFor(patient);
    sendMultipleDispenseRequestsAndCount(task, 20);
    val firstCall =
        flughafenApo.performs(
            GetMessages.fromServerWith(CommunicationSearch.withAdditionalQuery(iQueryParameters)));
    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von Communications als Patient mit URL-Parameter {1}, erwartet im"
              + " RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss sichergestellt werden, dass in den Link-Relation für Patienten die Clientseitig"
          + " verwendeten Filter und Suchkriterien wiederverwendet werden")
  @MethodSource("communicationBundleQueryComposer")
  void backendShouldUseClientsParamsAsPatient(
      List<IQueryParameter> iQueryParameters,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {

    val task = doctor.prescribeFor(patient);
    sendMultipleReplyAndCount(task, 20);

    val firstCall =
        patient.performs(
            GetMessages.fromServerWith(CommunicationSearch.withAdditionalQuery(iQueryParameters)));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von CommunicationBundles als {0} muss der __offset URL-Parameter"
              + " geprüft werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundles funktioniert. Speziell"
          + " der __offset URL-Parameter")
  @MethodSource("actorComposer")
  void shouldGetCommunicationBundlesWithFixStartIndex(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);

    val pagingArgument =
        IQueryParameter.search().withOffset(5).sortedByDate(SortOrder.ASCENDING).createParameter();

    val firstCall =
        actor.performs(GetMessages.fromServerWith(new CommunicationGetCommand(pagingArgument)));

    val secondCall =
        actor.performs(GetMessages.fromServerWith(new CommunicationGetCommand(pagingArgument)));

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(hasSameEntryIds(secondCall.getExpectedResponse(), ErpAfos.A_24441))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von CommunicationBundles als {0} muss der __offset URL-Parameter"
              + " mit unterschiedlichen Werten für den Startpunkt überprüft werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundles funktioniert. Speziell"
          + " der __offset URL-Parameter")
  @MethodSource("actorComposer")
  void shouldGetCommunicationsWithDifferentOffset(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);
    val parameters =
        IQueryParameter.search().withOffset(5).sortedByDate(SortOrder.ASCENDING).createParameter();

    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 10);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 10);
    }

    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(CommunicationSearch.withAdditionalQuery(parameters)));

    val secondCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withOffset(10)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));
    val expectedCommunication = secondCall.getExpectedResponse().getCommunications().get(0);
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(hasElementAtPosition(expectedCommunication, 5))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von CommunicationBundles als {0} muss der __offset und _count"
              + " URL-Parameter in Kombination überprüft werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundles funktioniert. Speziell"
          + " der __offset und _count URL-Parameter in Kombination")
  @MethodSource("actorComposer")
  void shouldGetCommunicationBundlesWithDifferentOffsetAndCount(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);

    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 10);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 10);
    }
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(4)
                        .withOffset(0)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));

    val secondCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(1)
                        .withOffset(3)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                hasElementAtPosition(
                    secondCall.getExpectedResponse().getCommunications().getFirst(), 3))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von CommunicationBundles als {0} muss der __offset und"
              + " Default URL-Parameter (__offset=0) ")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundles funktioniert. Speziell"
          + " der __offset Default URL-Parameter (__offset=0)")
  @MethodSource("actorComposer")
  void shouldGetCommunicationBundlesWithDefaultOffsetAndCount(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);
    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 15);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 15);
    }
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search().sortedByDate(SortOrder.ASCENDING).createParameter())));

    val secondBundleInteraction =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withOffset(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));

    // Note: this will prevent ArrayIndexOutOfBoundException, this test requires a proper
    // pre-condition!
    val communications = firstCall.getExpectedResponse().getCommunications();
    val expected =
        communications.size() > 9
            ? firstCall.getExpectedResponse().getCommunications().get(9)
            : new ErxCommunication(); // just a dummy because we don't have enough communications!

    actor.attemptsTo(
        Verify.that(secondBundleInteraction)
            .withExpectedType()
            .has(minimumCountOfEntries(5))
            .and(hasElementAtPosition(expected, 4))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_09")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe, dass bei Paging von CommunicationBundles als {0} die RelationLink"
              + " next und previous funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundles funktioniert. Genauer"
          + " die RelationLink next und previous")
  @MethodSource("actorComposer")
  void getCommunicationBundlesWhileUsingRelationNextAndPrevious(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 15);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 15);
    }

    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .sortedByDate(SortOrder.ASCENDING)
                        .withCount(3)
                        .createParameter())));

    assertTrue(
        firstCall.getExpectedResponse().hasNextRelation(),
        "given first CommunicationBundle has to have a next-Relation-Link");

    val secondCall = actor.performs(DownloadBundle.nextFor(firstCall.getExpectedResponse()));
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .isCorrect());
    assertTrue(
        secondCall.getExpectedResponse().hasPreviousRelation(),
        "given second CommunicationBundle has to have a previous-Relation-Link");
    val firstFromSecondCall =
        actor.performs(DownloadBundle.previousFor(secondCall.getExpectedResponse()));
    actor.attemptsTo(
        Verify.that(firstFromSecondCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(hasSameEntryIds(firstFromSecondCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_10")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {1} muss überprüft werden, ob der Total Count im CommunicationBundle um"
              + " {0} erhöht wird")
  @DisplayName(
      "Es muss sichergestellt werden, dass der Total-Count um den folgenden Wert"
          + " erhöht wird: ")
  @MethodSource("actorComposerWithCount")
  void shouldAdaptTotalCount(
      int count, String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val task = doctor.prescribeFor(patient);
    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search().sortedByDate(SortOrder.ASCENDING).createParameter())));
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, count);
    } else {
      sendMultipleDispenseRequestsAndCount(task, count);
    }
    val secondCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search().sortedByDate(SortOrder.ASCENDING).createParameter())));
    actor.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .and(containsTotalCountOf(firstCall.getExpectedResponse().getTotal() + count))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_11")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob  Equals für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundl funktioniert. Genauer,"
          + " dass der Filteroperator Equals Sent für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateEqualSendFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 10);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 10);
    }

    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasSent(LocalDate.now(), SearchPrefix.EQ)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(sentDateIsEqual(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_12")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob  Equals für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator before Sent für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateLowerThanSentFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);

    val task = doctor.prescribeFor(patient);
    if (actor.getType().equals(ActorType.PATIENT)) {
      sendMultipleReplyAndCount(task, 10);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 10);
    }
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasSent(LocalDate.now(), SearchPrefix.LT)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(verifySentDateIsBefore(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_13")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob  after Send für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator after Sent für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateGraterThanSentFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasSent(LocalDate.now(), SearchPrefix.GT)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(verifySentDateIsAfter(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_14")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob  Equals Received für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator Equals Received für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateEqualReceivedFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasReceived(LocalDate.now(), SearchPrefix.EQ)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(receivedDateIsEqualTo(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_15")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob  Equals Received für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator before Received für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateLowerThanReceivedFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasReceived(LocalDate.now(), SearchPrefix.LT)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyReceivedDateWithPredicate(
                    ld -> ld.isBefore(LocalDate.now()),
                    format(
                        "das enthaltene Communication.received datum muss vor {0} liegen ",
                        LocalDate.now())))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_16")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob after Received für Datumsabfragen im"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator after Received für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleWhileUsingDateGraterThanReceivedFilter(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .wasReceived(LocalDate.now(), SearchPrefix.GT)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyReceivedDateWithPredicate(
                    ld -> ld.isAfter(LocalDate.now()),
                    format(
                        "das enthaltene Communication.received datum muss nach {0} liegen ",
                        LocalDate.now())))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_17")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob der Filter für recipient in Get"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator Recipient für Datumsabfragen funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleFilteredByRecipient(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val id =
        actor.getType().equals(ActorType.PATIENT)
            ? ((PatientActor) actor).getKvnr().getValue()
            : ((PharmacyActor) actor).getTelematikId().getValue();
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .andRecipient(id)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(containsOnlyRecipientWith(id, ErpAfos.A_24436))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_18")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob der Defaultwert sent in Get"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Defaultwert sent funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleFilteredByDefault(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val actor = actorProvider.apply(this);
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search().createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall).withExpectedType().and(verifySentDateIsSortedAscend()).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_19")
  @ParameterizedTest(
      name =
          "[{index}] -> Als {0} muss überprüft werden, ob der Filter für sender in Get"
              + " CommunicationBundle funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei CommunicationBundle funktioniert. Genauer,"
          + " dass der Filteroperator Sender funktionieren")
  @MethodSource("actorComposer")
  void getCommunicationBundleFilteredBySender(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {

    val task = doctor.prescribeFor(patient);
    if (actorType.equals("Patient")) {
      sendMultipleDispenseRequestsAndCount(task, 5);
    } else {
      sendMultipleReplyAndCount(task, 5);
    }

    val actor = actorProvider.apply(this);
    val id =
        actor.getType().equals(ActorType.PATIENT)
            ? ((PatientActor) actor).getKvnr().getValue()
            : ((PharmacyActor) actor).getTelematikId().getValue();
    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withCount(5)
                        .sortedByDate(SortOrder.ASCENDING)
                        .hasSender(id)
                        .createParameter())));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .has(onlySenderWith(id, ErpAfos.A_24436))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_PAGING_20")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe bei Communications, dass beim Paging als {0} alle RelationLink"
              + " verfügbar sind bei Verwendung der RelationLinks")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei Communications funktioniert. Speziell die"
          + " Bereitstellung aller RelationLinks während ihrer Anwendung")
  @MethodSource("actorComposer")
  void responseOfUsingRelationLinksHasToHaveAllRelationLinks(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    val actor = actorProvider.apply(this);

    val task = doctor.prescribeFor(patient);
    if (actorType.equals("Patient")) {
      sendMultipleReplyAndCount(task, 10);
    } else {
      sendMultipleDispenseRequestsAndCount(task, 10);
    }

    val firstCall =
        actor.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.searchFor()
                    .specificQuery(
                        IQueryParameter.search().withCount(2).withOffset(5).createParameter())
                    .sortedBySendDate(SortOrder.DESCENDING)));

    assertTrue(
        firstCall.getExpectedResponse().hasNextRelation(),
        "given CommunicationBundle has to have a NEXT-Relation-Link");
    val secondCall = actor.performs(DownloadBundle.nextFor(firstCall.getExpectedResponse()));

    val thirdCall = actor.performs(DownloadBundle.nextFor(secondCall.getExpectedResponse()));
    actor.attemptsTo(
        Verify.that(thirdCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24443))
            .and(containsAll5Links())
            .isCorrect());
  }

  private void sendMultipleDispenseRequestsAndCount(ErxTask task, int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      val request =
          CommunicationDisReqMessage.forV3()
              .communicationType(CommunicationType.ORDER.getLabel())
              .supplyOptionsType(SupplyOptionsType.DELIVERY)
              .transactionID(UUID.randomUUID())
              .firstname("John")
              .lastname("Doe")
              .phone("+49170123456")
              .text(
                  format(
                      "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Ist"
                          + " das Medikament No.1 heute noch verfügbar, liebe Apo"
                          + " woodlandPharma?",
                      i))
              .build();
      patient.performs(SendMessages.to(flughafenApo).forTask(task).asDispenseRequest(request));
    }
  }

  private void sendMultipleReplyAndCount(ErxTask task, int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      val replay =
          CommunicationReplyMessage.forV3()
              .communicationType(CommunicationType.PAYMENT_INFO.getLabel())
              .transactionID(UUID.randomUUID())
              .totalAmount(12550)
              .paymentMethods(
                  List.of(
                      new CommunicationReplyMessage.PaymentMethod(
                          "creditcard", "Visa", "https://pay.example.com")))
              .text(
                  format(
                      "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey"
                          + " patient, how are you? does the medicine takes an effect??",
                      i))
              .build();

      flughafenApo.performs(SendMessages.to(patient).forTask(task).asReply(replay, flughafenApo));
    }
  }
}
