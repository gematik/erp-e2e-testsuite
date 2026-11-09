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
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage.CommunicationReplyMessageV3Builder;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.core.FuzzingMutator;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import java.util.List;
import java.util.UUID;
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
class SendInvalidV3ReplyMessagesIT extends ErpTest {

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
   * Creates a valid baseline V3 reply message. Individual tests can override fields to create the
   * invalid payload they need to verify.
   */
  private CommunicationReplyMessageV3Builder prefilledComTextBuilder() {
    return CommunicationReplyMessage.forV3()
        .communicationType("text")
        .transactionID(UUID.randomUUID())
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
                throw new IllegalStateException("Could not mutate CommunicationReply payload", e);
              }
            });
  }

  /**
   * Removes properties belonging to other communication types and prepares the payload for the
   * selected communication type.
   */
  private void prepareCommunicationType(ObjectNode payload, String communicationType) {
    payload.put("communicationType", communicationType);
    payload.remove(
        List.of(
            "text",
            "url",
            "readyForCollection",
            "pickupCodeHR",
            "pickupCodeDMC",
            "deliveryStatus",
            "inTransportPosition",
            "inTransportETA",
            "totalAmount",
            "paymentMethods"));
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_01")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine Reply-Payload mit Version als String für {0}")
  @DisplayName("Es wird geprüft, dass eine Version als String korrekt abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectStringVersion(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator("version-as-string", payload -> payload.put("version", "3"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics("version must be 'integer'", ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_02")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine Reply-Payload mit Version 2 für {0}")
  @DisplayName("Es wird geprüft, dass eine ungültige Version korrekt abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectUnsupportedVersion(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator("unsupported-version", payload -> payload.put("version", 2));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(
                operationOutcomeContainsInDiagnostics(
                    "Invalid payload version: 2", ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_03")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen ungültigen communicationType für {0}")
  @DisplayName("Es wird geprüft, dass ein ungültiger communicationType korrekt abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectUnsupportedCommunicationType(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "unsupported-communication-type", payload -> payload.put("communicationType", "reply"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_04")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält communicationType link ohne url für {0}")
  @DisplayName("Es wird geprüft, dass eine fehlende URL bei communicationType link abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectLinkWithoutUrl(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "link-without-url",
            payload -> {
              prepareCommunicationType(payload, "link");
              payload.put("text", "Please open the link");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_05")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält communicationType text ohne text für {0}")
  @DisplayName("Es wird geprüft, dass das fehlende Pflichtfeld text abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingText(PrescriptionAssignmentKind assignmentKind) {

    val manipulator = createPayloadManipulator("missing-text", payload -> payload.remove("text"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_06")
  @ParameterizedTest(name = "[{index}] -> Der Fachdienst erhält einen ungültigen URL-Wert für {0}")
  @DisplayName("Es wird geprüft, dass ein ungültiges URL-Format abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidUrl(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "invalid-url",
            payload -> {
              prepareCommunicationType(payload, "link");
              payload.put("text", "Please open the link");
              payload.put("url", "invalid-url");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_07")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält reservationStatus ohne readyForCollection für {0}")
  @DisplayName("Es wird geprüft, dass das fehlende Pflichtfeld readyForCollection abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingReadyForCollection(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "missing-ready-for-collection",
            payload -> prepareCommunicationType(payload, "reservationStatus"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_08")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen ungültigen readyForCollection-Wert für {0}")
  @DisplayName("Es wird geprüft, dass ein ungültiger readyForCollection-Wert abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidReadyForCollection(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "invalid-ready-for-collection",
            payload -> {
              prepareCommunicationType(payload, "reservationStatus");
              payload.put("readyForCollection", "invalid");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_09")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält pickupCodeHR ohne pickupCodeHR für {0}")
  @DisplayName("Es wird geprüft, dass das fehlende Pflichtfeld pickupCodeHR abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingPickupCodeHR(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "missing-pickup-code-hr", payload -> prepareCommunicationType(payload, "pickupCodeHR"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_10")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen ungültigen pickupCodeHR für {0}")
  @DisplayName("Es wird geprüft, dass ungültige Zeichen in pickupCodeHR abgelehnt werden")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidPickupCodeHR(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "invalid-pickup-code-hr",
            payload -> {
              prepareCommunicationType(payload, "pickupCodeHR");
              payload.put("pickupCodeHR", "1234-567");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_11")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält pickupCodeDMC ohne pickupCodeDMC für {0}")
  @DisplayName("Es wird geprüft, dass das fehlende Pflichtfeld pickupCodeDMC abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingPickupCodeDMC(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "missing-pickup-code-dmc",
            payload -> prepareCommunicationType(payload, "pickupCodeDMC"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_12")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen zu kurzen pickupCodeDMC für {0}")
  @DisplayName("Es wird geprüft, dass pickupCodeDMC mit weniger als 8 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPickupCodeDMCShorterThanMinimum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "short-pickup-code-dmc",
            payload -> {
              prepareCommunicationType(payload, "pickupCodeDMC");
              payload.put("pickupCodeDMC", "1234567");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_13")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen ungültigen deliveryStatus für {0}")
  @DisplayName("Es wird geprüft, dass ein ungültiger deliveryStatus abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidDeliveryStatus(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "invalid-delivery-status",
            payload -> {
              prepareCommunicationType(payload, "deliveryStatus");
              payload.put("deliveryStatus", "invalid");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_14")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen nicht ganzzahligen totalAmount für {0}")
  @DisplayName("Es wird geprüft, dass ein nicht ganzzahliger totalAmount abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectNonIntegerTotalAmount(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "non-integer-total-amount",
            payload -> {
              prepareCommunicationType(payload, "paymentInfo");
              payload.put("totalAmount", 12.5);
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_15")
  @ParameterizedTest(name = "[{index}] -> Der Fachdienst erhält paymentMethods ohne method für {0}")
  @DisplayName("Es wird geprüft, dass ein fehlendes paymentMethods.method abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPaymentMethodWithoutMethod(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "payment-method-without-method",
            payload -> {
              prepareCommunicationType(payload, "paymentInfo");
              payload.put("totalAmount", 1200);
              val paymentMethod = OBJECT_MAPPER.createObjectNode();
              payload.set("paymentMethods", OBJECT_MAPPER.createArrayNode().add(paymentMethod));
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_16")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen ungültigen paymentMethods-Wert für {0}")
  @DisplayName("Es wird geprüft, dass eine ungültige paymentMethods.method abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidPaymentMethod(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "invalid-payment-method",
            payload -> {
              prepareCommunicationType(payload, "paymentInfo");
              payload.put("totalAmount", 1200);
              val paymentMethod = OBJECT_MAPPER.createObjectNode();
              paymentMethod.put("method", "bitcoin");
              payload.set("paymentMethods", OBJECT_MAPPER.createArrayNode().add(paymentMethod));
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_17")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Fachdienst erhält eine transactionID mit ungültigem UUID-Format für"
              + " {0}")
  @DisplayName("Es wird geprüft, dass eine transactionID mit ungültigem UUID-Format abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidTransactionId(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val manipulator =
        createPayloadManipulator(
            "invalid-transaction-id", payload -> payload.put("transactionID", "not-a-uuid"));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_18")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine zusätzliche unbekannte Eigenschaft für {0}")
  @DisplayName("Es wird geprüft, dass eine zusätzliche unbekannte Eigenschaft abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectAdditionalProperty(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "additional-property", payload -> payload.put("additionalProperties", true));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_19")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine Position ohne Latitude für {0}")
  @DisplayName("Es wird geprüft, dass eine Position ohne Latitude abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPositionWithoutLatitude(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "position-without-latitude",
            payload -> {
              prepareCommunicationType(payload, "deliveryStatus");
              payload.put("deliveryStatus", "inTransport");
              val position = OBJECT_MAPPER.createObjectNode();
              position.put("long", 13.405);
              payload.set("inTransportPosition", position);
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @Tag("FeatureComV3ForNextRelease")
  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_20")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine Position ohne Longitude für {0}")
  @DisplayName("Es wird geprüft, dass eine Position ohne Longitude abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPositionWithoutLongitude(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "position-without-longitude",
            payload -> {
              prepareCommunicationType(payload, "deliveryStatus");
              payload.put("deliveryStatus", "inTransport");
              val position = OBJECT_MAPPER.createObjectNode();
              position.put("lat", 52.52);
              payload.set("inTransportPosition", position);
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_21")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen Text mit mehr als 800 Zeichen für {0}")
  @DisplayName("Es wird geprüft, dass ein Text mit mehr als 800 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectTextLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator("text-too-long", payload -> payload.put("text", "A".repeat(801)));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_22")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine URL mit mehr als 500 Zeichen für {0}")
  @DisplayName("Es wird geprüft, dass eine URL mit mehr als 500 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectUrlLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "url-too-long",
            payload -> {
              prepareCommunicationType(payload, "link");
              payload.put("text", "Please open the link");
              payload.put("url", "https://" + "a".repeat(490) + ".de");
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_23")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält eine transactionID mit 51 Zeichen für {0}")
  @DisplayName("Es wird geprüft, dass eine transactionID mit mehr als 50 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectTransactionIdLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "transaction-id-too-long", payload -> payload.put("transactionID", "A".repeat(51)));

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_24")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen pickupCodeHR mit 9 Zeichen für {0}")
  @DisplayName("Es wird geprüft, dass ein pickupCodeHR mit mehr als 8 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPickupCodeHRLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "pickup-code-hr-too-long",
            payload -> {
              prepareCommunicationType(payload, "pickupCodeHR");
              payload.put("pickupCodeHR", "A".repeat(9));
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_REPLY_25")
  @ParameterizedTest(
      name = "[{index}] -> Der Fachdienst erhält einen pickupCodeDMC mit 2001 Zeichen für {0}")
  @DisplayName("Es wird geprüft, dass ein pickupCodeDMC mit mehr als 2000 Zeichen abgelehnt wird")
  @MethodSource("communicationTestComposer")
  void shouldRejectPickupCodeDMCLongerThanMaximum(PrescriptionAssignmentKind assignmentKind) {

    val manipulator =
        createPayloadManipulator(
            "pickup-code-dmc-too-long",
            payload -> {
              prepareCommunicationType(payload, "pickupCodeDMC");
              payload.put("pickupCodeDMC", "A".repeat(2001));
            });

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg = prefilledComTextBuilder().build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(manipulator)
                .asReply(msg, pharma));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }
}
