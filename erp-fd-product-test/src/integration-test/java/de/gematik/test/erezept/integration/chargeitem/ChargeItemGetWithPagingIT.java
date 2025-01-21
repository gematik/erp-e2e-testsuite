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

package de.gematik.test.erezept.integration.chargeitem;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.GenericBundleVerifier.*;
import static org.junit.Assert.assertTrue;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.bundlepaging.DownloadBundle;
import de.gematik.test.erezept.actions.chargeitem.GetChargeItems;
import de.gematik.test.erezept.actions.chargeitem.PostChargeItem;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.arguments.PagingArgumentComposer;
import de.gematik.test.erezept.client.rest.param.IQueryParameter;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.ChargeItemDeleteCommand;
import de.gematik.test.erezept.client.usecases.search.ChargeItemSearch;
import de.gematik.test.erezept.fhir.builder.dav.DavAbgabedatenFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxConsentBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@net.jcip.annotations.NotThreadSafe
@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("ChargeItem PagingTests")
@Tag("CHARGE_ITEM")
public class ChargeItemGetWithPagingIT extends ErpTest {
  private static final LinkedList<ErxTask> erxTasks = new LinkedList<>();
  private static final LinkedList<ErxChargeItem> chargeItems = new LinkedList<>();

  @Actor(name = "Günther Angermänn")
  private static PatientActor patient;

  @Actor(name = "Hanna Bäcker")
  private static PatientActor secondPatient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor pharmacy;

  @AfterAll
  static void housekeeping() {
    deleteChargeItems(patient, chargeItems);
  }

  private static void deleteChargeItems(PatientActor patient, List<ErxChargeItem> chargeItemList) {
    val erpClient = patient.abilityTo(UseTheErpClient.class);
    for (val chargeItem : chargeItemList) {
      val erg = erpClient.request(new ChargeItemDeleteCommand(chargeItem.getPrescriptionId()));
      if (erg.isOperationOutcome()) {
        log.info(OperationOutcomeWrapper.extractFrom(erg.getAsOperationOutcome()));
      }
    }
  }

  public static Stream<Arguments> ChargeItemBundleQueryComposer() {
    return PagingArgumentComposer.queryComposerSmallValues().create();
  }

  private void ensurePrecondition() {
    if (erxTasks.isEmpty()) {
      grandConsentAsPkv(patient);
      postTasksAndChargeItems(30);
    }
  }

  private void postTasksAndChargeItems(int numOfTasks) {
    for (int i = 1; i <= numOfTasks; i++) {
      erxTasks.add(setUpTask(patient));
    }
    for (val task : erxTasks) {
      chargeItems.add(setUpChargeItem(task, patient));
    }
  }

  private ErxChargeItem setUpChargeItem(ErxTask task, PatientActor patientActor) {

    val acceptation = pharmacy.performs(AcceptPrescription.forTheTask(task)).getExpectedResponse();
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(task.getPrescriptionId()).fake();

    return pharmacy
        .performs(
            PostChargeItem.forPatient(patientActor)
                .davBundle(davAbgabedatenBundle)
                .withAcceptBundle(acceptation))
        .getExpectedResponse();
  }

  private ErxTask setUpTask(PatientActor patientActor) {
    return doctor
        .performs(IssuePrescription.forPatient(patientActor).withRandomKbvBundle())
        .getExpectedResponse();
  }

