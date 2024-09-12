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
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.security.SecureRandom;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Communication Send Tests")
public class SendMessagesIT extends ErpTest {

  private static final int MAX_STRING_LENGTH_500 = 500;

  private static final SecureRandom RANDOM = new SecureRandom();

  @Actor(name = "Leonie Hütter")
  private PatientActor patient;

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
      result.append((char) (RANDOM.nextInt(26) + 'a'));
    }
    return result.toString();
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content als"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die CommunicationReply der Apotheke genauer die"
          + " Stringlänge Hint akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithCorrectStringLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(prescTask)
                .asReply(
                    new CommunicationReplyMessage(supplyOptionsType, getRandomString(500)),
                    pharma));
    pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Patientin schickt eine Communication mit validem (500 Zeichen langem)"
              + " Json-Content mit {0} und SupplyOption {1} an die Stadtapotheke !")
  @DisplayName(
      "Es muss geprüft werden, dass der Fachdienst die CommunicationDispenseRequest des Patienten"
          + " genauer die Stringlänge Hint <= 500 akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithCorrectStringLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = prescribe(assignmentKind);
    val cDRM = new CommunicationDisReqMessage(supplyOptionsType, getRandomString(500));
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    val cDRM2 =
        new CommunicationDisReqMessage(
            supplyOptionsType, getRandomString(RANDOM.nextInt(0, MAX_STRING_LENGTH_500)));
    val response2 =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM2));
    patient.attemptsTo(Verify.that(response2).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die Version == 1 akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithCorrectVersion(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val cDRM =
        new CommunicationDisReqMessage(
            1, supplyOptionsType.getLabel(), patient.getName(), null, getRandomString(500), null);
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " SupplyOption akzeptiert")
  @MethodSource("communicationTestComposerForSupplyOption")
  void shouldValidatePharmaciesCommunicationWithCorrectSupplyOption(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = prescribe(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            1, supplyOptionsType.getLabel(), getRandomString(500), null, null, null);
    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " die SupplyOption akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithCorrectSupplyOption(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val cDRM =
        new CommunicationDisReqMessage(
            1, supplyOptionsType.getLabel(), "patientName", null, getRandomString(500), null);
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " PickUpCodeDCM Länge validiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithCorrectPickUpCodeDCM(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    final ErxTask prescTask = prescribe(assignmentKind);

    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            getRandomString(500),
            null,
            null,
            "5346a991-c5c6-49c8-b87b-4cdd255bbde4");
    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    if (supplyOptionsType.equals(SupplyOptionsType.ON_PREMISE)) {
      pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    } else {
      pharma.attemptsTo(
          Verify.that(response)
              .withOperationOutcome(ErpAfos.A_23879)
              .hasResponseWith(returnCode(400, ErpAfos.A_23879))
              .isCorrect());
    }
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " Länge des Namens < 100 akzepiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithCorrectNameLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val cDRM =
        new CommunicationDisReqMessage(
            1,
            supplyOptionsType.getLabel(),
            "Prof Dr. Dr. Johann-Wolfgang von und zu Burkstätten Grteifswalde",
            null,
            getRandomString(500),
            null);
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_8")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " pickUpCodeHR Länge == 8 akzepiert, wenn eine SupplyOption onPremise gesetzt ist")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWithCorrectPickUpCodHR(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = prescribe(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1, supplyOptionsType.getLabel(), getRandomString(500), null, "12345678", null);
    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));
    if (supplyOptionsType.equals(SupplyOptionsType.ON_PREMISE)) {
      pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    } else {
      pharma.attemptsTo(
          Verify.that(response)
              .withOperationOutcome(ErpAfos.A_23879)
              .hasResponseWith(returnCode(400, ErpAfos.A_23879))
              .isCorrect());
    }
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_9")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die URL"
          + " akzeptiert ")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWitCorrectUrlLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = prescribe(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            GemFaker.getFaker().internet().url(),
            null,
            null,
            null);
    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_10")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten und genauer"
          + " eine Telefonnummer akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithCorrectPhoneLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val cDRM =
        new CommunicationDisReqMessage(
            1, supplyOptionsType.getLabel(), null, null, getRandomString(500), "123456789");
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_11")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply der Apotheke und genauer die"
          + " EscapeQuotes akzeptiert ")
  @MethodSource("communicationTestComposer")
  void shouldValidatePharmaciesCommunicationWitEscapeQuotes(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {
    final ErxTask prescTask = prescribe(assignmentKind);
    val replyMessage =
        new CommunicationReplyMessage(
            1,
            supplyOptionsType.getLabel(),
            "\" Test \" Text \"mit jeder \" Menge escaped \"  Anführungszeichen \" ",
            null,
            null,
            null);
    val response =
        pharma.performs(SendMessages.to(patient).forTask(prescTask).asReply(replyMessage, pharma));

    pharma.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_10")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke schickt eine Communication mit validem Json-Content mit"
              + " {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest des Patienten mit"
          + " EscapeQuotes akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidatePatientCommunicationWithEscapeQuotes(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val prescTask = prescribe(assignmentKind);

    val cDRM =
        new CommunicationDisReqMessage(
            1,
            supplyOptionsType.getLabel(),
            " \"" + patient.getName() + " \"",
            List.of(" \"" + GemFaker.fakerStreetName() + " \""),
            "\" Test \" Text \"mit jeder \" Menge escaped \"  Anführungszeichen \" ",
            " \" 123456789");
    val response =
        patient.performs(SendMessages.to(pharma).forTask(prescTask).asDispenseRequest(cDRM));
    patient.attemptsTo(Verify.that(response).withExpectedType().isCorrect());
    // cleanup
    pharma.performs(
        ClosePrescription.acceptedWith(pharma.performs(AcceptPrescription.forTheTask(prescTask))));
  }

  private ErxTask prescribe(PrescriptionAssignmentKind assignmentKind) {
    return doc.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle())
        .getExpectedResponse();
  }
}
