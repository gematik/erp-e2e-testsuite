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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.authoredOnDateIsEqual;
import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.verifyAuthoredOnDateWithPredicate;
import static org.junit.Assert.assertTrue;

import de.gematik.bbriccs.fhir.codec.OperationOutcomeExtractor;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadReadyTask;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actors.ActorType;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.PagingArgumentComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SearchPrefix;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("TaskBundle PagingTests")
@Tag("Task_Paging")
public class TaskBundlePagingIT extends ErpTest {
  private final VsdmService vsdmService = config.getSoftKonnVsdmService();
  private static final String QUERY_KEY_SORT = "_sort";
  private static final LinkedList<ErxTask> erxTasks = new LinkedList<>();

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Günther Angermänn")
  private static PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafenApo;

  @AfterAll
  static void housekeeping() {
    val erpClient = patient.abilityTo(UseTheErpClient.class);
    for (val task : erxTasks) {
      val erg = erpClient.request(new TaskAbortCommand(task.getTaskId(), task.getAccessCode()));
      if (erg.isOperationOutcome()) {
        log.info(OperationOutcomeExtractor.extractFrom(erg.getAsOperationOutcome()));
      }
    }
  }

  private void ensurePrecondition() {
    if (erxTasks.isEmpty()) {
      postTasks(10);
    }
  }

  private void postTasks(int numOfTasks) {
    for (int i = 1; i <= numOfTasks; i++) {
      erxTasks.add(
          doctor
              .performs(
                  IssuePrescription.forPatient(patient)
                      .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                      .withRandomKbvBundle())
              .getExpectedResponse());
    }
  }

