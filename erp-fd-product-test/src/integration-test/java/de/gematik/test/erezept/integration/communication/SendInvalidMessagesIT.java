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

package de.gematik.test.erezept.integration.communication;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.*;
import static de.gematik.test.fuzzing.erx.ErxCommunicationPayloadManipulatorFactory.*;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
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
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500 + 1))
            .build();
    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val disReqMessage =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .hint(getRandomString(MAX_STRING_LENGTH_500 + 1))
            .build();

    val response2 =
        patient.performs(
            SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(disReqMessage));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .version(2) // invalid version > 1
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .build();
    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Invalid payload version: 2", ErpAfos.A_23879))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .version(2) // invalid version > 1
            .supplyOptionsType(supplyOptionsType)
            .name("patientName")
            .addressLines()
            .hint(null)
            .phone(null)
            .build();

    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Invalid payload version: 2", ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType("zuwerfen") // invalid supply option")
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .build();

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType("zuwerfen")
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .build();

    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType("zuwerfen")
            .name("patientName")
            .addressLines()
            .hint(null)
            .phone(null)
            .build();

    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(" ")
            .name("PatientName")
            .addressLines(List.of("address-line"))
            .phone("1234567890")
            .hint(getRandomString(MAX_STRING_LENGTH_500))
            .build();

    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .pickUpCodeDMC(getRandomString(MAX_STRING_LENGTH_128 + 1)) // invalid DMC (>128)
            .build();

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .name(getRandomString(MAX_STRING_LENGTH_100 + 1)) // invalid name
            .addressLines(List.of("address-line"))
            .phone("1234567890")
            .hint("hint")
            .build();

    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);
    val manipulator = getCommunicationPayloadManipulators();

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .version(2) // invalid version
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .build();

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
            .and(
                operationOutcomeContainsInDiagnostics("version must be 'integer'", ErpAfos.A_23879))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);
    val manipulator = getCommunicationPayloadManipulators();

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .name("patientName")
            .addressLines(List.of("address-line"))
            .phone(getRandomString(10))
            .hint("hint")
            .build();

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
            .and(
                operationOutcomeContainsInDiagnostics("version must be 'integer'", ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .pickUpCodeHR("123456789") // invalid: > 8 chars
            .build();

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val replyMessage =
        CommunicationReplyMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .infoText(getRandomString(MAX_STRING_LENGTH_500))
            .url(getRandomString(MAX_STRING_LENGTH_500 + 1)) // invalid URL (>500)
            .build();

    val response2 =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400, ErpAfos.A_23879))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
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

    val prescTask = doc.prescribeFor(patient, assignmentKind);

    val cDRM2 =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .name("patientName")
            .addressLines(List.of("some-address"))
            .phone(getRandomString(32 + 1)) // invalid phone (> 32)
            .hint("hint")
            .build();

    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));

    patient.attemptsTo(
        Verify.that(response2)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400, ErpAfos.A_23878))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
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

    final ErxTask prescTask = doc.prescribeFor(patient, assignmentKind);

    val disReqMessage =
        CommunicationDisReqMessage.forV1()
            .supplyOptionsType(supplyOptionsType)
            .hint(getRandomString(MAX_STRING_LENGTH_500 + 1))
            .build();

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
  }

  private static Stream<Arguments> getDspRequestManipulationComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, List.of(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV))
        .multiply(1, getCommunicationDspRequestSystemsManipulators())
        .create();
  }

  private static Stream<Arguments> getReplyManipulationComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, List.of(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV))
        .multiply(1, getCommunicationReplySystemsManipulators())
        .create();
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_15")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient versucht eine Nachricht für {0} und"
              + " mit falschen Systems: {1}  zu senden")
  @DisplayName(
      "Es muss geprüft werden, dass eine Patient oder eine Apotheke Nachrichten mit ungeslicedten"
          + " Systems nicht einstellen kann")
  @MethodSource("getDspRequestManipulationComposer")
  void shouldPostInvalidCommunicationsAsPatient(
      InsuranceTypeDe insuranceType, NamedEnvelope<FuzzingMutator<ErxCommunication>> manipulator) {

    patient.changePatientInsuranceType(insuranceType);

    val task = doc.prescribeFor(patient);

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(
                    CommunicationDisReqMessage.forV1()
                        .supplyOptionsType(SupplyOptionsType.SHIPMENT)
                        .hint("nope, we´ll get SMOK!")
                        .build()));

    patient.attemptsTo(
        Verify.that(response).withOperationOutcome().hasResponseWith(returnCode(400)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_15")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient versucht eine Nachricht für {0} und"
              + " mit falschen Systems: {1}  zu senden")
  @DisplayName(
      "Es muss geprüft werden, dass eine Apotheke Nachrichten mit ungesliceten"
          + " Systems nicht einstellen kann")
  @MethodSource("getReplyManipulationComposer")
  void shouldPostInvalidSenderCommunicationsAsPharmacy(
      InsuranceTypeDe insuranceType, NamedEnvelope<FuzzingMutator<ErxCommunication>> manipulator) {
    patient.changePatientInsuranceType(insuranceType);

    val task = doc.prescribeFor(patient);
    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(
                    CommunicationReplyMessage.forV1()
                        .supplyOptionsType(SupplyOptionsType.SHIPMENT)
                        .infoText("We can deliver a mask, too")
                        .build(),
                    pharma));

    pharma.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_22927)
            .hasResponseWith(returnCode(400))
            .isCorrect());
  }
}