  private void grandConsentAsPkv(PatientActor patientActor) {
    patientActor.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    val consent = ErxConsentBuilder.forKvnr(patientActor.getKvnr()).build();
    consent.setDateTime(new Date());
    patientActor.performs(GrantConsent.forOneSelf().withDefaultConsent());

    patientActor.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_01")
  @Test
  @DisplayName(
      "Es muss überprüft werden, dass mehrere ChargeItems für Versicherte abgerufen werden können")
  void shouldDownloadChargeItems() {
    // precondition
    grandConsentAsPkv(secondPatient);
    LinkedList<ErxTask> localTasks = new LinkedList<>();
    LinkedList<ErxChargeItem> lokalChargeItems = new LinkedList<>();
    for (int i = 1; i <= 10; i++) {
      localTasks.add(setUpTask(secondPatient));
    }
    for (val task : localTasks) {
      lokalChargeItems.add(setUpChargeItem(task, secondPatient));
    }

    val chargeItemSet =
        secondPatient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.getChargeItems(SortOrder.ASCENDING)));
    secondPatient.attemptsTo(
        Verify.that(chargeItemSet)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(minimumCountOfEntriesOf(10))
            .isCorrect());
    // housekeeping
    deleteChargeItems(secondPatient, lokalChargeItems);
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_02")
  @Test
  @DisplayName(
      "Es muss überprüft werden, dass mehrere ChargeItems für Versicherte abgerufen werden können"
          + " mit Count Parameter")
  void shouldDownloadChargeItemsWithCount() {
    ensurePrecondition();
    val queries =
        IQueryParameter.search()
            .sortedBy("date", SortOrder.ASCENDING)
            .withCount(5)
            .createParameter();

    val chargeItemSet =
        patient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.searchFor().withQuery(queries).build()));
    patient.attemptsTo(
        Verify.that(chargeItemSet)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(containsEntriesOfCount(5))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_03")
  @Test
  @DisplayName(
      "Es muss überprüft werden, dass mehrere ChargeItems für Versicherte abgerufen werden können"
          + " mit allen 5 RelativeLink")
  void shouldDownloadChargeItemsWithRelationLinks() {
    grandConsentAsPkv(secondPatient);
    LinkedList<ErxTask> localTasks = new LinkedList<>();
    LinkedList<ErxChargeItem> lokalChargeItems = new LinkedList<>();

    for (int i = 1; i <= 10; i++) {
      localTasks.add(setUpTask(secondPatient));
    }
    for (val task : localTasks) {
      lokalChargeItems.add(setUpChargeItem(task, secondPatient));
    }

    val queries =
        IQueryParameter.search()
            .sortedBy("date", SortOrder.ASCENDING)
            .withCount(2)
            .withOffset(4)
            .createParameter();

    val chargeItemSet =
        secondPatient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.searchFor().withQuery(queries).build()));
    secondPatient.attemptsTo(
        Verify.that(chargeItemSet)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(containsCountOfGivenLinks(List.of("next", "prev", "self", "first", "last"), 5))
            .isCorrect());
    deleteChargeItems(secondPatient, lokalChargeItems);
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_04")
  @Test
  @DisplayName(
      "Es muss überprüft werden, dass mehrere ChargeItems für Versicherte abgerufen werden können"
          + " und der Self Link funktioniert")
  void shouldDownloadChargeItemsWithRelationLinksSelf() {
    ensurePrecondition();
    val queries =
        IQueryParameter.search()
            .sortedBy("date", SortOrder.ASCENDING)
            .withCount(2)
            .withOffset(4)
            .createParameter();

    val chargeItemSetFirstCall =
        patient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.searchFor().withQuery(queries).build()));

    val chargeItemSetSecondCall =
        patient.performs(DownloadBundle.selfFor(chargeItemSetFirstCall.getExpectedResponse()));

    patient.attemptsTo(
        Verify.that(chargeItemSetFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(hasSameEntryIds(chargeItemSetSecondCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von ChargeItemBundlesBundles als Patient mit URL-Parameter {1},"
              + " erwartet im RelationLink {3} als Wert für {2} = {4}")
  @DisplayName(
      "Es muss überprüft werden, dass mehrere ChargeItems für Versicherte abgerufen werden können"
          + " und Search-Parameter in den RelationLinks wiederverwendet werden")
  @MethodSource("ChargeItemBundleQueryComposer")
  void shouldDownloadChargeItemsAndUseGivenQueryParamsInRelationLinks(
      List<IQueryParameter> iQueryParameters,
      String serenityDescription,
      String queryValue,
      String relation,
      String expectedValue) {
    ensurePrecondition();

    val chargeItemSetFirstCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor().withQuery(iQueryParameters).build()));

    patient.attemptsTo(
        Verify.that(chargeItemSetFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(expectedParamsIn(relation, queryValue, expectedValue))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_06")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei ChargeItems funktioniert. Speziell der"
          + " _offset URL-Parameter")
  void shouldDownloadChargeItemsWithOffsetParam() {
    ensurePrecondition();
    val queries =
        IQueryParameter.search()
            .sortedBy("date", SortOrder.ASCENDING)
            .withCount(4)
            .withOffset(4)
            .createParameter();

    val chargeItemSetFirstCall =
        patient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.searchFor().withQuery(queries).build()));
    val chargeItemSetSecondCall =
        patient.performs(
            GetChargeItems.fromServerWith(ChargeItemSearch.searchFor().withQuery(queries).build()));

    patient.attemptsTo(
        Verify.that(chargeItemSetFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(hasSameEntryIds(chargeItemSetSecondCall.getExpectedResponse(), ErpAfos.A_24441))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_07")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei ChargeItems funktioniert. Speziell der"
          + " _offset URL-Parameter")
  void shouldDownloadChargeItemsWithOffsetParam2() {
    ensurePrecondition();

    val chargeItemSetFirstCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withOffset(5)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));
    val chargeItemSetSecondCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withOffset(10)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));

    patient.attemptsTo(
        Verify.that(chargeItemSetFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(
                hasElementAtPosition(
                    chargeItemSetSecondCall.getExpectedResponse().getChargeItems().get(0), 5))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_08")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei ChargeItems funktioniert. Speziell der"
          + " __offset und _count URL-Parameter in Kombination")
  void shouldGetChargeItemsWithDifferentOffsetAndCount() {
    ensurePrecondition();

    val chargeItemSetFirstCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withCount(10)
                            .withOffset(5)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));
    val chargeItemSeSecondCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withCount(5)
                            .withOffset(10)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));

    patient.attemptsTo(
        Verify.that(chargeItemSetFirstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(
                hasElementAtPosition(
                    chargeItemSeSecondCall.getExpectedResponse().getChargeItems().get(4), 9))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_9")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei ChargeItems funktioniert. Speziell der"
          + " Default URL-Parameter (__offset=0)")
  void shouldGetChargeItemsWithDefaultOffset() {
    ensurePrecondition();

    val firstCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));
    patient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCodeIsBetween(200, 210))
            .isCorrect());
    val secondCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withOffset(5)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));

    patient.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24434))
            .and(hasElementAtPosition(firstCall.getExpectedResponse().getChargeItems().get(9), 4))
            .isCorrect());
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_10")
  @Test
  @DisplayName(
      "Es muss sichergestellt werden, dass Paging bei ChargeItems funktioniert. Genauer die"
          + " RelationLink next und previous")
  void getTaskChargeItemsWhileUsingRelationNextAndPrevious() {
    // precondition
    grandConsentAsPkv(secondPatient);
    LinkedList<ErxTask> localTasks = new LinkedList<>();
    LinkedList<ErxChargeItem> lokalChargeItems = new LinkedList<>();

    for (int i = 1; i <= 10; i++) {
      localTasks.add(setUpTask(secondPatient));
    }
    for (val task : localTasks) {
      lokalChargeItems.add(setUpChargeItem(task, secondPatient));
    }

    val firstCall =
        secondPatient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .withCount(2)
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));
    assertTrue(
        "given first TaskBundle has next-Relation-Link",
        firstCall.getExpectedResponse().hasNextRelation());
    val secondCall =
        secondPatient.performs(DownloadBundle.nextFor(firstCall.getExpectedResponse()));
    secondPatient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200, ErpAfos.A_24442))
            .isCorrect());
    assertTrue(
        "given second TaskBundle has previous-Relation-Link",
        secondCall.getExpectedResponse().hasPreviousRelation());
    val firstFromSecondCall =
        secondPatient.performs(DownloadBundle.previousFor(secondCall.getExpectedResponse()));
    secondPatient.attemptsTo(
        Verify.that(firstFromSecondCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());
    secondPatient.attemptsTo(
        Verify.that(firstCall)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(hasSameEntryIds(firstFromSecondCall.getExpectedResponse(), ErpAfos.A_24442))
            .isCorrect());
    // housekeeping
    deleteChargeItems(secondPatient, lokalChargeItems);
  }

  @TestcaseId("ERP_CHARGE_ITEM_PAGING_11")
  @Test
  @DisplayName(
      "Für den Patienten muss sichergestellt werden, dass der Total-Count immer entsprechend um den"
          + " folgenden Wert erhöht wird ")
  void shouldHaveCorrectTotalCountAsPatient() {
    ensurePrecondition();
    patient.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);

    val firstCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));

    val newTask =
        doctor
            .performs(IssuePrescription.forPatient(patient).withRandomKbvBundle())
            .getExpectedResponse();
    erxTasks.add(newTask);

    val acceptation =
        pharmacy.performs(AcceptPrescription.forTheTask(newTask)).getExpectedResponse();
    pharmacy.performs(ClosePrescription.acceptedWith(acceptation));
    val davAbgabedatenBundle = DavAbgabedatenFaker.builder(newTask.getPrescriptionId()).fake();
    val response =
        pharmacy
            .performs(
                PostChargeItem.forPatient(patient)
                    .davBundle(davAbgabedatenBundle)
                    .withAcceptBundle(acceptation))
            .getExpectedResponse();
    chargeItems.add(response);

    val secondCall =
        patient.performs(
            GetChargeItems.fromServerWith(
                ChargeItemSearch.searchFor()
                    .withQuery(
                        IQueryParameter.search()
                            .sortedBy("date", SortOrder.ASCENDING)
                            .createParameter())
                    .build()));
    patient.attemptsTo(
        Verify.that(secondCall)
            .withExpectedType()
            .and(containsTotalCountOf(firstCall.getExpectedResponse().getTotal() + 1))
            .isCorrect());
  }
}
