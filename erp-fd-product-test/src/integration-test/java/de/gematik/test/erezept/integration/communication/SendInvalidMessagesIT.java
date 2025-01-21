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

package de.gematik.test.erezept.integration.communication;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.fuzzing.erx.ErxCommunicationPayloadManipulatorFactory.getCommunicationPayloadManipulators;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Communication Tests")
@Tag("Communication")
public class SendInvalidMessagesIT extends ErpTest {

  public static final int MAX_STRING_LENGTH_500 = 500;
  public static final int MAX_STRING_LENGTH_100 = 100;
  public static final int MAX_STRING_LENGTH_128 = 128;
  private static final String INVALID_JSON_PAYLOAD =
      "Invalid payload: does not conform to expected JSON schema: validation of JSON document"
          + " failed";

  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

  @Actor(name = "Hanna Bäcker")
  private PatientActor alternativPatient;

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doc;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharma;

  private static Stream<Arguments> communicationTestComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, PrescriptionAssignmentKind.class)
        .multiply(1, SupplyOptionsType.class)
        .create();
  }

  private static Stream<Arguments> communicationTestComposerForSupplyOption() {
    return ArgumentComposer.composeWith()
        .arguments(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, SupplyOptionsType.SHIPMENT)
        .arguments(PrescriptionAssignmentKind.PHARMACY_ONLY, SupplyOptionsType.ON_PREMISE)
        .arguments(PrescriptionAssignmentKind.PHARMACY_ONLY, SupplyOptionsType.DELIVERY)
        .create();
  }

  public static String getRandomString(int length) {
    StringBuilder result = new StringBuilder();
    for (int iter = 0; iter < length; iter++) {
      result.append((char) (new SecureRandom().nextInt(26) + 'a'));
    }
    return result.toString();
  }

  /**
   * pickUpCodeHR Optional, Wenn gesetzt, muss das Attribut supplyOptionsType den Wert "onPremise"
   * haben und die Zeichenlänge darf maximal 8 Zeichen betragen. pickUpCodeDMC Optional. Wenn
   * gesetzt, muss das Attribut supplyOptionsType den Wert "onPremise" haben und die Zeichenlänge
   * darf maximal 128 Zeichen betragen.
   */
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content als"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die CommunicationReply der Apotheke genauer die"
          + " Stringlänge Hint (max 500) korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithToLongString(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            supplyOptionsType, getRandomString(MAX_STRING_LENGTH_500 + 1));
    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Patientin schickt eine Communication mit invalidem (501 Zeichen langem)"
              + " Json-Content mit {0} und SupplyOption {1} an die Stadtapotheke !")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst CommunicationDispenseRequest des Patienten"
          + " genauer die Stringlänge Hint (max 500) korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithToLongString(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = getTask(assignmentKind);

    val disReqMessage =
        new CommunicationDisReqMessage(
            supplyOptionsType, getRandomString(MAX_STRING_LENGTH_500 + 1));
    val response2 =
        patient.performs(
            SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(disReqMessage));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die Version"
          + " > 1 korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithIncorrectVersion(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            new Random().nextInt(2, 999999),
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_500),
            null,
            null,
            null);
    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die Version > 1 korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectVersion(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = getTask(assignmentKind);
    val cDRM2 =
        new CommunicationDisReqMessage(
            new Random().nextInt(2, 1999999),
            supplyOptionsType.getLabel(),
            "patientName",
            null,
            getRandomString(MAX_STRING_LENGTH_500),
            null);
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_05_A")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption 'zuwerfen' an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " SupplyOption korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithIncorrectSupplyOption(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            1, "zuwerfen", getRandomString(MAX_STRING_LENGTH_500), null, null, null);

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_05_B")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst die CommunicationReply der Apotheke und genauer die"
          + " SupplyOption korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithIncorrectSupplyOptionEqualsNoSupplyOption(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            1, "", getRandomString(MAX_STRING_LENGTH_500), null, null, null);

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_06_A")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption 'zuwerfen' an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die SupplyOption korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectSupplyOption(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = getTask(assignmentKind);

    val cDRM2 =
        new CommunicationDisReqMessage(
            1, "zuwerfen", "patientName", null, getRandomString(MAX_STRING_LENGTH_500), null);
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_06_B")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die SupplyOption korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectSupplyOptionAsNull(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = getTask(assignmentKind);
    val cDRM2 =
        new CommunicationDisReqMessage(
            1, "", "patientName", null, getRandomString(MAX_STRING_LENGTH_500), null);
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " PickUpCodeDCM Länge validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithToIncorrectPickUpCodeDCM(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_500),
            null,
            null,
            getRandomString(MAX_STRING_LENGTH_128 + 1)); // max allowed 128

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDisenseRequest des Patienten und genauer"
          + " Länge des Namens korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectNameLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = getTask(assignmentKind);
    val cDRM2 =
        new CommunicationDisReqMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_100 + 1),
            null,
            getRandomString(MAX_STRING_LENGTH_500),
            null);
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_09")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die Version"
          + " als String ablehnt")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithToIncorrectVersionAsString(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = getTask(assignmentKind);
    val manipulator = getCommunicationPayloadManipulators();
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_500),
            null,
            null,
            null);

    val response2 =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(prescTask)
                .addManipulator(manipulator)
                .asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_10")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die Version als String ablehnt")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectVersionAsString(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    val prescTask = getTask(assignmentKind);
    val manipulator = getCommunicationPayloadManipulators();

    val cDRM2 =
        new CommunicationDisReqMessage(
            1,
            supplyOptionsType.getLabel(),
            "patientName",
            null,
            getRandomString(MAX_STRING_LENGTH_500),
            null);
    val response2 =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(prescTask)
                .addManipulator(manipulator)
                .asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_11")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " pickUpCodeHR Länge validiert und eine SupplyOption onPremise gesetzt ist")
  @MethodSource("communicationTestComposerForSupplyOption")
  void shouldValidatePharmaciesCommunicationWithToIncorrectPickUpCodHR(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = getTask(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_500),
            null,
            "123456789", // only length of 8 is allowed for pickUpCodeHR
            null);

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_12")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die URL"
          + " Länge validiert ")
  @MethodSource("communicationTestComposerForSupplyOption")
  void shouldValidatePharmaciesCommunicationWithIncorrectUrlLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = getTask(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(MAX_STRING_LENGTH_500),
            getRandomString(MAX_STRING_LENGTH_500 + 1),
            null, // only length of 8 is allowed for pickUpCodeHR
            null);

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_13")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit invalidem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die Phone länge")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithIncorrectPhoneLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    val prescTask = getTask(assignmentKind);
    val cDRM2 =
        new CommunicationDisReqMessage(
            1,
            supplyOptionsType.getLabel(),
            null,
            null,
            getRandomString(MAX_STRING_LENGTH_500),
            getRandomString(32 + 1)); // max allowd Stringlength == 32
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_14")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Patient:in schickt eine Communication mit für ein fremdes Rezept an die"
              + " Stadtapotheke !")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst CommunicationDispenseRequest des Patienten"
          + " genauer die HeaderParameter korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithHeadderParams(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = getTask(assignmentKind);

    val disReqMessage =
        new CommunicationDisReqMessage(
            supplyOptionsType, getRandomString(MAX_STRING_LENGTH_500 + 1));
    val response =
        alternativPatient.performs(
            SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(disReqMessage));
    alternativPatient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome()
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeHasDetailsText(
                    "Header must contain an access code", ErpAfos.A_19520))
            .isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  private ErxTask getTask(PrescriptionAssignmentKind assignmentKind) {
    return doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle())
        .getExpectedResponse();
  }
}
