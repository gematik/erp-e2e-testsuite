/*
 *
 *  * Copyright (c) 2022 gematik GmbH
 *  * 
 *  * Licensed under the Apache License, Version 2.0 (the License);
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  * 
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  * 
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an 'AS IS' BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static net.serenitybdd.screenplay.GivenWhenThen.*;
import static org.awaitility.Awaitility.*;

import de.gematik.test.core.*;
import de.gematik.test.core.annotations.*;
import de.gematik.test.core.expectations.verifier.*;
import de.gematik.test.erezept.*;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.konnektor.commands.options.*;
import java.time.*;
import java.time.temporal.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.junit.runners.*;
import net.serenitybdd.junit5.*;
import net.thucydides.core.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junit.runner.*;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept abrufen als Apotheker")
@Tag("Feature:EGKinApotheke")
@WithTag("Feature:EGKinApotheke")
class TaskGetAsPharmacyUseCase extends ErpTest {

  private static List<TimeOfRequest> counterPharmacyRequests = new ArrayList<>();
  @Data
  private static class TimeOfRequest {
    private final PharmacyActor actor;
    private final Instant timestamp = Instant.now();
  }

  private static final boolean isRu = !System
      .getProperty("erp.config.activeEnvironment", "TU")
      .equals("TU");

  @Actor(name = "Bernd Claudius")
  private DoctorActor bernd;

  @Actor(name = "Hanna Bäcker")
  private PatientActor hanna;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @Actor(name = "Stadtapotheke")
  private PharmacyActor stadtapotheke;


  private ErxTask erxTask;


  @SneakyThrows
  @BeforeEach
  void setup() {
    givenThat(flughafen).can(ManagePharmacyPrescriptions.itWorksWith());
  }

  private static boolean hasPharmacyToManyRequests(PharmacyActor actor) {
    counterPharmacyRequests.removeIf(c -> c.getActor().equals(actor) && c.getTimestamp()
        .isBefore(Instant.now().minus(1, ChronoUnit.MINUTES)));
    return counterPharmacyRequests.size() >= 5;
  }

  private static ErpInteraction<ErxTaskBundle> performDownloadOpenTask(PharmacyActor pharmacy,
      @Nullable PatientActor patient,
      @Nullable ExamEvidence evidence) {
    // wait if a pharmacist has tried to retrieve Task more than 5 times in one minute
    await().atMost(1, TimeUnit.MINUTES).until(() -> !hasPharmacyToManyRequests(pharmacy));
    var actionBuilder = DownloadOpenTask.builder();
    if(patient != null) {
      actionBuilder.kvnr(patient.getKvnr());
    }
    if(evidence != null) {
      actionBuilder.examEvidence(evidence.encodeAsBase64());
    }
    counterPharmacyRequests.add(new TimeOfRequest(pharmacy));
    return pharmacy.performs(actionBuilder.build());
  }

  @AfterEach
  void postcondition() {
    val acception = flughafen.performs(AcceptPrescription.forTheTask(erxTask));
    flughafen.performs(DispensePrescription.acceptedWith(acception));
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_01")
  @ParameterizedTest(name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}")
  @DisplayName("Abrufen von Tasks als Apotheker mit einem Prüfungsnachweis und prüfen des Versichertenprotokolls")
  @MethodSource("examEvidenceWithExpectedStatusCode")
  void downloadTaskAsPharmcyForAllExamEvidences(
      ExamEvidence examEvidence, int expectedStatusCode) {

    erxTask = bernd.performs(
        IssuePrescription.forPatient(hanna)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withRandomKbvBundle()).getExpectedResponse();

    val response = performDownloadOpenTask(flughafen, hanna, examEvidence);

    Verify.Builder<?> builder;
    if(expectedStatusCode == 200) {
      builder = Verify.that(response).withExpectedType();
    } else {
      builder = Verify.that(response).withOperationOutcome();
    }
    flughafen.attemptsTo(builder.hasResponseWith(returnCode(expectedStatusCode)).isCorrect());
  }

  @SneakyThrows
  @TestcaseId("ERP_TASK_GET_PHARMACY_02")
  @Test
  @DisplayName("Mehrfaches Abrufen von Tasks als Apotheker innerhalb einer Minute")
  void downloadSeveralTaskWithinAMinute() {
    givenThat(stadtapotheke).can(ManagePharmacyPrescriptions.itWorksWith());
    erxTask = bernd.performs(
        IssuePrescription.forPatient(hanna)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withRandomKbvBundle()).getExpectedResponse();
    // A_23160: dass durch die abgebende LEI die Operation nicht häufiger als 5 mal (konfigurierbar)
    // pro Minute aufgerufen wurde und bei Überschreiten des Limits den http-Request mit dem
    // http-Status-Code 429 ablehnen.

    val examEvidence = ExamEvidence.UPDATES_SUCCESSFUL;

    for(int i = 1; i<=5; i++) {
      stadtapotheke.performs(DownloadOpenTask.builder()
          .kvnr(hanna.getKvnr())
          .examEvidence(examEvidence.encodeAsBase64()).build());
    }
    val erxTaskBundleErpInteraction = stadtapotheke.performs(DownloadOpenTask.builder()
        .kvnr(hanna.getKvnr())
        .examEvidence(examEvidence.encodeAsBase64()).build());
    stadtapotheke.attemptsTo(
        Verify.that(erxTaskBundleErpInteraction)
            .withOperationOutcome()
            .hasResponseWith(returnCode(429))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_03")
  @Test
  @DisplayName("Abruf Task als Aoptheker ohne KVNR")
  void downloadTaskWithoutKVNR() {
    val examEvidence = ExamEvidence.UPDATES_SUCCESSFUL;
    erxTask = bernd.performs(
        IssuePrescription.forPatient(hanna)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withRandomKbvBundle()).getExpectedResponse();

    val response = performDownloadOpenTask(flughafen, null, examEvidence);

    flughafen.attemptsTo(Verify.that(response)
        .withOperationOutcome()
        .hasResponseWith(returnCode(400))
        .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_04")
  @Test
  @DisplayName("Abruf Task als Aoptheker ohne Prüfungsnachweis")
  void downloadTaskWithoutExamEvidence() {
    erxTask = bernd.performs(
        IssuePrescription.forPatient(hanna)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withRandomKbvBundle()).getExpectedResponse();

    val response = performDownloadOpenTask(flughafen, hanna, null);

    flughafen.attemptsTo(Verify.that(response)
        .withOperationOutcome()
        .hasResponseWith(returnCode(403))
        .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
        .pharmacy(flughafen)
        .build();

    val downloadedAuditEvents = hanna.performs(
        DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
        .withExpectedType()
        .hasResponseWith(returnCode(200))
        .and(auditEventVerifier.firstCorrespondsTo(Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
        .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_05")
  @ParameterizedTest(name = "[{index}] -> Protokolleintrag für Prüfungsnachweis {0}")
  @DisplayName("Erzeugen von Protokolleinträgen bei Abruf von Tasks als Apotheker mit einem Prüfungsnachweis")
  @MethodSource("possibleAuditEvents")
  void verifyAuditEvent(
      ExamEvidence examEvidence, ErxAuditEvent.Representation auditEventRepresentation, int expectedStatusCode) {
    erxTask = bernd.performs(
        IssuePrescription.forPatient(hanna)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withRandomKbvBundle()).getExpectedResponse();

    val response = performDownloadOpenTask(flughafen,
        hanna, examEvidence);

    flughafen.attemptsTo(Verify.that(response)
        .withIndefiniteType()
        .hasResponseWith(returnCode(expectedStatusCode))
        .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
        .pharmacy(flughafen)
        .checksum(examEvidence.getChecksum().orElse(""))
        .build();

    val downloadedAuditEvents = hanna.performs(
        DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
        .withExpectedType()
        .hasResponseWith(returnCode(200))
        .and(auditEventVerifier.firstCorrespondsTo(auditEventRepresentation))
        .isCorrect());

  }


  static Stream<Arguments> examEvidenceWithExpectedStatusCode() {
    return ArgumentComposer.composeWith()
        .arguments(ExamEvidence.UPDATES_SUCCESSFUL, isRu? 200 : 403)
        .arguments(ExamEvidence.NO_UPDATES, isRu? 200 : 403)
        .arguments(ExamEvidence.ERROR_EGK, isRu? 200 : 403)
        .arguments(ExamEvidence.ERROR_AUTH_CERT_INVALID,403)
        .arguments(ExamEvidence.ERROR_ONLINECHECK_NOT_POSSIBLE,403)
        .arguments(ExamEvidence.ERROR_OFFLINE_PERIOD_EXCEEDED, 403)
        .arguments(ExamEvidence.INVALID_EVIDENCE_NUMBER, 403)
        .arguments(ExamEvidence.NO_UPDATE_WITH_EXPIRED_TIMESTAMP,  403)
        .arguments(ExamEvidence.NO_WELL_FORMED_XML, 403)
        .create();
  }

  static Stream<Arguments> possibleAuditEvents() {
    return ArgumentComposer.composeWith()
        .arguments(ExamEvidence.UPDATES_SUCCESSFUL,
            isRu? Representation.PHARMACY_GET_TASK_SUCCESSFUL_WITH_CHECKSUM : Representation.PHARMACY_GET_TASK_UNSUCCESSFUL,
            isRu? 200 : 403)
        .arguments(ExamEvidence.ERROR_EGK, isRu ? Representation.PHARMACY_GET_TASK_SUCCESSFUL_WITHOUT_CHECKSUM : Representation.PHARMACY_GET_TASK_UNSUCCESSFUL,
            isRu ? 200 : 403)
        .arguments(ExamEvidence.NO_WELL_FORMED_XML,Representation.PHARMACY_GET_TASK_UNSUCCESSFUL, 403)
        .create();
  }



}
