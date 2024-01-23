/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.AuditEventVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.konnektor.soap.mock.vsdm.*;
import de.gematik.test.smartcard.Egk;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Stream;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.WithTag;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept abrufen als Apotheker")
@Tag("Feature:EGKinApotheke")
@WithTag("Feature:EGKinApotheke")
class TaskGetAsPharmacyUseCase extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  private static ErpInteraction<ErxTaskBundle> performDownloadOpenTask(
      PharmacyActor pharmacy, @Nullable VsdmExamEvidence evidence) {
    return pharmacy.performs(
        evidence != null
            ? DownloadReadyTask.withExamEvidence(evidence.encodeAsBase64())
            : DownloadReadyTask.withoutExamEvidence());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_01")
  @ParameterizedTest(name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {1}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {1}")
  @MethodSource("validExamEvidence")
  void validExamEvidences(VsdmUpdateReason reason, VsdmExamEvidenceResult examEvidenceResult) {
    val vsdmService = config.getSoftKonnVsdmService();
    val checksum = vsdmService.requestFor(patient.getEgk(), reason);
    val examEvidence = VsdmExamEvidence.builder(examEvidenceResult).checksum(checksum);

    val response = performDownloadOpenTask(pharmacy, examEvidence.build());

    pharmacy.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(200)).isCorrect());

    val auditEventVerifier =
        new AuditEventVerifier.Builder().pharmacy(pharmacy).checksum(checksum.generate()).build();

    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());

    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                auditEventVerifier.firstCorrespondsTo(
                    ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_02")
  @ParameterizedTest(name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}")
  @MethodSource("invalidExamEvidence")
  void invalidExamEvidences(VsdmExamEvidenceResult examEvidenceResult) {

    var examEvidence = VsdmExamEvidence.builder(examEvidenceResult);

    val response = performDownloadOpenTask(pharmacy, examEvidence.build());

    pharmacy.attemptsTo(
        Verify.that(response).withOperationOutcome().hasResponseWith(returnCode(403)).isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder().pharmacy(pharmacy).build();

    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());

    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                auditEventVerifier.firstCorrespondsTo(
                    ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_03")
  @ParameterizedTest(
      name =
          "[{index}] ->Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} mit ungültiger Prüfziffer {1}")
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} mit ungültiger Prüfziffer")
  @MethodSource("invalidChecksums")
  void invalidChecksum(
      VsdmExamEvidenceResult examEvidenceResult,
      String explanation,
      BiFunction<VsdmService, Egk, VsdmChecksum> checksumBuilder,
      VsdmErrorMessage expectedErrorMessage) {

    val vsdmService = config.getSoftKonnVsdmService();
    val checksum = checksumBuilder.apply(vsdmService, patient.getEgk());
    val examEvidence = VsdmExamEvidence.builder(examEvidenceResult).checksum(checksum);
    val response = performDownloadOpenTask(pharmacy, examEvidence.build());

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(operationOutcomeHasDetailsText(expectedErrorMessage.getText(), ErpAfos.A_23454))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder().pharmacy(pharmacy).build();

    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());

    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                auditEventVerifier.firstCorrespondsTo(
                    ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_04")
  @Test
  @DisplayName("Abrufen von Tasks als Apotheker mit Prüfungsnachweis 2 ohne Prüfziffer")
  void withoutChecksum() {
    val examEvidence = VsdmExamEvidence.builder(VsdmExamEvidenceResult.NO_UPDATES);
    val response = performDownloadOpenTask(pharmacy, examEvidence.build());
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(
                operationOutcomeHasDetailsText(
                    VsdmErrorMessage.PROOF_OF_PRESENCE_WITHOUT_CHECKSUM.getText(), ErpAfos.A_23455))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder().pharmacy(pharmacy).build();

    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());

    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                auditEventVerifier.firstCorrespondsTo(
                    ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_05")
  @Test
  @DisplayName("Abrufen von Tasks als Apotheker ohne Prüfungsnachweis")
  void withoutExamEvidence() {
    val response = performDownloadOpenTask(pharmacy, null);
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(403))
            .and(
                operationOutcomeHasDetailsText(
                    VsdmErrorMessage.INVALID_PNW.getText(), ErpAfos.A_23450))
            .isCorrect());

    val auditEventVerifier = new AuditEventVerifier.Builder().pharmacy(pharmacy).build();

    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());

    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(
                auditEventVerifier.firstCorrespondsTo(
                    ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_06")
  @Test
  @DisplayName("Abrufen von Tasks als Apotheker ohne Secret und AccessCode")
  void withTaskIdOnly() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    val response =
        pharmacy.performs(GetPrescriptionById.withTaskId(task.getTaskId()).withoutAuthentication());
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_19113_01)
            .responseWith(returnCodeIs(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_07")
  @Test
  @DisplayName(
      "Abrufen von Rezepten als Apotheker ohne vorheriges ACCEPT, daher kein Secret und nur ein AccessCode")
  void withTaskIdAndAccessCode() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    val response =
        pharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_24176)
            .responseWith(returnCodeIs(412))
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
    return ArgumentComposer.composeWith()
        .arguments(
            "mit invalidem Identifier",
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) ->
                    VsdmChecksum.builder(egk.getKvnr())
                        .identifier('y')
                        .key(vsdm.getHMacKey())
                        .build(),
            VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "mit invalider Version",
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) ->
                    VsdmChecksum.builder(egk.getKvnr()).version('0').key(vsdm.getHMacKey()).build(),
            VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "mit invalider KVNR",
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) -> VsdmChecksum.builder("ABC").key(vsdm.getHMacKey()).build(),
            VsdmErrorMessage.FAILED_PARSING_PNW)
        .arguments(
            "mit invalidem hMacKey für A_23546", // invalid hMacKey - A_23546 is tested with this
            // test date
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) -> VsdmChecksum.builder(egk.getKvnr()).build(),
            VsdmErrorMessage.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "mit invalidem Timestamp -30 Minuten",
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) ->
                    VsdmChecksum.builder(egk.getKvnr())
                        .timestamp(Instant.now().minus(30, ChronoUnit.MINUTES))
                        .key(vsdm.getHMacKey())
                        .build(),
            VsdmErrorMessage.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
        .arguments(
            "mit invalidem Timestamp +40 Minuten",
            (BiFunction<VsdmService, Egk, VsdmChecksum>)
                (vsdm, egk) ->
                    VsdmChecksum.builder(egk.getKvnr())
                        .timestamp(Instant.now().plus(40, ChronoUnit.MINUTES))
                        .key(vsdm.getHMacKey())
                        .build(),
            VsdmErrorMessage.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
        .multiply(
            List.of(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL, VsdmExamEvidenceResult.NO_UPDATES))
        .create();
  }
}
