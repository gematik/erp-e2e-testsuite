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
 */

package de.gematik.test.erezept.screenplay.task;

import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxComPrescriptionBuilder;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.json.CommunicationDisReqMessage;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManagePatientPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RedeemPrescription implements Task {

  private final ErxComPrescriptionBuilder<?> communicationBuilder;
  private final boolean isAssignToPharmacist;
  private final DequeStrategy deque;

  public static RedeemPrescription reserve(Actor pharmacist, DequeStrategy deque) {
    val useSMCBAbility = SafeAbility.getAbility(pharmacist, UseSMCB.class);

    // TODO replace this
    val message = "Hallo, ich wollte gern fragen, ob das Medikament bei Ihnen vorraetig ist.";
    val communicationBuilder =
        ErxCommunicationBuilder.forInfoRequest(message)
            .receiver(useSMCBAbility.getTelematikID())
            .supplyOptions(SupplyOptionsType.createDefault())
            // TODO replace this
            .insurance(IKNR.asArgeIknr("104212059"))
            .flowType(PrescriptionFlowType.FLOW_TYPE_160);
    return new RedeemPrescription(communicationBuilder, false, deque);
  }

  public static RedeemPrescription assign(Actor pharmacist, DequeStrategy deque) {
    val useSMCBAbility = SafeAbility.getAbility(pharmacist, UseSMCB.class);

    // TODO replace this
    val message =
        new CommunicationDisReqMessage(
            SupplyOptionsType.DELIVERY,
            "Dr. Maximilian von Muster",
            List.of("wohnhaft bei Emilia Fischer", "Bundesallee 312", "123. OG", "12345 Berlin"),
            "Bitte im Morsecode klingeln: -.-.",
            "004916094858168");

    val communicationBuilder =
        ErxCommunicationBuilder.forDispenseRequest(message)
            .receiver(useSMCBAbility.getTelematikID());

    return new RedeemPrescription(communicationBuilder, true, deque);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val managePatientPrescriptions =
        SafeAbility.getAbility(actor, ManagePatientPrescriptions.class);
    val prescription = deque.chooseFrom(managePatientPrescriptions.getFullDetailedPrescriptions());

    final ErxCommunication communication;
    if (!isAssignToPharmacist) {
      prescription
          .getKbvBundle()
          .ifPresent(kbvErpBundle -> communicationBuilder.medication(kbvErpBundle.getMedication()));
      communicationBuilder.basedOn(prescription.getTask().getTaskId());
      communication = communicationBuilder.build();
    } else {
      communicationBuilder.flowType(prescription.getTask().getFlowType());
      communicationBuilder.basedOn(
          prescription.getTask().getTaskId(), prescription.getTask().getAccessCode());
      // TODO: check me later, builder not working because of IDEA issues?
      //      communicationBuilder
      //          .basedOn(prescription.getTask().getTaskId(),
      // prescription.getTask().getAccessCode())
      //          .flowType(prescription.getTask().getFlowType());
      communication = communicationBuilder.build();
    }

    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communicationCmd = new CommunicationPostCommand(communication);

    try {
      erpClientAbility.request(communicationCmd).getExpectedResource();
    } catch (UnexpectedResponseResourceError urre) {
      log.warn(
          "Sending message {} to pharmacy {} failed",
          communication.getBasedOnReferenceId(),
          communication.getRecipientId());
      throw urre;
    }
  }
}
