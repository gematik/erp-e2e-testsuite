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

package de.gematik.test.erezept.integration.paging;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.containsIdsOf;
import static de.gematik.test.core.expectations.verifier.MedicationDispenseBundleVerifier.verifyAllPerformerIdsAre;
import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.verifyAuthoredOnDateWithPredicate;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.vsdm.VsdmExamEvidence;
import de.gematik.bbriccs.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actions.communication.GetMessages;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

@net.jcip.annotations.NotThreadSafe
@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Paging Tests")
@Tag("Paging")
@SuppressWarnings("java:S8692")
class PagingUserJourneyIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Günther Angermänn")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  private List<ErxTask> postTasks(int numOfTasks) {
    LinkedList<ErxTask> erxTasks = new LinkedList<>();
    for (int i = 0; i < numOfTasks; i++) {
      erxTasks.add(doctor.prescribeFor(patient));
    }
    return erxTasks;
  }

  private void sendMultipleDispenseRequestsAndCount(ErxTask task, int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      patient.performs(
          SendMessages.to(flughafenApo)
              .forTask(task)
              .asDispenseRequest(
                  CommunicationDisReqMessage.forV3()
                      .supplyOptionsType(SupplyOptionsType.ON_PREMISE)
                      .hint(
                          format(
                              "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication:"
                                  + " Ist das Medikament No.1 heute noch verfügbar, liebe Apo"
                                  + " woodlandPharma?",
                              i))
                      .build()));
    }
  }

  private void sendMultipleReplyAndCount(ErxTask task, int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      flughafenApo.performs(
          SendMessages.to(patient)
              .forTask(task)
              .asReply(
                  CommunicationReplyMessage.forV3()
                      .communicationType(CommunicationType.TEXT.getLabel())
                      .transactionID(UUID.randomUUID())
                      .text(
                          format(
                              "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication:"
                                  + " Hey patient, how are you? does the medicine takes an"
                                  + " effect??",
                              i))
                      .build(),
                  flughafenApo));
    }
  }

  @TestcaseId("ERP_TASK_PAGING_01")
  @RepeatedTest(1)
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging und Filterung bei TaskBundles, Dispensations,"
          + " AuditEvents und Communications funktioniert.")
  void stakeholderPagingJourney() {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(config.getSoftKonnVsdmService(), patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val totalCount =
        patient
            .performs(
                DownloadReadyTask.asPatient(
                    IQueryParameter.search()
                        .sortedBy("date", SortOrder.ASCENDING)
                        .withCount(2)
                        .withOffset(2)
                        .createParameter()))
            .getExpectedResponse()
            .getTotal();
    val addNewTasks = 7;
    val testDate = LocalDate.now();

    // arzt verschreibt 5 Rezepte
    val tasks = postTasks(addNewTasks);

    // prüfung self-link
    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withCount(2)
                    .withOffset(2)
                    .createParameter()));
    // zweiter Aufruf sollte gleich dem firstCall sein
    val alternativeFirstCall =
        patient.performs(DownloadBundle.selfFor(firstCall.getExpectedResponse()));
    // used next relation link
    val secondCall = patient.performs(DownloadBundle.nextFor(firstCall.getExpectedResponse()));
    // used next relation link twice
    val thirdCall = patient.performs(DownloadBundle.nextFor(secondCall.getExpectedResponse()));
    // used previous relationLink
    val fourthCall = patient.performs(DownloadBundle.previousFor(thirdCall.getExpectedResponse()));
    // use lowerThan searchPrefix
    val lowerThanTasksCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(testDate, SearchPrefix.LT)
                    .createParameter()));

    val apoFirstCall = // prüfung self-link
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withCount(2)
                    .withOffset(2)
                    .createParameter()));
    val apoAlternativeCall =
        flughafenApo.performs(DownloadBundle.selfFor(apoFirstCall.getExpectedResponse()));
    val apoSecondCall =
        flughafenApo.performs(DownloadBundle.nextFor(apoFirstCall.getExpectedResponse()));

    List<ErxReceipt> dispensations = new LinkedList<>();

    for (ErxTask task : tasks) {
      val acceptation = flughafenApo.performs(AcceptPrescription.forTheTask(task));
      dispensations.add(
          flughafenApo
              .performs(
                  ClosePrescription.alternative()
                      .acceptedWith(
                          acceptation,
                          DateConverter.getInstance()
                              .localDateToDate(LocalDate.now().minusDays(1))))
              .getExpectedResponse());
    }

    val dispensationsCalledByPatient =
        patient.performs(
            GetMedicationDispense.withQueryParams(
                IQueryParameter.search()
                    .fromPerformer(flughafenApo.getTelematikId())
                    .whenPrepared(SearchPrefix.EQ, LocalDate.now().minusDays(1))
                    .createParameter()));

    val firstAuditEventBundle =
        patient.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(10)
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));
    val secondAuditEventBundleByRelationLinkCall =
        patient.performs(DownloadBundle.nextFor(firstAuditEventBundle.getExpectedResponse()));
    val secondAuditEventBundleAsDirectCall =
        patient.performs(
            DownloadAuditEvent.withQueryParams(
                IQueryParameter.search()
                    .withOffset(14)
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .createParameter()));

    // Communications
    val communicationMultiplyer = 2;

    for (ErxTask task : tasks) {
      sendMultipleDispenseRequestsAndCount(task, 2);
      sendMultipleReplyAndCount(task, 2);
    }

    val firstComCall =
        patient.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withOffset(5)
                        .withCount(3)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));
    val selfForFirstComCall =
        patient.performs(DownloadBundle.selfFor(firstComCall.getExpectedResponse()));
    val secondComCall =
        patient.performs(DownloadBundle.nextFor(firstComCall.getExpectedResponse()));
    val firstFromSecondComCall =
        patient.performs(DownloadBundle.previousFor(secondComCall.getExpectedResponse()));

    val comCallWithFilter =
        patient.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withOffset(7)
                        .withCount(1)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));

    val aposFirstComCall =
        flughafenApo.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.withAdditionalQuery(
                    IQueryParameter.search()
                        .withOffset(3)
                        .withCount(2)
                        .sortedByDate(SortOrder.ASCENDING)
                        .createParameter())));

    val aposSecondComCall =
        flughafenApo.performs(DownloadBundle.nextFor(aposFirstComCall.getExpectedResponse()));
    val aposFirstFromSecondComCall =
        flughafenApo.performs(DownloadBundle.previousFor(aposSecondComCall.getExpectedResponse()));

    // TASK - Prüfung self-link
    assertTrue(
        firstCall.getExpectedResponse().hasSelfRelation(),
        "given TaskBundle has to have a Self-Relation-Link");
    // prüfe, dass der Self-relation-link korrekt verarbeitet wird
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(alternativeFirstCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
    // prüfe, dass paging vor und wieder zurück, zum gleichen ergebnis führt
    patient.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(fourthCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
    // prüfe, dass alle 5 links vorhanden sind und totalCount korrekt gezählt wurde
    patient.attemptsTo(
        Verify.that(thirdCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(containsAll5Links())
            .and(containsTotalCountOf(totalCount + addNewTasks))
            .and(expectedParamsIn("self", "__offset", "6"))
            .and(expectedParamsIn("prev", "__offset", "4"))
            .and(expectedParamsIn("first", "__offset", "0"))
            .isCorrect());

    // überprüfe, dass das lowerThan 'lt' search-prefix funktioniert
    patient.attemptsTo(
        Verify.that(lowerThanTasksCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> ld.isBefore(testDate),
                    format(
                        "Die enthaltenen Tasks müssen ein früheres AuthoredOn Datum als {0}"
                            + " enthalten",
                        testDate)))
            .isCorrect());

    // verifySelfLink Apo
    flughafenApo.attemptsTo(
        Verify.that(apoFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(apoAlternativeCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());

    // Verify RelationLinks, Offset-Values and totalCount of Apos call
    flughafenApo.attemptsTo(
        Verify.that(apoSecondCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(containsAll5Links())
            .and(expectedParamsIn("self", "__offset", "4"))
            .and(expectedParamsIn("prev", "__offset", "2"))
            .and(expectedParamsIn("first", "__offset", "0"))
            .isCorrect());

    // Dispensation Paging
    // validate that the patient gets all dispensed prescriptions + from specific performer +
    // totalCount-check + non relationlinks
    patient.attemptsTo(
        Verify.that(dispensationsCalledByPatient)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(minimumCountOfEntries(dispensations.size()))
            .and(containsIdsOf(dispensations, ErpAfos.A_24436))
            .and(verifyAllPerformerIdsAre(flughafenApo.getTelematikId()))
            .and(containsTotalCountOf(0))
            // (0 von 5 möglichen)
            .and(containsCountOfGivenLinks(List.of("next", "prev", "self", "first", "last"), 0L))
            .isCorrect());

    // auditEventPaging
    patient.attemptsTo(
        Verify.that(secondAuditEventBundleByRelationLinkCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                hasSameEntryIds(
                    secondAuditEventBundleAsDirectCall.getExpectedResponse(), ErpAfos.A_24442))
            .and(expectedParamsIn("next", "_count", "5"))
            .and(expectedParamsIn("first", "_count", "5"))
            .isCorrect());
    patient.attemptsTo(
        Verify.that(secondAuditEventBundleAsDirectCall)
            .withExpectedType()
            .and(containsCountOfGivenLinks(List.of("next", "prev", "self", "first"), 4L))
            .and(containsEntriesOfCount(5))
            .and(containsTotalCountOf(0))
            .and(expectedParamsIn("self", "_count", "5"))
            .and(expectedParamsIn("prev", "_count", "5"))
            .isCorrect());

    // CommunicationPaging
    patient.attemptsTo(
        Verify.that(firstComCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(selfForFirstComCall.getExpectedResponse(), ErpAfos.A_24442))
            .and(containsAll5Links())
            .and(expectedParamsIn("self", "_count", "3"))
            .and(expectedParamsIn("next", "_count", "3"))
            .and(expectedParamsIn("first", "_count", "3"))
            .and(expectedParamsIn("first", "__offset", "0"))
            .and(containsMinimumTotalCountOf(tasks.size() * communicationMultiplyer))
            .isCorrect());
    patient.attemptsTo(
        Verify.that(comCallWithFilter)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(
                hasElementAtPosition(
                    firstComCall.getExpectedResponse().getCommunications().get(2), 0))
            .isCorrect());
    patient.attemptsTo(
        Verify.that(firstFromSecondComCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(firstComCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());

    flughafenApo.attemptsTo(
        Verify.that(aposFirstComCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(containsAll5Links())
            .and(expectedParamsIn("self", "_count", "2"))
            .and(expectedParamsIn("next", "_count", "2"))
            .and(expectedParamsIn("first", "_count", "2"))
            .and(expectedParamsIn("first", "__offset", "0"))
            .and(expectedParamsIn("last", "_count", "2"))
            .and(containsMinimumTotalCountOf(tasks.size() * communicationMultiplyer))
            .isCorrect());

    flughafenApo.attemptsTo(
        Verify.that(aposFirstComCall)
            .withExpectedType()
            .and(hasSameEntryIds(aposFirstFromSecondComCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }
}
