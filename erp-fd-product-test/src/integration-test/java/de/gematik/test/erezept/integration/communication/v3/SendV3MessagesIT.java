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

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.extensions.erp.CommunicationType;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
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
@DisplayName("Communication Send Tests")
@Tag("Communication")
public class SendV3MessagesIT extends ErpTest {

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

  // In „CommunicationDispReqPayloadV3“ enthält die Enumeration „SupplyOption“ nur die Werte
  // [„delivery“, „shipment“]
  private static Stream<Arguments> communicationDeliveryShipmentComposer() {
    return Stream.of(
        Arguments.of(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, SupplyOptionsType.DELIVERY),
        Arguments.of(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT, SupplyOptionsType.SHIPMENT),
        Arguments.of(PrescriptionAssignmentKind.PHARMACY_ONLY, SupplyOptionsType.DELIVERY),
        Arguments.of(PrescriptionAssignmentKind.PHARMACY_ONLY, SupplyOptionsType.SHIPMENT));
  }

  public static String getRandomString(int length) {
    SecureRandom random = new SecureRandom();
    StringBuilder result = new StringBuilder();
    for (int i = 0; i < length; i++) {
      result.append((char) (random.nextInt(26) + 'a'));
    }
    return result.toString();
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Communication mit gültigem Inhalt "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst  CommunicationReply V3 mit gültiger Textlänge "
          + " akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectTextLength(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationType.TEXT.getLabel())
            .transactionID(UUID.randomUUID())
            .text(getRandomString(300))
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));
    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Communication mit gültiger URL "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst  CommunicationReply V3 "
          + "mit Text und gültiger URL akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectUrl(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationType.LINK.getLabel())
            .transactionID(UUID.randomUUID())
            .text("text")
            .url("https://example.com")
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_03")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 ReservationStatus-Communication "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply V3 mit korrekte reservationStatus"
          + " akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectReservationStatus(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationType.RESERVATION_STATUS.getLabel())
            .transactionID(UUID.randomUUID())
            .readyForCollection("nextDay")
            .text(null)
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet einen gültigen pickupCodeHR "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply V3 "
          + "mit gültigem pickupCodeHR  akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectPickupCodeHR(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType("pickupCodeHR")
            .transactionID(UUID.randomUUID())
            .pickupCodeHR("12345678")
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet einen gültigen pickupCodeDMC "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply V3 "
          + "mit gültigem pickupCodeDMC akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectPickupCodeDMC(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType("pickupCodeDMC")
            .transactionID(UUID.randomUUID())
            .pickupCodeDMC("{\"id\":\"12345\"}")
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet gültige Zahlungsinformationen "
              + "für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationReply V3"
          + "mit korrekte totalAmount und paymentMethods akzeptiert")
  @MethodSource("communicationTestComposer")
  void shouldValidateCommunicationWithCorrectPaymentInfo(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationReplyMessage.forV3()
            .communicationType(CommunicationType.PAYMENT_INFO.getLabel())
            .transactionID(UUID.randomUUID())
            .totalAmount(12550)
            .paymentMethods(
                List.of(
                    new CommunicationReplyMessage.PaymentMethod(
                        "creditcard", "Visa", "https://pay.example.com")))
            .text("paid")
            .build();

    val response = pharma.performs(SendMessages.to(patient).forTask(task).asReply(request, pharma));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Communication DispReq mit Pflichtfeldern"
              + " für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispReq V3 "
          + "mit minimalen Pflichtfeldern akzeptiert wird")
  @MethodSource("communicationDeliveryShipmentComposer")
  void shouldValidateCommunicationDispenseRequest(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationDisReqMessage.forV3()
            .communicationType(CommunicationType.ORDER.getLabel())
            .supplyOptionsType(supplyOptionsType.getLabel())
            .transactionID(UUID.randomUUID())
            .firstname("John")
            .lastname("Doe")
            .phone("+49170123456")
            .text("Please prepare medication")
            .build();

    val response =
        patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(request));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }

  @TestcaseId("ERP_COMMUNICATION_V3_SEND_08")
  @ParameterizedTest(
      name =
          "[{index}] -> Die Stadtapotheke sendet eine V3 Communication DispReq mit vollständigen"
              + " Kontaktdaten für {0} und SupplyOption {1} an den Versicherten Leonie Hütter!")
  @DisplayName(
      "Es wird geprüft, dass der Fachdienst CommunicationDispReq V3 "
          + "mit vollständigen Kontaktdaten akzeptiert wird")
  @MethodSource("communicationDeliveryShipmentComposer")
  void shouldValidateCommunicationDispenseRequestWithContactData(
      PrescriptionAssignmentKind assignmentKind, SupplyOptionsType supplyOptionsType) {

    val task = doc.prescribeFor(patient, assignmentKind);

    val request =
        CommunicationDisReqMessage.forV3()
            .communicationType(CommunicationType.ORDER.getLabel())
            .supplyOptionsType(supplyOptionsType.getLabel())
            .transactionID(UUID.randomUUID())
            .firstname("Anna")
            .lastname("Schmidt")
            .address("Hauptstrasse 12")
            .postcode("40210")
            .city("Düsseldorf")
            .country("DE")
            .phone("+4915123456789")
            .email("anna.schmidt@test.de")
            .text("Delivery preferred in the afternoon")
            .build();

    val response =
        patient.performs(SendMessages.to(pharma).forTask(task).asDispenseRequest(request));

    patient.attemptsTo(
        Verify.that(response).withExpectedType().hasResponseWith(returnCode(201)).isCorrect());
  }
}
