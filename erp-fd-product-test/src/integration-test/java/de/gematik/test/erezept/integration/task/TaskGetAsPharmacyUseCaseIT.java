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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.AuditEventVerifier;
import de.gematik.test.core.expectations.verifier.TaskBundleVerifier;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.toggle.EgkPharmacyAcceptPN3Toggle;
import de.gematik.test.erezept.toggle.EgkPharmacyEnforceHcvCheck;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidence.VsdmExamEvidenceBuilder;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmExamEvidenceResult;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept abrufen als Apotheker")
@Tag("EGKinDerApotheke")
@Execution(ExecutionMode.SAME_THREAD)
class TaskGetAsPharmacyUseCaseIT extends ErpTest {

  private static boolean tasksInitialized = false;

  private static final Boolean PN3_ACTIVATE =
      featureConf.getToggle(new EgkPharmacyAcceptPN3Toggle());
  private static final Boolean ENFORCE_HCV_CHECK =
      featureConf.getToggle(new EgkPharmacyEnforceHcvCheck());

  private final VsdmService vsdmService = config.getSoftKonnVsdmService();

  @Actor(name = "Tina Kleinschmidt")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  static Stream<Arguments> validExamEvidenceParameters() {
    return ArgumentComposer.composeWith()
        .arguments(VsdmExamEvidenceResult.UPDATES_SUCCESSFUL)
        .arguments(VsdmExamEvidenceResult.NO_UPDATES)
        .multiply(Arrays.stream(VsdmCheckDigitVersion.values()).toList())
        .create();
  }

  static Stream<Arguments> invalidCheckDigits() {
    return ArgumentComposer.composeWith()
        .arguments(
            "invalide Betreiberkennung in der Prüfziffer",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                builder -> builder.with(VsdmService.CheckDigitConfiguration.INVALID_MANUFACTURER),
            false)
        .arguments(
            "invalide Key-Version für die Prüfziffer",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                builder -> builder.with(VsdmService.CheckDigitConfiguration.INVALID_KEY_VERSION),
            false)
        .arguments(
            "invalider KVNR in der Prüfziffer",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                builder -> builder.with(VsdmService.CheckDigitConfiguration.INVALID_KVNR),
            false)
        .arguments(
            "ungültiger HMac-Key", // invalid hMacKey
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                builder -> builder.with(VsdmService.CheckDigitConfiguration.INVALID_KEY),
            false)
        .arguments(
            "abgelaufener Zeitstempel -30 Minuten in der Prüfziffer",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                VsdmExamEvidenceBuilder::withExpiredIatTimestamp,
            true)
        .arguments(
            "invalider Zeitstempel +40 Minuten in der Prüfziffer",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                VsdmExamEvidenceBuilder::withInvalidIatTimestamp,
            true)
        .multiply(List.of(VsdmCheckDigitVersion.V1, VsdmCheckDigitVersion.V2))
        .arguments(
            VsdmCheckDigitVersion.V2,
            "mit einer ungültigen (Revoked) Egk",
            (Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder>)
                builder -> builder.with(VsdmService.CheckDigitConfiguration.REVOKED_EGK),
            true)
        .create();
  }

