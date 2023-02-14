/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.screenplay.task;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.exceptions.UnexpectedResponseResourceError;
import de.gematik.test.erezept.client.usecases.CommunicationPostCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.resources.erp.ErxCommunication;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManagePatientPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class RedeemPrescription implements Task {

  private final ErxCommunicationBuilder communicationBuilder;
  private final boolean isAssignToPharmacist;
  private final DequeStrategy strategy;

  public static RedeemPrescription reserve(Actor pharmacist, DequeStrategy strategy) {
    val useSMCBAbility = SafeAbility.getAbility(pharmacist, UseSMCB.class);

    val communicationBuilder =
        ErxCommunicationBuilder.builder()
            .recipient(useSMCBAbility.getTelematikID())
            .supplyOptions(new SupplyOptionsType(true, true, false))
            // TODO replace this
            .insurance(IKNR.from("104212059"))
            .flowType(PrescriptionFlowType.FLOW_TYPE_160);
    return new RedeemPrescription(communicationBuilder, false, strategy);
  }

  public static RedeemPrescription assign(Actor pharmacist, DequeStrategy strategy) {
    val useSMCBAbility = SafeAbility.getAbility(pharmacist, UseSMCB.class);

    val communicationBuilder =
        ErxCommunicationBuilder.builder().recipient(useSMCBAbility.getTelematikID());

    return new RedeemPrescription(communicationBuilder, true, strategy);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val managePatientPrescriptions =
        SafeAbility.getAbility(actor, ManagePatientPrescriptions.class);
    val prescription =
        strategy.chooseFrom(managePatientPrescriptions.getFullDetailedPrescriptions());

    final ErxCommunication communication;
    if (!isAssignToPharmacist) {
      // TODO replace this
      val message = "Hallo, ich wollte gern fragen, ob das Medikament bei Ihnen vorraetig ist.";

      communicationBuilder.medication(prescription.getKbvBundle().getMedication());
      communicationBuilder.basedOnTaskId(prescription.getTask().getUnqualifiedId());
      communication = communicationBuilder.buildInfoReq(message);
    } else {
      // TODO replace values
      val message =
          "{ \"version\": \"1\", \"supplyOptionsType\": \"delivery\", \"name\": \"Dr. Maximilian von Muster\", "
              + "\"address\": [ \"wohnhaft bei Emilia Fischer\", \"Bundesallee 312\", \"123. OG\", \"12345 Berlin\" ], "
              + "\"hint\": \"Bitte im Morsecode klingeln: -.-.\", \"phone\": \"004916094858168\" }";
      communicationBuilder.basedOnTask(
          prescription.getTask().getUnqualifiedId(), prescription.getTask().getAccessCode());
      communication = communicationBuilder.buildDispReq(message);
    }

    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val communicationCmd = new CommunicationPostCommand(communication);

    try {
      erpClientAbility.request(communicationCmd).getResource(ErxCommunication.class);
    } catch (UnexpectedResponseResourceError urre) {
      log.warn(
          format(
              "Sending message {0} to pharmacy {1} failed",
              communication.getBasedOnReferenceId(), communication.getRecipientId()));
      throw urre;
    }
  }
}
