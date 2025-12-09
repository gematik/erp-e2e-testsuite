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

package de.gematik.test.erezept.fdv.questions;

import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RedeemCodeIsAvailable implements Question<Boolean> {

  private final DequeStrategy order;

  /**
   * Checks if the medication dispense is available for the given order, by getting the
   * PrescriptionId from DMC-Stack and asking the FD for the Task for Now the Method works for the
   * Patient!!!
   *
   * @param order
   * @return boolean
   */
  public static RedeemCodeIsAvailable forThePrescription(String order) {
    return forThePrescription(DequeStrategy.fromString(order));
  }

  public static RedeemCodeIsAvailable forThePrescription(DequeStrategy deque) {
    return new RedeemCodeIsAvailable(deque);
  }

  @Override
  @Step("{0} checks if the medication dispense is available")
  public Boolean answeredBy(Actor actor) {
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
    val stack = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = order.chooseFrom(stack.getDmcList());
    val taskId = dmc.getTaskId();

    val cmd = new MedicationDispenseSearchByIdCommand(PrescriptionId.from(taskId));
    val response = erpClient.request(cmd);
    val mD = response.getExpectedResource();
    log.info(
        "Actor {} asked for MedicationDispense and received {} elements",
        actor.getName(),
        mD.getMedicationDispenses().size());

    return mD.getMedicationDispenses().stream()
        .filter(ErxMedicationDispenseBase::isDiGA)
        .map(ErxMedicationDispense::getRedeemCode)
        .map(Optional::isPresent)
        .findFirst()
        .orElse(false);
  }
}
