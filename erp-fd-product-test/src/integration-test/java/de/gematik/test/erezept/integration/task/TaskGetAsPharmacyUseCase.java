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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static java.text.MessageFormat.format;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.core.expectations.verifier.AuditEventVerifier;
import de.gematik.test.core.expectations.verifier.TaskBundleVerifier;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.DownloadAuditEvent;
import de.gematik.test.erezept.actions.DownloadReadyTask;
import de.gematik.test.erezept.actions.GetPrescriptionById;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.resources.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.toggle.EgkPharmacyAcceptPN3Toggle;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmUpdateReason;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept abrufen als Apotheker")
@Tag("Feature:EGKinApotheke")
class TaskGetAsPharmacyUseCase extends ErpTest {
  private static final Boolean pn3Activate =
      featureConf.getToggle(new EgkPharmacyAcceptPN3Toggle());

  private final VsdmService vsdmService = config.getSoftKonnVsdmService();

  @Actor(name = "Hanna Bäcker")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  public static VerificationStep<ErxTaskBundle> hasNoTasks() {
    Predicate<ErxTaskBundle> verify = bundle -> bundle.getTasks().isEmpty();
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            ErpAfos.A_23452.getRequirement(),
            format(
                "Das ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf nur"
                    + " E-Rezepte des Patienten mit der KVNR aus dem Prüfungsnachweis enthalten."));
    return step.predicate(verify).accept();
  }

  static Stream<Arguments> validExamEvidence() {
    return ArgumentComposer.composeWith()
        .arguments(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL)
        .arguments(VsdmExamEvidenceResult.NO_UPDATES)
        .multiply(Arrays.stream(VsdmUpdateReason.values()).toList())
        .create();
  }

  static Stream<Arguments> invalidChecksums() {
    return ArgumentComposer.composeWith()
        .arguments(
            "invalider Identifier in der Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) ->
                    VsdmExamEvidence.asOnlineMode(service, egk).checksumWithInvalidManufacturer(),
            OperationOutcomeMessages.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "invalide Version in der Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) ->
                    VsdmExamEvidence.asOnlineMode(service, egk).checksumWithInvalidVersion(),
            OperationOutcomeMessages.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "invalider KVNR in der Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) ->
                    VsdmExamEvidence.asOnlineMode(service, egk).checksumWithInvalidKvnr(),
            OperationOutcomeMessages.FAILED_PARSING_PNW)
        .arguments(
            "ungültiger HMac-Key für die Signatur der Prüfziffer", // invalid hMacKey
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) -> VsdmExamEvidence.asOnlineTestMode(egk),
            OperationOutcomeMessages.PROOF_OF_PRESENCE_ERROR_SIG)
        .arguments(
            "abgelaufener Zeitstempel -30 Minuten in der Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) ->
                    VsdmExamEvidence.asOnlineMode(service, egk).withExpiredTimestamp(),
            OperationOutcomeMessages.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
        .arguments(
            "invalider Zeitstempel +40 Minuten in der Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) ->
                    VsdmExamEvidence.asOnlineMode(service, egk).withInvalidTimestamp(),
            OperationOutcomeMessages.PROOF_OF_PRESENCE_INVALID_TIMESTAMP)
        .arguments(
            "ohne Prüfziffer",
            (BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder>)
                (service, egk) -> VsdmExamEvidence.asOfflineMode(),
            OperationOutcomeMessages.WITHOUT_CHECKSUM)
        .multiply(
            List.of(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL, VsdmExamEvidenceResult.NO_UPDATES))
        .create();
  }

  private void verifyAuditEvent(ErxAuditEvent.Representation representation) {
    val auditEventVerifier = AuditEventVerifier.forPharmacy(pharmacy).build();
    val downloadedAuditEvents = patient.performs(DownloadAuditEvent.orderByDateDesc());
    patient.attemptsTo(
        Verify.that(downloadedAuditEvents)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.firstElementCorrespondsTo(representation))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_01")
  @ParameterizedTest(
      name = "[{index}] -> Abrufen von Tasks als Apotheker mit validen Prüfungsnachweis {1}")
  @DisplayName("Abrufen von Tasks als Apotheker mit validen Prüfungsnachweis {1}")
  @MethodSource("validExamEvidence")
  void validExamEvidences(VsdmUpdateReason reason, VsdmExamEvidenceResult result) {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .checksumWithUpdateReason(reason)
            .generate(result);
    val response = pharmacy.performs(DownloadReadyTask.withExamEvidence(examEvidence));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(TaskBundleVerifier.doesContainsErxTasksWithoutQES(ErpAfos.A_23452))
            .and(TaskBundleVerifier.doesNotContainsExpiredErxTasks(ErpAfos.A_23452))
            .isCorrect());
    verifyAuditEvent(ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL);
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_02")
  @Test
  @DisplayName("Egk in der Apotheke - ohne Prüfungsnachweis")
  void withoutExamEvidence() {
    val response = pharmacy.performs(DownloadReadyTask.withoutExamEvidence(patient.getKvnr()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23450)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_03")
  @Test
  @DisplayName("Egk in der Apotheke - mit Prüfungsnachweis ABC (invalid)")
  void invalidExamEvidence() {
    val response = pharmacy.performs(DownloadReadyTask.withInvalidExamEvidence());
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23450)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Egk in der Apotheke - Prüfungsnachweis {0} ohne Prüfziffer und ohne"
              + " QueryParameter KVNR")
  @DisplayName(
      "Egk in der Apotheke - Prüfungsnachweis {0} ohne Prüfziffer und ohne QueryParameter KVNR")
  @EnumSource(
      value = VsdmExamEvidenceResult.class,
      names = {"ERROR_EGK"},
      mode = EnumSource.Mode.EXCLUDE)
  void examEvidencesWithoutChecksum(VsdmExamEvidenceResult result) {
    val action =
        DownloadReadyTask.withExamEvidence(VsdmExamEvidence.asOfflineMode().generate(result));
    val response = pharmacy.performs(action);

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_25206)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_05")
  @ParameterizedTest(
      name =
          "[{index}] ->Egk in der Apotheke - Prüfungsnachweis {0} mit invalider Prüfziffer ({1})")
  @DisplayName("Egk in der Apotheke - Prüfungsnachweis {0} mit invalider Prüfziffer ({1})")
  @MethodSource("invalidChecksums")
  void invalidChecksum(
      VsdmExamEvidenceResult result,
      String explanation,
      BiFunction<VsdmService, Egk, VsdmExamEvidence.VsdmExamEvidenceBuilder> evidenceBuilder,
      OperationOutcomeMessages expectedErrorMessage) {

    val examEvidence = evidenceBuilder.apply(vsdmService, patient.getEgk()).generate(result);
    val response = pharmacy.performs(DownloadReadyTask.withExamEvidence(examEvidence));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23451)
            .hasResponseWith(returnCode(403))
            .and(
                operationOutcomeHasDetailsText(
                    expectedErrorMessage.getText(), expectedErrorMessage.getReq()))
            .isCorrect());

    verifyAuditEvent(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL);
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_06")
  @ParameterizedTest(
      name = "[{index}] ->Egk in der Apotheke - AcceptPN3 und QueryParameter KVNR {0}")
  @DisplayName("Egk in der Apotheke - AcceptPN3 und QueryParameter KVNR {0}")
  @ValueSource(booleans = {true, false})
  void acceptPN3(boolean withKvnr) {
    // PN3
    val examEvidencePN3 =
        VsdmExamEvidence.asOfflineMode().generate(VsdmExamEvidenceResult.ERROR_EGK);
    val response =
        pharmacy.performs(
            withKvnr
                ? DownloadReadyTask.withExamEvidence(examEvidencePN3, patient.getKvnr())
                : DownloadReadyTask.withExamEvidence(examEvidencePN3));

    if (pn3Activate && withKvnr) {
      pharmacy.attemptsTo(
          Verify.that(response)
              .withExpectedType()
              .hasResponseWith(returnCode(202))
              .and(TaskBundleVerifier.doesContainsErxTasksWithoutQES(ErpAfos.A_25209))
              .and(TaskBundleVerifier.doesNotContainsExpiredErxTasks(ErpAfos.A_25209))
              .isCorrect());
    } else {
      pharmacy.attemptsTo(
          Verify.that(response)
              .withOperationOutcome(withKvnr ? ErpAfos.A_25207 : ErpAfos.A_25208)
              .hasResponseWith(returnCode(withKvnr ? 454 : 455))
              .isCorrect());
    }

    if (withKvnr) {
      verifyAuditEvent(
          pn3Activate
              ? ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL_PN3
              : ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL_PN3);
    }
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_07")
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
            .withOperationOutcome(ErpAfos.A_19113)
            .responseWith(returnCodeIs(403))
            .isCorrect());
    patient.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_08")
  @Test
  @DisplayName(
      "Abrufen von Rezepten als Apotheker ohne vorheriges ACCEPT, daher kein Secret und nur ein"
          + " AccessCode")
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
    patient.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_09")
  @Test
  @DisplayName("Egk in der Apotheke - Nur Abruf von GKV E-Rezepten möglich")
  void gkvInsuranceType() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    patient.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());

    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .generate(VsdmExamEvidenceResult.NO_UPDATES);
    val response = pharmacy.performs(DownloadReadyTask.withExamEvidence(examEvidence));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .responseWith(returnCodeIs(200))
            .and(TaskBundleVerifier.containsExclusivelyTasksWithGKVInsuranceType())
            .isCorrect());
    patient.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_10")
  @ParameterizedTest(
      name =
          "[{index}] ->Egk in der Apotheke - Keine Verwendung der KVNR bei Prüfungsnachweis {0} aus"
              + " Query Parameter")
  @DisplayName(
      "Egk in der Apotheke - Keine Verwendung der KVNR bei Prüfungsnachweis {0} aus Query"
          + " Parameter")
  @EnumSource(
      value = VsdmExamEvidenceResult.class,
      names = {"UPDATES_SUCCESSFUL", "NO_UPDATES"},
      mode = EnumSource.Mode.INCLUDE)
  void useKvnrFromPN(VsdmExamEvidenceResult result) {
    val egk = mock(Egk.class);
    when(egk.getKvnr()).thenReturn("C000500021");
    val examEvidence = VsdmExamEvidence.asOnlineMode(vsdmService, egk).generate(result);
    val response =
        pharmacy.performs(DownloadReadyTask.withExamEvidence(examEvidence, patient.getKvnr()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .responseWith(returnCodeIs(200))
            .and(hasNoTasks())
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_11")
  @Test
  @DisplayName("Get-Task ohne AccessCode ist nicht möglich")
  void getTaskWithoutACIsForbidden() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    pharmacy.performs(AcceptPrescription.forTheTask(task));
    val response =
        pharmacy.performs(GetPrescriptionById.withTaskId(task.getTaskId()).withoutAuthentication());
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_24177)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeHasDetailsText(
                    "Neither AccessCode(ac) nor secret provided as URI-Parameter.",
                    ErpAfos.A_24177))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_12")
  @Test
  @DisplayName("Get-Task mit falschem AccessCode ist nicht möglich")
  void getTaskWithWrongACIsForbidden() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    pharmacy.performs(AcceptPrescription.forTheTask(task));
    val response =
        pharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(AccessCode.random()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_24177)
            .responseWith(returnCodeIs(403))
            .and(operationOutcomeHasDetailsText("AccessCode mismatch", ErpAfos.A_24177))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_13")
  @Test
  @DisplayName("Get-Task mit richtigem AccessCode ohne vorheriges Accept nicht möglich")
  void getTaskWithAC() {
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    val activation = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val task = activation.getExpectedResponse();
    val response =
        pharmacy.performs(
            GetPrescriptionById.withTaskId(task.getTaskId()).withAccessCode(task.getAccessCode()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_24178)
            .responseWith(returnCodeIs(412))
            .and(operationOutcomeHasDetailsText("Task not in-progress.", ErpAfos.A_24178))
            .isCorrect());
  }

  @RequiredArgsConstructor
  @Getter
  private enum OperationOutcomeMessages {
    PROOF_OF_PRESENCE_ERROR_SIG(
        ErpAfos.A_23456,
        "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Fehler bei Prüfung der"
            + " HMAC-Sicherung)."),
    INVALID_PNW(ErpAfos.A_23454, "Missing or invalid PNW query parameter"),
    INVALID_CHECKSUM_SIZE(ErpAfos.A_23454, "Invalid size of Prüfziffer"),
    PROOF_OF_PRESENCE_INVALID_TIMESTAMP(
        ErpAfos.A_23451,
        "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Zeitliche Gültigkeit"
            + " des Anwesenheitsnachweis überschritten)."),
    FAILED_PARSING_PNW(ErpAfos.A_23454, "Failed parsing PNW XML."),
    WITHOUT_CHECKSUM(
        ErpAfos.A_23455,
        "Anwesenheitsnachweis konnte nicht erfolgreich durchgeführt werden (Prüfziffer fehlt im"
            + " VSDM Prüfungsnachweis oder ungültiges Ergebnis im Prüfungsnachweis).");
    private final RequirementsSet req;
    private final String text;
  }
}
