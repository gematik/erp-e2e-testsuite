/*
 *  Copyright 2023 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.communication;

import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.containsCountOfCommunication;
import static de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier.onlySenderWith;
import static de.gematik.test.core.expectations.verifier.CommunicationVerifier.emptyReceivedElement;
import static de.gematik.test.core.expectations.verifier.CommunicationVerifier.presentReceivedElement;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeContainsInDetailText;
import static java.text.MessageFormat.format;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.verifier.CommunicationBundleVerifier;
import de.gematik.test.core.expectations.verifier.CommunicationVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.DispensePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.GetMessage;
import de.gematik.test.erezept.actions.communication.GetMessages;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.rest.param.SortOrder;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.client.usecases.search.CommunicationSearch;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
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
@DisplayName("Communication Get Tests")
public class GetMessagesIT extends ErpTest {

  @Actor(name = "Hanna Bäcker")
  private PatientActor hanna;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Dr. Schraßer")
  private DoctorActor doc;

  @Actor(name = "Am Flughafen")
  private PharmacyActor airportApo;

  @Actor(name = "Am Waldesrand")
  private PharmacyActor woodlandPharma;

  private static Stream<Arguments> getCommunicationTestComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, PrescriptionAssignmentKind.class)
        .multiply(1, SupplyOptionsType.class)
        .multiply(2, List.of(VersicherungsArtDeBasis.GKV, VersicherungsArtDeBasis.PKV))
        .create();
  }

  @TestcaseId("ERP_COMMUNICATION_GET_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein Apotheke versucht mittels Id eine Nachricht von einem {2}-Versicherten als {0} und SupplyOption {1} abzurufen, die nicht für sie ist!")
  @DisplayName("Es muss geprüft werden, dass nur der Adressierte seine Nachricht abrufen kann")
  @MethodSource("getCommunicationTestComposer")
  void shouldNotGetForeignCommunicationsAsPharmacy(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    hanna.changePatientInsuranceType(VersicherungsArtDeBasis.PKV);
    val task = prescribe(assignmentKind, hanna);
    val dispRequest =
        hanna.performs(
            SendMessages.to(woodlandPharma)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        supplyOptionsType,
                        "Nachricht zum testen des ErpFD bezüglich Communication: Ist das Medikament No.1 heute noch verfügbar, liebe Apo woodlandPharma?")));
    val airportRequest =
        airportApo.performs(
            GetMessage.byId(
                new CommunicationGetByIdCommand(dispRequest.getExpectedResponse().getIdPart())));
    airportApo.attemptsTo(
        Verify.that(airportRequest)
            .withOperationOutcome()
            .hasResponseWith(returnCode(404))
            .and(
                operationOutcomeContainsInDetailText(
                    "no Communication found for id", ErpAfos.A_19520_01))
            .isCorrect());
    val woodlandCommunications =
        woodlandPharma.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getAllCommunications(SortOrder.DESCENDING)));
    woodlandPharma.attemptsTo(
        Verify.that(woodlandCommunications)
            .withExpectedType()
            .and(
                CommunicationBundleVerifier.containsCommunicationWithId(
                    dispRequest.getExpectedResponse().getIdPart(), ErpAfos.A_19520_01))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_02")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein Apotheke versucht ihre neuen Nachrichten für einen {2}-Versicherten als {0} und Belieferungsoption {1} abzurufen!")
  @DisplayName("Es muss geprüft werden, dass eine Apotheke nur neue Nachrichten abrufen kann")
  @MethodSource("getCommunicationTestComposer")
  void shouldGetCorrectCountOfNewCommunicationsAsPharmacy(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    hanna.changePatientInsuranceType(insuranceType);
    val getNewCommunicationFirst =
        woodlandPharma.performs(
            GetMessages.fromServerWith(CommunicationSearch.getNewCommunications()));
    val countOfNewMessages =
        getNewCommunicationFirst.getExpectedResponse().getCommunications().size();
    woodlandPharma.attemptsTo(
        Verify.that(getNewCommunicationFirst)
            .withExpectedType()
            .and(
                CommunicationBundleVerifier.containsCountOfCommunication(
                    countOfNewMessages, ErpAfos.A_19521))
            .isCorrect());
    val task = prescribe(assignmentKind, hanna);
    val counterToSendMessages = 5;
    val expectation = countOfNewMessages + counterToSendMessages;
    sendMultipleDispenseRequestsAndCount(
        hanna, woodlandPharma, task, supplyOptionsType, counterToSendMessages);

    val getNewCommunicationSecond =
        woodlandPharma.performs(
            GetMessages.fromServerWith(CommunicationSearch.getNewCommunications()));
    woodlandPharma.attemptsTo(
        Verify.that(getNewCommunicationSecond)
            .withExpectedType()
            .and(
                CommunicationBundleVerifier.containsCountOfCommunication(
                    expectation, ErpAfos.A_19521))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_03")
  @ParameterizedTest(
      name =
          "[{index}] ->  Ein Patient versucht als {2}-Versicherter mittels Id eine Nachricht als {0} und SupplyOption {1} abzurufen, die nicht für ihn ist!")
  @DisplayName(
      "Es muss geprüft werden, dass eine Patient nur seine eigenen Nachrichten abrufen kann")
  @MethodSource("getCommunicationTestComposer")
  void shouldNotGetForeignCommunicationsAsPatient(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    sina.changePatientInsuranceType(insuranceType);
    val task = prescribe(assignmentKind, sina);
    val replyMessage =
        new CommunicationReplyMessage(
            supplyOptionsType, "don´t worry, your medicine is ready for take of ;-)");
    val reqMessageResponse =
        woodlandPharma.performs(SendMessages.to(hanna).forTask(task).asReply(replyMessage));
    val getReqMessageResponse =
        hanna.performs(
            GetMessage.byId(
                new CommunicationGetByIdCommand(
                    reqMessageResponse.getExpectedResponse().getIdPart())));
    hanna.attemptsTo(
        Verify.that(getReqMessageResponse)
            .withExpectedType()
            .and(
                CommunicationVerifier.matchId(
                    reqMessageResponse.getExpectedResponse().getIdPart(), ErpAfos.A_19520_01))
            .isCorrect());
    val getReqMessageResponseAsSina =
        sina.performs(
            GetMessage.byId(
                new CommunicationGetByIdCommand(
                    reqMessageResponse.getExpectedResponse().getIdPart())));
    sina.attemptsTo(
        Verify.that(getReqMessageResponseAsSina)
            .withOperationOutcome()
            .hasResponseWith(returnCodeIsBetween(400, 410))
            .and(
                operationOutcomeContainsInDetailText(
                    "no Communication found for id", ErpAfos.A_19520_01))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_04")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient versucht seine neuen Nachrichten für {0} und Belieferungsoption {1} abzurufen!")
  @DisplayName("Es muss geprüft werden, dass eine Patient nur neue Nachrichten abrufen kann")
  @MethodSource("getCommunicationTestComposer")
  void shouldGetCorrectCountOfNewCommunicationsAsPatient(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    sina.changePatientInsuranceType(insuranceType);
    val task = prescribe(assignmentKind, sina);
    val communicationCount =
        sina.performs(GetMessages.fromServerWith(CommunicationSearch.getNewCommunications()))
            .getExpectedResponse()
            .getCommunications()
            .size();
    int noOfSendMessages = 5;
    val expactation = communicationCount + noOfSendMessages;
    sendMultipleReplyAndCount(airportApo, sina, task, supplyOptionsType, noOfSendMessages);
    val updatedMessages =
        sina.performs(GetMessages.fromServerWith(CommunicationSearch.getNewCommunications()));

    sina.attemptsTo(
        Verify.that(updatedMessages)
            .withExpectedType()
            .hasResponseWith(returnCode(200))
            .and(containsCountOfCommunication(expactation, ErpAfos.A_19520_01))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_05")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient und eine Apotheke versuchen Nachrichten als {0} und Belieferungsoption {1} über Recipient sortiert abzurufen!")
  @DisplayName(
      "Es muss geprüft werden, dass eine Patient bzw. eine Apotheke Nachrichten von einem bestimmten Empfänger abrufen können")
  @MethodSource("getCommunicationTestComposer")
  void shouldGetCorrectRecipientCommunications(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    sina.changePatientInsuranceType(insuranceType);
    val task = prescribe(assignmentKind, sina);
    woodlandPharma.attemptsTo(
        Verify.that(
                woodlandPharma.performs(
                    SendMessages.to(sina)
                        .forTask(task)
                        .asReply(
                            new CommunicationReplyMessage(
                                supplyOptionsType, "how much is the fish?"))))
            .isFromExpectedType());
    sina.performs(
        SendMessages.to(woodlandPharma)
            .forTask(task)
            .asDispenseRequest(
                new CommunicationDisReqMessage(supplyOptionsType, "how much is the fish, now?")));
    val messagesFromSinaForSina =
        sina.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getRecipientCommunications(sina.getKvnr().getValue())));
    sina.attemptsTo(
        Verify.that(messagesFromSinaForSina)
            .withExpectedType()
            .and(
                CommunicationBundleVerifier.containsOnlyRecipientWith(
                    sina.getKvnr().getValue(), ErpAfos.A_19522_01))
            .isCorrect());
    val SinaMessagesAtWoodland =
        woodlandPharma.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getRecipientCommunications(sina.getKvnr().getValue())));
    woodlandPharma.attemptsTo(
        Verify.that(SinaMessagesAtWoodland)
            .withExpectedType()
            .and(
                CommunicationBundleVerifier.containsOnlyRecipientWith(
                    sina.getKvnr().getValue(), ErpAfos.A_19522_01))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_06")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient und eine Apotheke versuchten Nachrichten für {0} und Belieferungsoption {1} über Sender sortiert abzurufen!")
  @DisplayName(
      "Es muss geprüft werden, dass eine Patient, eine Apotheke Nachrichten von einem bestimmten Sender abrufen können")
  @MethodSource("getCommunicationTestComposer")
  void shouldGetCorrectSenderCommunications(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    sina.changePatientInsuranceType(insuranceType);

    val task = prescribe(assignmentKind, sina);
    sina.attemptsTo(
        Verify.that(
                sina.performs(
                    SendMessages.to(woodlandPharma)
                        .forTask(task)
                        .asDispenseRequest(
                            new CommunicationDisReqMessage(
                                supplyOptionsType, "nope, we´ll get SMOK!"))))
            .isFromExpectedType());
    woodlandPharma.attemptsTo(
        Verify.that(
                woodlandPharma.performs(
                    SendMessages.to(sina)
                        .forTask(task)
                        .asReply(
                            new CommunicationReplyMessage(
                                supplyOptionsType, "We can deliver a mask, too"))))
            .isFromExpectedType());
    val woodMessagesFromSina =
        woodlandPharma.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getSenderCommunications(sina.getKvnr().getValue())));
    val sinaMessagesFromWoodland =
        sina.performs(
            GetMessages.fromServerWith(
                CommunicationSearch.getSenderCommunications(
                    woodlandPharma.getTelematikId().getValue())));
    woodlandPharma.attemptsTo(
        Verify.that(woodMessagesFromSina)
            .withExpectedType()
            .and(onlySenderWith(sina.getKvnr()))
            .isCorrect());
    sina.attemptsTo(
        Verify.that(sinaMessagesFromWoodland)
            .withExpectedType()
            .has(onlySenderWith(woodlandPharma.getTelematikId()))
            .isCorrect());
    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  @TestcaseId("ERP_COMMUNICATION_GET_07")
  @ParameterizedTest(
      name =
          "[{index}] -> Ein {2}-Patient validiert, ob seine Nachrichten für {0} und Belieferungsoption {1} abgerufen wurde!")
  @DisplayName(
      "Es muss geprüft werden, dass beim Abruf einer Communication die System Zeit gesetzt wird")
  @MethodSource("getCommunicationTestComposer")
  void shouldSetSystemTimeByGetCommunication(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      VersicherungsArtDeBasis insuranceType) {
    sina.changePatientInsuranceType(insuranceType);
    val task = prescribe(assignmentKind, sina);
    val disRequest =
        sina.performs(
            SendMessages.to(airportApo)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        supplyOptionsType,
                        "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??")));
    val dispReqId = disRequest.getExpectedResponse().getIdPart();
    val disReq2 = sina.performs(GetMessage.byId(new CommunicationGetByIdCommand(dispReqId)));
    sina.attemptsTo(
        Verify.that(disReq2).withExpectedType().has(emptyReceivedElement()).isCorrect());
    airportApo.performs(GetMessage.byId(new CommunicationGetByIdCommand(dispReqId)));

    val disReq3 = sina.performs(GetMessage.byId(new CommunicationGetByIdCommand(dispReqId)));
    sina.attemptsTo(
        Verify.that(disReq3).withExpectedType().has(presentReceivedElement()).isCorrect());

    // cleanup
    airportApo.performs(
        DispensePrescription.acceptedWith(
            airportApo.performs(AcceptPrescription.forTheTask(task))));
  }

  private ErxTask prescribe(PrescriptionAssignmentKind assignmentKind, PatientActor actor) {
    return doc.performs(
            IssuePrescription.forPatient(actor)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle())
        .getExpectedResponse();
  }

  private void sendMultipleDispenseRequestsAndCount(
      ErpActor sender,
      ErpActor receiver,
      ErxTask task,
      SupplyOptionsType supplyOptionsType,
      int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      sender.performs(
          SendMessages.to(receiver)
              .forTask(task)
              .asDispenseRequest(
                  new CommunicationDisReqMessage(
                      supplyOptionsType,
                      format(
                          "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Ist das Medikament No.1 heute noch verfügbar, liebe Apo woodlandPharma?",
                          i))));
    }
  }

  private void sendMultipleReplyAndCount(
      ErpActor sender,
      ErpActor receiver,
      ErxTask task,
      SupplyOptionsType supplyOptionsType,
      int numberOfMessages) {
    for (int i = 0; i < numberOfMessages; i++) {
      sender.performs(
          SendMessages.to(receiver)
              .forTask(task)
              .asReply(
                  new CommunicationReplyMessage(
                      supplyOptionsType,
                      format(
                          "Nachricht Nr. {0} zum testen des ErpFD bezüglich Communication: Hey patient, how are you? does the medicine takes an effect??",
                          i))));
    }
  }
}