  private void verifyAuditEvent(ErxAuditEvent.Representation representation) {
    val auditEventVerifier = AuditEventVerifier.forPharmacy(pharmacy).build();
    val timestamp = Instant.now();
    val response = patient.performs(DownloadAuditEvent.orderByDateDesc());
    if (response.getResponse().isValidPayload()) {
      val auditEvents = response.getExpectedResponse().getAuditEvents();
      auditEvents.stream()
          .filter(ae -> ae.getRecorded().toInstant().isAfter(timestamp.minusSeconds(5)))
          .forEach(
              auditEvent ->
                  log.debug(
                      format(
                          "{1} Versichertenprotokoll mit \"{0}\"",
                          auditEvent.getFirstText(),
                          auditEvent.getRecorded().toInstant().truncatedTo(ChronoUnit.SECONDS))));
    }

    patient.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.contains(representation, timestamp))
            .isCorrect());
  }

  private void validateActualResponseFor(
      ErpInteraction<ErxTaskBundle> response, boolean withAuditEvent, boolean validateForPn3) {
    val req = validateForPn3 ? ErpAfos.A_25209 : ErpAfos.A_23452;
    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(validateForPn3 ? 202 : 200))
            .and(TaskBundleVerifier.doesNotContainQES(req))
            .and(TaskBundleVerifier.doesNotContainExpiredTasks(req))
            .and(TaskBundleVerifier.containsOnlyTasksWith(Task.TaskStatus.READY, req))
            .and(TaskBundleVerifier.containsOnlyTasksWith(PrescriptionFlowType.FLOW_TYPE_160, req))
            .and(TaskBundleVerifier.containsOnlyTasksFor(patient.getKvnr(), req))
            .isCorrect());
    if (withAuditEvent) {
      verifyAuditEvent(
          validateForPn3
              ? ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL_PN3
              : ErxAuditEvent.Representation.PHARMACY_GET_TASK_SUCCESSFUL);
    }
  }

  private void initTasks() {
    if (tasksInitialized) {
      return;
    }
    // Task in status ready
    val doctor = this.getDoctorNamed("Adelheid Ulmenwald");
    doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());

    // Task in Status In-Progress
    var task = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    pharmacy.performs(AcceptPrescription.forTheTask(task.getExpectedResponse()));

    // Task in Status close
    task = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val acceptedTask = pharmacy.performs(AcceptPrescription.forTheTask(task.getExpectedResponse()));
    pharmacy.performs(ClosePrescription.acceptedWith(acceptedTask));

    // Task with InsuranceTypeDe.PKV
    patient.changePatientInsuranceType(InsuranceTypeDe.PKV);
    doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());

    tasksInitialized = true;
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}, PZ Version {1}")
  @DisplayName(
      "Egk in der Apotheke - Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0}, PZ Version"
          + " {1}")
  @MethodSource("validExamEvidenceParameters")
  void shouldReturnAllOpenTasksForPatient(
      VsdmCheckDigitVersion checkDigitVersion, VsdmExamEvidenceResult result) {
    initTasks();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .with(checkDigitVersion)
            .build(result);
    val response = pharmacy.performs(DownloadReadyTask.with(examEvidence, patient.getEgk()));
    validateActualResponseFor(response, true, false);
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_02")
  @Test
  @DisplayName("Egk in der Apotheke - Abrufen von Tasks als Apotheker mit Prüfungsnachweis 3")
  void shouldReturnAllOpenTaskWithPN3() {
    initTasks();
    val examEvidencePN3 = VsdmExamEvidence.asOfflineMode().build(VsdmExamEvidenceResult.ERROR_EGK);
    val response = pharmacy.performs(DownloadReadyTask.with(examEvidencePN3, patient.getEgk()));
    if (!PN3_ACTIVATE) {
      pharmacy.attemptsTo(
          Verify.that(response)
              .withOperationOutcome(ErpAfos.A_25206)
              .hasResponseWith(returnCode(454))
              .isCorrect());
      verifyAuditEvent(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL_PN3);
    } else {
      validateActualResponseFor(response, true, true);
    }
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_03")
  @Test
  @DisplayName("Egk in der Apotheke - Request ohne pnw QueryParameter")
  void withoutPwnParameter() {
    val response =
        pharmacy.performs(
            DownloadReadyTask.withoutPnwParameter(
                patient.getKvnr(),
                patient.getEgk().getInsuranceStartDate(),
                patient.getEgk().getOwnerData().getStreet()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23450)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_04")
  @Test
  @DisplayName("Egk in der Apotheke - Request ohne kvnr QueryParameter")
  void withoutKvnrParameter() {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    val response =
        pharmacy.performs(
            DownloadReadyTask.withoutKvnrParameter(
                examEvidence,
                patient.getEgk().getInsuranceStartDate(),
                patient.getEgk().getOwnerData().getStreet()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_25208)
            .hasResponseWith(returnCode(455))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_05")
  @ParameterizedTest
  @DisplayName("Egk in der Apotheke - Request ohne hcv QueryParameter")
  @EnumSource(value = VsdmCheckDigitVersion.class)
  void withoutHcvParameter(VsdmCheckDigitVersion checkDigitVersion) {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .with(checkDigitVersion)
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    val response =
        pharmacy.performs(DownloadReadyTask.withoutHcvParameter(examEvidence, patient.getKvnr()));
    if (ENFORCE_HCV_CHECK) {
      pharmacy.attemptsTo(
          Verify.that(response)
              .withOperationOutcome(ErpAfos.A_27346)
              .hasResponseWith(returnCode(457))
              .isCorrect());
    } else {
      validateActualResponseFor(response, false, false);
    }
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_06")
  @ParameterizedTest(
      name = "[{index}] -> Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} ohne PZ")
  @DisplayName(
      "Egk in der Apotheke - Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} ohne PZ")
  @EnumSource(
      value = VsdmExamEvidenceResult.class,
      names = "ERROR_EGK",
      mode = EnumSource.Mode.EXCLUDE)
  void withoutCheckDigit(VsdmExamEvidenceResult result) {
    val examEvidence = VsdmExamEvidence.asOfflineMode().build(result);
    val response = pharmacy.performs(DownloadReadyTask.with(examEvidence, patient.getEgk()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23455)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_07")
  @Test
  @DisplayName(
      "Egk in der Apotheke - Abrufen von Tasks als Apotheker mit invaliden Prüfungsnachweis")
  void invalidExamEvidence() {
    val response =
        pharmacy.performs(
            DownloadReadyTask.with(
                "ABC",
                patient.getKvnr(),
                patient.getEgk().getInsuranceStartDate(),
                patient.getEgk().getOwnerData().getStreet()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23450)
            .hasResponseWith(returnCode(403))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_08")
  @ParameterizedTest(
      name =
          "[{index}] ->Egk in der Apotheke - Abrufen von Tasks als Apotheker mit Prüfungsnachweis"
              + " {0} mit invalider Prüfziffer ({1})")
  @DisplayName(
      "Egk in der Apotheke - Abrufen von Tasks als Apotheker mit Prüfungsnachweis {0} mit invalider"
          + " Prüfziffer ({1})")
  @MethodSource("invalidCheckDigits")
  void invalidChecksum(
      VsdmCheckDigitVersion checkDigitVersion,
      String explanation,
      Function<VsdmExamEvidenceBuilder, VsdmExamEvidenceBuilder> additionalBuilderConfiguration,
      boolean withVerifyAuditEvent) {
    val evidenceBuilder =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk()).with(checkDigitVersion);
    val examEvidence =
        additionalBuilderConfiguration
            .apply(evidenceBuilder)
            .build(VsdmExamEvidenceResult.NO_UPDATES);

    val response = pharmacy.performs(DownloadReadyTask.with(examEvidence, patient.getEgk()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23451)
            .hasResponseWith(returnCode(403))
            .isCorrect());
    if (withVerifyAuditEvent) {
      verifyAuditEvent(ErxAuditEvent.Representation.PHARMACY_GET_TASK_UNSUCCESSFUL);
    }
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_09")
  @Test
  @DisplayName("Egk in der Apotheke - Nur Abruf von GKV E-Rezepten möglich")
  void gkvInsuranceType() {
    initTasks();
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    val response = pharmacy.performs(DownloadReadyTask.with(examEvidence, patient.getEgk()));

    validateActualResponseFor(response, false, false);
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_10")
  @Test
  @DisplayName("Egk in der Apotheke - Abweichung pz.hcv != url.hcv")
  void deviationHcvFromCheckDigitHcv() {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    val response =
        pharmacy.performs(
            DownloadReadyTask.with(examEvidence.encode(), patient.getKvnr(), "ABC", List.of()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_27347)
            .hasResponseWith(returnCode(458))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_11")
  @Test
  @DisplayName("Egk in der Apotheke - Abweichung pz.kvnr != url.kvnr")
  void deviationKvnrFromCheckDigitKvnr() {
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    val response =
        pharmacy.performs(
            DownloadReadyTask.with(
                examEvidence,
                KVNR.from("X110614233"), // KVNR von Sina
                patient.getEgk().getInsuranceStartDate(),
                patient.getEgk().getOwnerData().getStreet()));
    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_27287)
            .hasResponseWith(returnCode(456))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_12")
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

  @TestcaseId("ERP_TASK_GET_PHARMACY_13")
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

  @TestcaseId("ERP_TASK_GET_PHARMACY_14")
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

  @TestcaseId("ERP_TASK_GET_PHARMACY_15")
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

  @TestcaseId("ERP_TASK_GET_PHARMACY_16")
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

  @TestcaseId("ERP_TASK_GET_PHARMACY_17")
  @Test
  @DisplayName("Egk in der Apotheke - RateLimit - StatusCode 423")
  void rateLimit() {
    val pharmacyRateLimit = this.getPharmacyNamed("Christine Ulmendorfer");
    val examEvidence =
        VsdmExamEvidence.asOnlineMode(vsdmService, patient.getEgk())
            .build(VsdmExamEvidenceResult.NO_UPDATES);
    // The RateLimit is a counter of failed attempts for each pharmacy and will reset every 24
    // hours.
    // If more than 100 failed attempts are made the pharmacy will be blocked.
    // The counter will incremented with each response status code of 456 or 458.

    // A_27445: E-Rezept-Fachdienst - Rezepte lesen - Apotheke - Ratelimit pro Telematik-ID pro Tag
    for (int i = 0; i < 50; i++) {
      // a failed attempt with 456
      pharmacyRateLimit.performs(
          DownloadReadyTask.with(
              examEvidence,
              KVNR.from("X110614233"), // KVNR von Sina
              patient.getEgk().getInsuranceStartDate(),
              patient.getEgk().getOwnerData().getStreet()));
      // a failed attempt with 458
      pharmacyRateLimit.performs(
          DownloadReadyTask.with(examEvidence.encode(), patient.getKvnr(), "ABC", List.of()));
    }
    // a valid attempt
    val response =
        pharmacyRateLimit.performs(DownloadReadyTask.with(examEvidence, patient.getEgk()));
    pharmacyRateLimit.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_27446)
            .responseWith(returnCodeIs(423))
            .isCorrect());
  }
}
