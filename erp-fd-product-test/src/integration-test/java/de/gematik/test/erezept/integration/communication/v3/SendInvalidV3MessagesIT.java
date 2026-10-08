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
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationPayloadType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
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
@Tag("CommunicationV3")
class SendInvalidV3MessagesIT extends ErpTest {

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

  private static String getRandomString(int length) {
    SecureRandom random = new SecureRandom();
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < length; i++) {
      sb.append((char) (random.nextInt(26) + 'a'));
    }
    return sb.toString();
  }

  private static final String INVALID_JSON_PAYLOAD =
      "Invalid payload: does not conform to expected JSON schema";

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 CommunicationDispenseRequest mit fehlendem"
              + " firstname mit {0} und SupplyOption {1} an die Stadtapotheke!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest in V3 Pflichtfelder"
          + " korrekt validiert, wenn das Feld „firstname“ fehlt")
  @MethodSource("communicationTestComposer")
  void shouldRejectMissingFirstname(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg =
        CommunicationDisReqMessage.forV3()
            .supplyOptionsType(supplyOptionsType)
            .firstname("")
            .lastname("Mustermann")
            .address("Street 1")
            .postcode("12345")
            .city("Berlin")
            .country("DE")
            .phone("123456789")
            .email("test@test.de")
            .text("hello")
            .build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 Communication mit invalidem"
              + " communicationType mit {0} und SupplyOption {1} an die Stadtapotheke!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest in V3 communicationType"
          + " korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidCommunicationType(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg =
        CommunicationDisReqMessage.forV3()
            .communicationType("Invalid")
            .firstname("Max")
            .lastname("Mustermann")
            .address("Street 1")
            .postcode("12345")
            .city("Berlin")
            .country("DE")
            .phone("123456789")
            .email("test@test.de")
            .text("hello")
            .build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23878))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Der Versicherte sendet eine V3 Communication mit zu langem text mit {0} und"
              + " SupplyOption {1} an die Stadtapotheke!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispenseRequest in V3 Längenlimits des"
          + " Texts korrekt validiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectTooLongText(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val msg =
        CommunicationDisReqMessage.forV3()
            .supplyOptionsType(supplyOptionsType)
            .text(getRandomString(801)) // > 800 invalid in v3
            .firstname("Max")
            .lastname("Mustermann")
            .address("Street 1")
            .postcode("12345")
            .city("Berlin")
            .country("DE")
            .phone("123456789")
            .email("test@test.de")
            .build();

    val response = patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(msg));

    patient.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23878)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Reply mit ungültigem readyForCollection"
              + " Wert mit {0} und SupplyOption {1} an den Versicherten!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply in V3 readyForCollection nur gültige"
          + " Enum-Werte akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidReadyForCollection(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.DELIVERY_STATUS.getLabel())
            .readyForCollecting("tomorrow_maybe") // invalid
            .text("test")
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    pharma.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Reply mit ungültigem totalAmount als"
              + " String mit {0} und SupplyOption {1} an den Versicherten!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply in V3 totalAmount nur als"
          + " numerischen Wert akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidTotalAmount(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val totalAmountAsStringManipulator =
        NamedEnvelope.of(
            "HelloWorld",
            (FuzzingMutator<ErxCommunication>)
                communication -> {
                  val content = communication.getPayloadFirstRep().getContentStringType();
                  content.setValue(
                      content
                          .getValue()
                          .replace(
                              "\"totalAmount\":10", "\"totalAmount\":\"invalid_total_amount\""));
                });

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.PAYMENT_INFO.getLabel())
            .totalAmount(10)
            .text("test")
            .build();

    val response =
        pharma.performs(
            SendMessages.to(patient)
                .forTask(task)
                .addManipulator(totalAmountAsStringManipulator)
                .asReply(request, pharma));

    pharma.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23877_02)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23877_02))
            .isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_SEND_INVALID_V3_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Reply mit ungültigem PaymentMethod type"
              + " {0} und SupplyOption {1}")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply in V3 nur erlaubte PaymentMethod"
          + " types akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldRejectInvalidPaymentMethodType(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationPayloadType.PAYMENT_INFO.getLabel())
            .paymentMethods(
                List.of(
                    new CommunicationReplyMessage.PaymentMethod(
                        "bitcoin", "BTC", "https://pay.example.com")))
            .text(getRandomString(200))
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    pharma.attemptsTo(
        Verify.that(response)
            .withOperationOutcome(ErpAfos.A_23879)
            .hasResponseWith(returnCode(400))
            .and(operationOutcomeContainsInDetailText(INVALID_JSON_PAYLOAD, ErpAfos.A_23879))
            .isCorrect());
  }
}