  public static Stream<Arguments> taskBundleQueryComposer() {
    return PagingArgumentComposer.queryComposerSmallValues()
        .arguments(
            IQueryParameter.search().withOffset(10).withCount(10).createParameter(),
            "_count=10&__offset=10",
            "_count",
            "last",
            "10")
        .arguments(
            IQueryParameter.search()
                .sortedBy("expiry-date", SortOrder.DESCENDING)
                .withCount(3)
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
        .create();
  }

  private static Stream<Arguments> actorComposer() {
    return ArgumentComposer.composeWith()
        .arguments(
            "Patient",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPatientNamed("Sina Hüllmann"))
        .arguments(
            "Apotheke",
            (Function<ErpTest, ErpActor>) erpTest -> erpTest.getPharmacyNamed("Am Flughafen"))
        .create();
  }

  @TestcaseId("ERP_TASK_PAGING_01")
  @ParameterizedTest(
      name = "[{index}] -> Prüfe, dass bei Paging als {0} der RelationLink self funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell der self"
          + " Link ")
  @MethodSource("actorComposer")
  void getTaskBundleWhileUsingRelationSelfLink(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);
    val queries = IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter();
    ErpInteraction<ErxTaskBundle> firstCall = getErxTaskBundleInteraction(actor, queries);

    assertTrue(
        "given TaskBundle has to have a Self-Relation-Link",
        firstCall.getExpectedResponse().hasSelfRelation());

    val secondCall = actor.performs(DownloadBundle.selfFor(firstCall.getExpectedResponse()));
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .and(hasSameEntryIds(secondCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }

  private ErpInteraction<ErxTaskBundle> getErxTaskBundleInteraction(
      ErpActor actor, List<IQueryParameter> queryParams) {
    ErpInteraction<ErxTaskBundle> call;
    if (actor.getType().equals(ActorType.PATIENT)) {
      call = actor.performs(DownloadReadyTask.asPatient(queryParams));
    } else {
      val examEvidence =
          VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
              .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
      call = actor.performs(DownloadReadyTask.with(examEvidence, patient.getEgk(), queryParams));
    }
    return call;
  }

  @TestcaseId("ERP_TASK_PAGING_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe, dass Paging bei TaskBundles funktioniert. Speziell, dass für {0}"
              + " alle fünf RelationLinks vorhanden sind")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell, dass für"
          + " Abrufende alle fünf RelationLinks vorhanden sind")
  @MethodSource("actorComposer")
  void checkInTaskBundleForRelationLinks(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);
    val queries =
        IQueryParameter.search()
            .sortedBy("date", SortOrder.ASCENDING)
            .withOffset(10)
            .withCount(3)
            .createParameter();
    val firstCall = getErxTaskBundleInteraction(actor, queries);

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(containsAll5Links())
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als Apotheke mit URL-Parameter {1}, erwartet im"
              + " RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss sichergestellt werden, dass in den Link-Relation für Apotheken die Clientseitig"
          + " verwendeten Filter und Suchkriterien wiederverwendet werden")
  @MethodSource("taskBundleQueryComposer")
  void backendShouldUseClientsParamsAsPharmacy(
      List<IQueryParameter> iQueryParameters,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(examEvidence, patient.getEgk(), iQueryParameters));

    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als Versicherte mit URL-Parameter {1}, erwartet im"
              + " RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss sichergestellt werden, dass in den Link-Relation für Patienten die Clientseitig"
          + " verwendeten Filter und Suchkriterien wiederverwendet werden")
  @MethodSource("taskBundleQueryComposer")
  void backendShouldUseClientsParamsAsPatient(
      List<IQueryParameter> iQueryParameters,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {
    ensurePrecondition();

    val firstCall = patient.performs(DownloadReadyTask.asPatient(iQueryParameters));

    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als {0} muss der -offset URL-Parameter geprüft"
              + " werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell der"
          + " _offset URL-Parameter")
  @MethodSource("actorComposer")
  void shouldGetTaskBundleWithFixStartIndex(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);

    val pagingArgument =
        IQueryParameter.search()
            .withOffset(5)
            .sortedBy("date", SortOrder.ASCENDING)
            .createParameter();

    val firstCall = getErxTaskBundleInteraction(actor, pagingArgument);
    val secondCall = getErxTaskBundleInteraction(actor, pagingArgument);

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(hasSameEntryIds(secondCall.getExpectedResponse(), ErpAfos.A_24441))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als {0} muss der _offset URL-Parameter mit"
              + " unterschiedlichen Werten für den Startpunkt überprüft werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell der"
          + " _offset URL-Parameter")
  @MethodSource("actorComposer")
  void shouldGetTaskBundleWithDifferent_offset(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);
    val firstCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .withOffset(5)
                .sortedBy("date", SortOrder.ASCENDING)
                .createParameter());

    val secondCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .withOffset(10)
                .sortedBy("date", SortOrder.ASCENDING)
                .createParameter());

    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(hasElementAtPosition(secondCall.getExpectedResponse().getTasks().get(0), 5))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als {0} muss der _offset und _count URL-Parameter"
              + " in Kombination überprüft werden")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell der"
          + " _offset und _count URL-Parameter in Kombination")
  @MethodSource("actorComposer")
  void shouldGetTaskBundleWithDifferentOffsetAndCount(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);

    val firstCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .withCount(10)
                .withOffset(0)
                .sortedBy("date", SortOrder.ASCENDING)
                .createParameter());
    actor.attemptsTo(
        Verify.that(firstCall).withExpectedType().and(containsEntriesOfCount(10)).isCorrect());

    // Task of interest
    val toi = firstCall.getExpectedResponse().getTasks().get(4);

    val secondCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .withCount(5)
                .withOffset(4)
                .sortedBy("date", SortOrder.ASCENDING)
                .createParameter());

    /*
    first:  [ 0 | 1 | 2 | 3 | TOI | 5 | 6 | 7 | 8 | 9 ]
    second: {  <<offset>>  }[ TOI | 1 | 2 | 3 | 4 ]
     */
    actor.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .and(containsEntriesOfCount(5))
            .and(hasElementAtPosition(toi, 0))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von TaskBundles als {0} muss der _offset  Default"
              + " URL-Parameter ( __offset=0) ")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Speziell der"
          + " _offset Default URL-Parameter (__offset=0)")
  @MethodSource("actorComposer")
  void shouldGetTaskBundleWithDefault_offsetAND_Count(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);

    val firstCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter());

    val secondBundleInteraction =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .withOffset(5)
                .sortedBy("date", SortOrder.ASCENDING)
                .createParameter());
    actor.attemptsTo(
        Verify.that(secondBundleInteraction)
            .withExpectedType()
            .and(hasElementAtPosition(firstCall.getExpectedResponse().getTasks().get(9), 4))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_09")
  @ParameterizedTest(
      name =
          "[{index}] -> Prüfe, dass bei Paging als {0} die RelationLink next und previous"
              + " funktioniert")
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert. Genauer die"
          + " RelationLink next und previous")
  @MethodSource("actorComposer")
  void getTaskBundleWhileUsingRelationNextAndPrevious(
      String actorType, Function<ErpTest, ErpActor> actorProvider) {
    ensurePrecondition();
    val actor = actorProvider.apply(this);

    val firstCall =
        getErxTaskBundleInteraction(
            actor,
            IQueryParameter.search()
                .sortedBy("date", SortOrder.ASCENDING)
                .withCount(3)
                .createParameter());

    assertTrue(
        "given first TaskBundle has next-Relation-Link",
        firstCall.getExpectedResponse().hasNextRelation());

    val secondCall = actor.performs(DownloadBundle.nextFor(firstCall.getExpectedResponse()));
    actor.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .isCorrect());
    assertTrue(
        "given second TaskBundle has previous-Relation-Link",
        secondCall.getExpectedResponse().hasPreviousRelation());
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

  @TestcaseId("ERP_TASK_PAGING_10")
  @ParameterizedTest
  @ValueSource(ints = {4, 3, 5})
  @DisplayName(
      "Als Apotheker muss sichergestellt werden, dass der Total-Count um den folgenden Wert erhöht"
          + " wird: ")
  void shouldHaveTotalCountAsApothecary() {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter()));

    postTasks(5);
    val secondCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter()));
    flughafenApo.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .and(containsTotalCountOf(firstCall.getExpectedResponse().getTotal() + 5))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_11")
  @ParameterizedTest
  @ValueSource(ints = {2, 1, 4})
  @DisplayName(
      "Für den Patienten muss sichergestellt werden, dass der Total-Count immer entsprechend um den"
          + " folgenden Wert erhöht wird: ")
  void shouldHaveCorrectTotalCountAsPatient() {
    ensurePrecondition();
    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter()));
    postTasks(5);
    val secondCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search().sortedBy("date", SortOrder.ASCENDING).createParameter()));
    patient.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .and(containsTotalCountOf(firstCall.getExpectedResponse().getTotal() + 5))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_12")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Patienten  funktioniert."
          + " Genauer, dass der Filteroperator Equals für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateEqualFilterAsPatient() {
    ensurePrecondition();

    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
                    .createParameter()));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(authoredOnDateIsEqual(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_13")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Patienten funktioniert."
          + " Genauer, dass der Filteroperator Not-Equals für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateNotEqualFilterAsPatient() {
    ensurePrecondition();

    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.NE)
                    .createParameter()));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> !ld.isEqual(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein anderes AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_14")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles funktioniert für Apotheken."
          + " Genauer, dass der Filteroperator ist gleich für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateEqualFilterAsPharmacy() {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.EQ)
                    .createParameter()));
    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(authoredOnDateIsEqual(LocalDate.now()))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_15")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Apotheken funktioniert."
          + " Genauer, dass der Filteroperator ungleich für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateWithNotEqualFilterAsPharmacy() {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.NE)
                    .createParameter()));
    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> !ld.isEqual(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein anderes AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_16")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Patienten funktioniert."
          + " Genauer, dass der Filteroperator höheres als für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateWithGraterThanFilterAsPatient() {
    ensurePrecondition();

    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.GT)
                    .createParameter()));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> ld.isAfter(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein späteres AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_17")
  @Test
  @DisplayName(
      "Bei TaskBundles muss Paging für Patienten funktionieren, genauer der Filteroperator für"
          + " Datumsabfragen mit früher als (lt = lowerThan)")
  void getTaskBundleWhileUsingDateWithLessThanFilterAsPatient() {
    ensurePrecondition();

    val firstCall =
        patient.performs(
            DownloadReadyTask.asPatient(
                IQueryParameter.search()
                    .withCount(5)
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.LT)
                    .createParameter()));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> ld.isBefore(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein früheres AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_18")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Apotheken funktioniert."
          + " Genauer, dass der Filteroperator höher als für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateWithGraterThabFilterAsPharmacy() {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.GT)
                    .createParameter()));
    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> ld.isAfter(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein späteres AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_PAGING_19")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei TaskBundles für Apotheken funktioniert."
          + " Genauer, dass der Filteroperator früher als für Datumsabfragen funktionieren")
  void getTaskBundleWhileUsingDateWithLessThenFilterAsPharmacy() {
    ensurePrecondition();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL);
    val firstCall =
        flughafenApo.performs(
            DownloadReadyTask.with(
                examEvidence,
                patient.getEgk(),
                IQueryParameter.search()
                    .sortedBy("date", SortOrder.ASCENDING)
                    .withAuthoredOnAndFilter(LocalDate.now(), SearchPrefix.LT)
                    .createParameter()));
    flughafenApo.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .and(
                verifyAuthoredOnDateWithPredicate(
                    ld -> ld.isBefore(LocalDate.now()),
                    "Die enthaltenen Tasks müssen ein früheres AuthoredOn Datum als "
                        + LocalDate.now()
                        + " enthalten"))
            .isCorrect());
  }
}
