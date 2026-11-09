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

package de.gematik.test.erezept.integration.communication.v3;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDiagnostics;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage.CommunicationDisReqMessageV3Builder;
import de.gematik.test.erezept.fhir.values.json.CommunicationPayloadValidation;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.function.Consumer;
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
@Tag("CommunicationV3Invalid")
@ExtendWith(SerenityJUnit5Extension.class)
class SendInvalidV3DispReqMessagesIT extends ErpTest {

  private static final String INVALID_JSON_PAYLOAD =
      "Invalid payload: does not conform to expected JSON schema";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

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
        .create();
  }

  /**
   * Creates a valid baseline V3 dispense-request message builder. Individual tests can override
   * fields to create the invalid payload they need to verify.
   */
  private CommunicationDisReqMessageV3Builder prefilledComTextBuilder() {

    return CommunicationDisReqMessage.forV3()
        .communicationType("text")
        .firstname("Max")
        .lastname("Mustermann")
        .address("Street 1")
        .postcode("12345")
        .city("Berlin")
        .country("DE")
        .phone("004915112345678")
        .email("test@example.de")
        .text("Please prepare medication");
  }

  /**
   * Creates a JSON payload manipulator for cases where the invalid payload cannot be represented
   * directly by the message builder.
   */
  private NamedEnvelope<FuzzingMutator<ErxCommunication>> createPayloadManipulator(
      String name, Consumer<ObjectNode> mutation) {

    return NamedEnvelope.of(
        name,
        (FuzzingMutator<ErxCommunication>)
            communication -> {
              val content = communication.getPayloadFirstRep().getContentStringType();

              try {
                val payload = (ObjectNode) OBJECT_MAPPER.readTree(content.getValue());
                mutation.accept(payload);
                content.setValue(OBJECT_MAPPER.writeValueAsString(payload));
              } catch (JsonProcessingException e) {
                throw new IllegalStateException(
                    "Could not mutate CommunicationDispenseRequest payload", e);
              }
            });
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " ungültigen Version als String mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen Version als String korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectStringVersion(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator("version-as-string", payload -> payload.put("version", "3"));

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics("version must be 'integer'", ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " ungültigen Version (2) mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen Version korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectUnsupportedVersion(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator("unsupported-version", payload -> payload.put("version", 2));

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Invalid payload version: 2", ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " ungültigen supplyOptionsType mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " ungültigen supplyOptionsType korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectUnsupportedSupplyOptionsType(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "unsupported-supply-options-type",
            payload -> payload.put("supplyOptionsType", "onPremise"));

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest ohne"
              + " supplyOptionsType mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 ohne das"
          + " Pflichtfeld supplyOptionsType korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingSupplyOptionsType(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "missing-supply-options-type",
            payload -> {
              payload.put("communicationType", "order");
              payload.remove("supplyOptionsType");
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Telefonnummer mit Pluszeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen Telefonnummer mit Pluszeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectPhoneWithPlus(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().phone("+4915112345678").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Telefonnummer mit Leerzeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen Telefonnummer mit Leerzeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectPhoneWithSpaces(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().phone("0049 151 12345678").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest ohne"
              + " address mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 ohne das"
          + " Pflichtfeld address korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingAddress(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().address("").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " fehlenden Postleitzahl mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " fehlenden Postleitzahl korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingPostcode(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().postcode("").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_09")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " fehlenden Stadt mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " fehlenden Stadt korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingCity(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().city("").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_10")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " fehlenden Ländercode mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " fehlenden Ländercode korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingCountry(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().country("").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_11")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " dreistelligen Ländercode mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " ungültigen dreistelligen Ländercode korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectThreeCharacterCountryCode(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().country("DEU").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_12")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest ohne"
              + " transactionID mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 ohne das"
          + " Pflichtfeld transactionID korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingTransactionId(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "missing-transaction-id",
            payload -> {
              payload.remove("transactionID");
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_13")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " ungültigen E-Mail-Adresse mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen E-Mail-Adresse korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidEmail(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().email("invalid-email").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_14")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " ungültigen communicationType mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " ungültigen communicationType korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectUnsupportedCommunicationType(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().communicationType("reply").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_15")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer "
              + "transactionID, die länger als 50 Zeichen ist, mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " transactionID, die länger als 50 Zeichen ist, korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectTransactionIdLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().transactionID("A".repeat(51)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_16")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest ohne Text bei"
              + " communicationType text mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 ohne das"
          + " Pflichtfeld text bei communicationType text korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingTextForTextCommunication(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "missing-text",
            payload -> {
              payload.put("hint", "xkcd:3299");
              payload.remove("text");
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_17")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " ungültigen transactionID mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen transactionID korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidTransactionId(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().transactionID("transactionID").build();
    val validator = new CommunicationPayloadValidation("erpcom/CommunicationDispReqPayloadV3.json");
    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_18")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Vornamen, der länger als 45 Zeichen ist, mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Vornamen, der länger als 45 Zeichen ist, korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectFirstnameLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().firstname("A".repeat(46)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_19")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Nachnamen, der länger als 45 Zeichen ist, mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Nachnamen, der länger als 45 Zeichen ist, korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectLastnameLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().lastname("A".repeat(46)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_20")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Adresse mit weniger als 3 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Adresse mit weniger als 3 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectAddressShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().address("AA").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_21")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Adresse mit mehr als 100 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Adresse mit mehr als 100 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectAddressLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().address("A".repeat(101)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_22")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Postleitzahl mit weniger als 3 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Postleitzahl mit weniger als 3 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectPostcodeShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().postcode("12").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_23")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Postleitzahl mit mehr als 10 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Postleitzahl mit mehr als 10 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectPostcodeLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().postcode("1".repeat(11)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_24")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer Stadt"
              + " mit weniger als 2 Zeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Stadt mit weniger als 2 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectCityShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().city("A").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_25")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Stadt mit mehr als 100 Zeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Stadt mit mehr als 100 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectCityLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().city("A".repeat(101)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_26")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Hint mit mehr als 100 Zeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Hint mit mehr als 100 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectHintLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "hint-longer-than-maximum", payload -> payload.put("hint", "A".repeat(101)));

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_27")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Text mit mehr als 800 Zeichen mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Text mit mehr als 800 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectTextLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "text-longer-than-maximum",
            payload -> {
              payload.put("hint", "xkcd:3299");
              payload.put("text", "A".repeat(801));
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_28")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " Telefonnummer mit mehr als 32 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " Telefonnummer mit mehr als 32 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectPhoneLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().phone("00" + "9".repeat(33)).build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_29")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " E-Mail-Adresse mit mehr als 70 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " E-Mail-Adresse mit mehr als 70 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectEmailLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().email("a".repeat(60) + "@example.de").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_30")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Nachnamen mit weniger als 2 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Nachnamen mit weniger als 2 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectLastnameShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().lastname("A").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_31")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einem"
              + " Vornamen mit weniger als 2 Zeichen mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einem"
          + " Vornamen mit weniger als 2 Zeichen korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectFirstnameShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().firstname("A").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_32")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit"
              + " communicationType text und hint mit {0} an die"
              + " Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit"
          + " communicationType text und hint korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectHintForCommunicationTypeText(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "hint-for-communication-type-text",
            payload -> {
              payload.put("communicationType", "text");
              payload.put("hint", "xkcd:3299");
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_33")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " fehlenden Stadt (NULL) bei communicationType order mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " fehlenden Stadt bei communicationType order korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectNullValueCity(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val manipulator =
        createPayloadManipulator(
            "null-city-for-communication-type-order",
            payload -> {
              payload.put("communicationType", "order");
              payload.putNull("city");
            });

    val response =
        patient.performs(
            SendMessages.to(pharma)
                .forTask(task)
                .addManipulator(manipulator)
                .asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_DISP_REQ_34")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit einer"
              + " ungültigen (alphabetisch) Telefonnummer mit {0} an die Stadtapotheke")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst eine CommunicationDispenseRequest in V3 mit einer"
          + " ungültigen (alphabetisch) Telefonnummer korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidPhoneValue(PrescriptionAssignmentKind assignmentKind) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().phone("UngültigeTelefonnummer").build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }
}
