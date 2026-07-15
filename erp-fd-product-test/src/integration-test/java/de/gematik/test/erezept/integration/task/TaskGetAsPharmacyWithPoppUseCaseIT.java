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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.AuditEventVerifier;
import de.gematik.test.core.expectations.verifier.TaskBundleVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxAuditEvent;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezepte abrufen als Apotheker mit einem Popp-Token")
@Tag("EGKinDerApotheke")
@Tag("WithPoPPToken")
class TaskGetAsPharmacyWithPoppUseCaseIT extends ErpTest {
  private static boolean tasksInitialized = false;

  @Actor(name = "Tina Kleinschmidt")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  private void initTasks() {
    if (tasksInitialized) return;

    doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());

    val kbvErpBundleFaker =
        KbvErpBundleFaker.builder().withMedication(KbvErpMedicationPZNFaker.asTPrescription());

    doctor.performs(
        IssuePrescription.forPatient(patient).asTPrescription(kbvErpBundleFaker.toBuilder()));

    var task = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    pharmacy.performs(AcceptPrescription.forTheTask(task.getExpectedResponse()));

    task = doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());
    val acceptedTask = pharmacy.performs(AcceptPrescription.forTheTask(task.getExpectedResponse()));
    pharmacy.performs(ClosePrescription.acceptedWith(acceptedTask));

    patient.changePatientInsuranceType(InsuranceTypeDe.PKV);
    doctor.performs(IssuePrescription.forPatient(patient).withRandomKbvBundle());

    tasksInitialized = true;
  }

  private void verifyAuditEvent(ErxAuditEvent.Representation representation) {
    val auditEventVerifier = AuditEventVerifier.forPharmacy(pharmacy).build();
    val timestamp = Instant.now();
    val response = patient.performs(DownloadAuditEvent.orderByDateDesc());
    patient.attemptsTo(
        Verify.that(response)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(auditEventVerifier.contains(representation, timestamp))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_01")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - Valid Token")
  void validPoppToken() {
    initTasks();

    val req = ErpAfos.A_22432;

    val poppToken = pharmacy.asksFor(GeneratePoppToken.forEgk(patient.getEgk()));

    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withExpectedType(req)
            .responseWith(returnCodeIs(200))
            .and(TaskBundleVerifier.doesNotContainQES(req))
            .and(TaskBundleVerifier.doesNotContainExpiredTasks(req))
            .and(TaskBundleVerifier.containsOnlyTasksWith(Task.TaskStatus.READY, req))
            .and(
                TaskBundleVerifier.containsOnlyTasksWith(
                    req, PrescriptionFlowType.FLOW_TYPE_160, PrescriptionFlowType.FLOW_TYPE_166))
            .and(TaskBundleVerifier.containsOnlyTasksFor(patient.getKvnr(), req))
            .isCorrect());

    verifyAuditEvent(ErxAuditEvent.Representation.PHARMACY_GET_TASK_WITH_POPP_SUCCESSFUL);
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_02")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - Zeitstempel iat älter als 30min")
  void invalidIAT() {
    val poppToken = pharmacy.asksFor(GeneratePoppToken.forEgk(patient.getEgk()).expired());

    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23399)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "PoPPToken.iat is older than 30 minutes", ErpAfos.A_23399))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_03")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - Invalid Signatur")
  void invalidSignatur() {
    val poppToken = pharmacy.asksFor(GeneratePoppToken.forEgk(patient.getEgk()).wrongKey());

    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22432)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Verification failed - invalid signature or payload.", ErpAfos.A_22432))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_04")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - Invalid Kid")
  void invalidKid() {

    val poppToken = pharmacy.asksFor(GeneratePoppToken.forEgk(patient.getEgk()).invalidKid());

    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22432)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Could not retrieve public key for kid: invalidKid", ErpAfos.A_22432))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_05")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - Invalid Issuer")
  void invalidIssuer() {

    val poppToken = pharmacy.asksFor(GeneratePoppToken.forEgk(patient.getEgk()).invalidIssuer());

    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22432)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "PoPPToken.iss must contain the URL of the PoPP-Service from entity statement"
                        + " sub attribute: https://dummy.de!=https://popp.dev.poppservice.de",
                    ErpAfos.A_22432))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_06")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - As Doctor")
  void asDoctor() {

    val poppToken = doctor.asksFor(GeneratePoppToken.forEgk(patient.getEgk()));

    val response = doctor.performs(DownloadReadyTask.withPoppToken(poppToken));

    doctor.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22432)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDetailText(
                    "endpoint is forbidden for professionOID 1.2.276.0.76.4.50", ErpAfos.A_22432))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_GET_PHARMACY_WITH_POPP_07")
  @Test
  @DisplayName("Egk in der Apotheke mit PoPP - AccessToken.idNumber != PoPPToken.actorId")
  void differentActor() {
    val poppToken = doctor.asksFor(GeneratePoppToken.forEgk(patient.getEgk()));

    // AccessToken wird mit der SMC-B des Apothekers erstellt
    // PoPP-Token wird mit der SMC-B des Arztes erstellt
    val response = pharmacy.performs(DownloadReadyTask.withPoppToken(poppToken));

    pharmacy.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22432)
            .responseWith(returnCodeIs(403))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "PoPPToken.actorId does not match ACCESS_TOKEN.idNumber", ErpAfos.A_22432))
            .isCorrect());
  }
}
