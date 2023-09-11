/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.integration.task;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.AuditEventVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.DownloadOpenTask;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmChecksum;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmErrorMessage;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmUpdateReason;
import de.gematik.test.smartcard.Egk;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.thucydides.core.annotations.WithTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import javax.annotation.Nullable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept abrufen als Apotheker")
@Tag("Feature:EGKinApotheke")
@WithTag("Feature:EGKinApotheke")

class TaskGetAsPharmacyUseCase extends ErpTest {

  private static SmartcardArchive smartCards;
  private static final VsdmService vsdmService = TestsuiteConfiguration.getInstance()
          .getVsdmServiceConfiguration().createDefault();

  @Actor(name = "Hanna Bäcker")
  private PatientActor hanna;

  private Egk hannasEgk;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;


  @BeforeAll
  static void init() {
    smartCards = SmartcardFactory.getArchive();
  }

  @SneakyThrows
  @BeforeEach
  void setup() {
    hannasEgk = smartCards.getEgkByICCSN(
        TestsuiteConfiguration.getInstance().getPatientConfig(hanna.getName()).getEgkIccsn());
    givenThat(flughafen).can(ManagePharmacyPrescriptions.itWorksWith());
  }


  private static ErpInteraction<ErxTaskBundle> performDownloadOpenTask(PharmacyActor pharmacy,
      @Nullable VsdmExamEvidence evidence) {
    return pharmacy.performs(evidence != null ?
            DownloadOpenTask.withExamEvidence(evidence.encodeAsBase64()) :
            DownloadOpenTask.withoutExamEvidence());
  }


  @TestcaseId("ERP_TASK_GET_PHARMACY_01")
  @ParameterizedTest(name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {1}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {1}")
  @MethodSource("validExamEvidence")
  void validExamEvidences(
      VsdmUpdateReason reason, VsdmExamEvidenceResult examEvidenceResult) {

    val checksum = vsdmService.requestFor(hannasEgk, reason);
    val examEvidence = VsdmExamEvidence.builder(examEvidenceResult)
            .checksum(checksum);

    val response = performDownloadOpenTask(flughafen, examEvidence.build());

    flughafen.attemptsTo(Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
            .pharmacy(flughafen)
            .checksum(checksum.generate())
            .build();

    val downloadedAuditEvents = hanna.performs(
            DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.firstCorrespondsTo(ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_02")
  @ParameterizedTest(name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}")
  @MethodSource("invalidExamEvidence")
  void invalidExamEvidences(VsdmExamEvidenceResult examEvidenceResult) {

    var examEvidence = VsdmExamEvidence.builder(examEvidenceResult);

    val response = performDownloadOpenTask(flughafen, examEvidence.build());

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
            .and(auditEventVerifier.firstCorrespondsTo(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_03")
  @ParameterizedTest(name = "[{index}] ->Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} mit ungültiger Prüfziffer {1}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} mit ungültiger Prüfziffer")
  @MethodSource("invalidChecksums")
  void invalidChecksum(VsdmExamEvidenceResult examEvidenceResult, VsdmChecksum checksum, VsdmErrorMessage expectedErrorMessage) {

    val examEvidence = VsdmExamEvidence.builder(examEvidenceResult).checksum(checksum);

    val response = performDownloadOpenTask(flughafen, examEvidence.build());

    flughafen.attemptsTo(Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(operationOutcomeHasDetailsText(expectedErrorMessage.getText(), ErpAfos.A_23454))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
            .pharmacy(flughafen)
            .build();

    val downloadedAuditEvents = hanna.performs(
            DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.firstCorrespondsTo(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_04")
  @Test
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis 2 ohne Prüfziffer")
  void withoutChecksum() {

    val examEvidence = VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES);
    val response = performDownloadOpenTask(flughafen, examEvidence.build());
    flughafen.attemptsTo(Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(operationOutcomeHasDetailsText(VsdmErrorMessage.PROOF_OF_PRESENCE_WITHOUT_CHECKSUM.getText(), ErpAfos.A_23455))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
            .pharmacy(flughafen)
            .build();

    val downloadedAuditEvents = hanna.performs(
            DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.firstCorrespondsTo(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_05")
  @Test
  @DisplayName("Abrufen von Tasks als Apotheker ohne Prüfungsnachweis")
  void withoutExamEvidence() {

    val response = performDownloadOpenTask(flughafen, null);
    flughafen.attemptsTo(Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(operationOutcomeHasDetailsText(VsdmErrorMessage.INVALID_PNW.getText(), ErpAfos.A_23450))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder()
            .pharmacy(flughafen)
            .build();

    val downloadedAuditEvents = hanna.performs(
            DownloadAuditEvent.orderByDateDesc());

    hanna.attemptsTo(Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.firstCorrespondsTo(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());

  }

  static Stream<Arguments> validExamEvidence() {
    return ArgumentComposer.composeWith()
        .arguments(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL)
        .arguments(VsdmExamEvidenceResult.NO_UPDATES)
            .multiply(Arrays.stream(VsdmUpdateReason.values()).toList())
        .create();
  }

  static Stream<Arguments> invalidExamEvidence() {
    return ArgumentComposer.composeWith()
            .arguments(VsdmExamEvidenceResult.ERROR_EGK)
            .arguments(VsdmExamEvidenceResult.ERROR_AUTH_CERT_INVALID)
            .arguments(VsdmExamEvidenceResult.ERROR_ONLINECHECK_NOT_POSSIBLE)
            .arguments(VsdmExamEvidenceResult.ERROR_OFFLINE_PERIOD_EXCEEDED)
            .create();
  }

  static Stream<Arguments> invalidChecksums() {
    val hMacKey = vsdmService.getHMacKey();
    val hannaKvnr = "X110499478";
    return ArgumentComposer.composeWith()
            // invalid identifier
            .arguments(VsdmChecksum.builder(hannaKvnr).identifier('y').key(hMacKey).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
            // invalid version
            .arguments(VsdmChecksum.builder(hannaKvnr).version('0').key(hMacKey).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
            // invalid kvnr
            .arguments(VsdmChecksum.builder("ABC").key(hMacKey).build(),
                    VsdmErrorMessage.FAILED_PARSING_PNW)
            .arguments(VsdmChecksum.builder(hannaKvnr).version('0').key(hMacKey).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
            // invalid hMacKey - A_23546 is tested with this test date
            .arguments(VsdmChecksum.builder(hannaKvnr).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
            // invalid timestamp
            .arguments(VsdmChecksum.builder(hannaKvnr).timestamp(Instant.now().minus(30, ChronoUnit.MINUTES)).key(hMacKey).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
            .arguments(VsdmChecksum.builder(hannaKvnr).timestamp(Instant.now().plus(40, ChronoUnit.MINUTES)).key(hMacKey).build(),
                    VsdmErrorMessage.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
            .multiply(List.of(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL, VsdmExamEvidenceResult.NO_UPDATES))
            .create();
  }

}
