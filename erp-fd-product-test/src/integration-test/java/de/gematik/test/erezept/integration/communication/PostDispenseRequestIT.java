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

package de.gematik.test.erezept.integration.communication;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.prescriptionHasStatus;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.taskIsInStatus;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Send DispenseRequest Tests")
@Tag("Communication")
@Tag("DispenseRequest")
public class PostDispenseRequestIT extends ErpTest {

  private static final CommunicationDisReqMessage USELESS_DSP_REQUEST =
      new CommunicationDisReqMessage(SupplyOptionsType.ON_PREMISE, "useless Questions");

  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doc;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharma;

  private static Stream<Arguments> communicationTestComposer() {
    return ArgumentComposer.composeWith(PrescriptionAssignmentKind.class).create();
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Eine Versicherte kann keine DispenseRequest für ein/e {0} an eine Apotheke"
              + " schicken wenn der Task.status = draft ist.")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die DispenseRequest der Versicherten im"
          + " task.status=draft nicht übermittelt")
  @MethodSource("communicationTestComposer")
  void shouldNotSendDispReqWithStatusIsDraft(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.performs(TaskCreate.forPatient(patient).ofAssignmentKind(assignmentKind));

    doc.attemptsTo(
        Verify.that(task)
            .withExpectedType()
            .and(taskIsInStatus(Task.TaskStatus.DRAFT, ErpAfos.A_19114))
            .isCorrect());

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task.getExpectedResponse())
                .asDispenseRequest(USELESS_DSP_REQUEST));
    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_26320)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText("Task has invalid status.", ErpAfos.A_26320))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Eine Versicherte kann keine DispenseRequest  für ein/e {0} an eine Apotheke"
              + " schicken wenn der Task.status = cancelled ist.")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die DispenseRequest der Versicherten im"
          + " task.status=cancelled nicht übermittelt")
  @MethodSource("communicationTestComposer")
  void shouldNotSendDispReqWithStatusIsCancelled(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val aborting = doc.performs(TaskAbort.asLeistungserbringer(task));
    doc.attemptsTo(
        Verify.that(aborting).withoutBody().hasResponseWith(returnCode(204)).isCorrect());

    val response =
        patient.performs(
            SendMessages.to(pharma).forTask(task).asDispenseRequest(USELESS_DSP_REQUEST));
    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_26320)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText("Task has invalid status.", ErpAfos.A_26320))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Eine Versicherte kann keine DispenseRequest  für ein/e {0} an eine Apotheke"
              + " schicken wenn der Task.status = in-progress ist.")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die DispenseRequest der Versicherten im"
          + " task.status=in-progress nicht übermittelt")
  @MethodSource("communicationTestComposer")
  void shouldNotSendDispReqWithStatusIsInProgress(PrescriptionAssignmentKind assignmentKind) {
    val task = doc.prescribeFor(patient, assignmentKind);
    val acceptance = pharma.performs(AcceptPrescription.forTheTask(task));

    val prescr =
        patient.performs(GetPrescriptionById.withTaskId(task.getTaskId()).withoutAuthentication());

    patient.attemptsTo(
        Verify.that(prescr)
            .withExpectedType()
            .and(prescriptionHasStatus(Task.TaskStatus.INPROGRESS, ErpAfos.A_19168))
            .isCorrect());

    val response =
        patient.performs(
            SendMessages.to(pharma).forTask(task).asDispenseRequest(USELESS_DSP_REQUEST));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_26320)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText("Task has invalid status.", ErpAfos.A_26320))
            .isCorrect());
    pharma.performs(ClosePrescription.acceptedWith(acceptance));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Eine Versicherte kann keine DispenseRequest  für ein/e {0} an eine Apotheke"
              + " schicken wenn die Prescription eine Mehrfachverordnung ist und der Beginn in der"
              + " Zukunft liegt.")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die DispenseRequest der Versicherten für die"
          + " MVO-Verordnung mit StartDatum in der Zukunft, nicht übermittelt")
  @MethodSource("communicationTestComposer")
  void shouldNotSendDspReqToFutureMVO(PrescriptionAssignmentKind assignmentKind) {
    val startDaysInFuture = 15;
    val task = activateMvoPrescription(assignmentKind, startDaysInFuture).getExpectedResponse();

    val response =
        patient.performs(
            SendMessages.to(pharma).forTask(task).asDispenseRequest(USELESS_DSP_REQUEST));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDetailText(
                    "Prescription is not fillable yet.", ErpAfos.A_26327))
            .isCorrect());
  }

  private ErpInteraction<ErxTask> activateMvoPrescription(
      PrescriptionAssignmentKind assignmentKind, int mvoStartsIn) {
    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    val mvo =
        MultiplePrescriptionExtension.asMultiple(1, 2).validThrough(mvoStartsIn, mvoStartsIn + 10);
    val kbvBundleBuilder =
        KbvErpBundleFaker.builder()
            .withMedication(medication)
            .withInsurance(patient.getInsuranceCoverage(), patient.getPatientData())
            .withPractitioner(doc.getPractitioner())
            .withMvo(mvo)
            .toBuilder();
    return doc.performs(
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(assignmentKind)
            .withKbvBundleFrom(kbvBundleBuilder));
  }
}
