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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.CloseTaskCommand;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseBuilder;
import de.gematik.test.erezept.fhir.builder.erp.GemErpMedicationFaker;
import de.gematik.test.erezept.fhir.builder.erp.GemOperationInputParameterBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

/**
 * Re-Dispense means in this case: take a dispensed prescription (in status closed) and try again to
 * re-dispense with the previous data (taskId and secret).
 *
 * <p>In such a scenario a pharmacy might try to re-use a prescription task to dispense multiple
 * medications which shall not be allowed
 */
@Slf4j
public class ResponseOfReDispenseMedication extends FhirResponseQuestion<ErxReceipt> {

  private final DequeStrategy deque;

  private ResponseOfReDispenseMedication(DequeStrategy deque) {
    super("Task/$close");
    this.deque = deque;
  }

  public static ResponseOfReDispenseMedication fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfReDispenseMedication fromStack(DequeStrategy deque) {
    return new ResponseOfReDispenseMedication(deque);
  }

  @Override
  public ErpResponse<ErxReceipt> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val receipt = deque.chooseFrom(prescriptionManager.getReceiptsList());

    val cmd = dispensePrescribedMedication(receipt, smcb.getTelematikID());
    val resp = erpClientAbility.request(cmd);
    return resp;
  }

  private CloseTaskCommand dispensePrescribedMedication(
      DispenseReceipt receipt, String telematikId) {
    val taskId = receipt.getTaskId();
    val secret = receipt.getSecret();
    val prescriptionId = receipt.getPrescriptionId();
    val kvnr = receipt.getReceiverKvnr();

    if (ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_3_0) <= 0) {
      val medication =
          KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

      val medicationDispense =
          ErxMedicationDispenseBuilder.forKvnr(kvnr)
              .prescriptionId(prescriptionId)
              .performerId(telematikId)
              .medication(medication)
              .build();
      return new CloseTaskCommand(taskId, secret, medicationDispense);

    } else {
      val gemMedication = GemErpMedicationFaker.builder().fake();
      val medicationDispense =
          ErxMedicationDispenseBuilder.forKvnr(kvnr)
              .prescriptionId(prescriptionId)
              .performerId(telematikId)
              .medication(gemMedication)
              .build();
      val closeParams =
          GemOperationInputParameterBuilder.forClosingPharmaceuticals()
              .with(medicationDispense, gemMedication)
              .build();

      return new CloseTaskCommand(taskId, secret, closeParams);
    }
  }
}
