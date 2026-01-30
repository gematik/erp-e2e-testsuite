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

import static de.gematik.test.core.expectations.verifier.CommunicationVerifier.emptyReceivedElement;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.verifier.ErpResponseVerifier;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.DeleteMessages;
import de.gematik.test.erezept.actions.communication.GetMessage;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.CommunicationDeleteCommand;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.List;
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
@DisplayName("Communication Delete Tests")
@Tag("DeleteCommunication")
public class DeleteCommunicationIT extends ErpTest {

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Dr. Schraßer")
  private DoctorActor doc;

  @Actor(name = "Am Flughafen")
  private PharmacyActor airportApo;

  private static Stream<Arguments> getCommunicationTestComposer() {
    return ArgumentComposer.composeWith()
        .arguments()
        .multiply(0, PrescriptionAssignmentKind.class)
        .multiply(1, SupplyOptionsType.class)
        .multiply(2, List.of(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV))
        .create();
  }

  @TestcaseId("ERP_COMMUNICATION_DELETE_01")
  @ParameterizedTest(
      name =
          "[{index}] -> Beim Löschen einer bereits abgerufenen Communication muss kein"
              + " Warning-Header gesetzt sein")
  @MethodSource("getCommunicationTestComposer")
  @DisplayName(
      "Beim Löschen einer bereits abgerufenen Communication muss kein Warning-Header gesetzt sein")
  void shouldSetWarningHeaderWhenDeletingReceivedCommunication(
      PrescriptionAssignmentKind assignmentKind,
      SupplyOptionsType supplyOptionsType,
      InsuranceTypeDe insuranceType) {

    sina.changePatientInsuranceType(insuranceType);
    val task = doc.prescribeFor(sina, assignmentKind);

    val postDispRequest =
        sina.performs(
            SendMessages.to(airportApo)
                .forTask(task)
                .asDispenseRequest(
                    new CommunicationDisReqMessage(
                        supplyOptionsType, "Bitte Medikament bereitstellen")));

    val communicationId = postDispRequest.getExpectedResponse().getIdPart();

    sina.attemptsTo(
        Verify.that(postDispRequest).withExpectedType().has(emptyReceivedElement()).isCorrect());

    airportApo.performs(GetMessage.byId(new CommunicationGetByIdCommand(communicationId)));

    val deleteResponse =
        sina.performs(
            DeleteMessages.fromServerWith(new CommunicationDeleteCommand(communicationId)));

    sina.attemptsTo(
        Verify.that(deleteResponse)
            .withExpectedType()
            .hasResponseWith(returnCode(204))
            .hasResponseWith(ErpResponseVerifier.warningHeaderIsUnset())
            .isCorrect());

    // cleanup
    airportApo.performs(
        ClosePrescription.acceptedWith(airportApo.performs(AcceptPrescription.forTheTask(task))));
  }
}
