/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemGetByIdCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

@Slf4j
public class ResponseOfGetChargeItem extends FhirResponseQuestion<ErxChargeItem> {

  private final DequeStrategyEnum deque;

  private ResponseOfGetChargeItem(DequeStrategyEnum deque) {
    this.deque = deque;
  }

  @Override
  public Class<ErxChargeItem> expectedResponseBody() {
    return ErxChargeItem.class;
  }

  @Override
  public String getOperationName() {
    return "ChargeItem";
  }

  @Override
  public ErpResponse answeredBy(Actor actor) {
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val lastDispensedPrescriptionId = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    val cmd = new ChargeItemGetByIdCommand(lastDispensedPrescriptionId);

    log.info(
        format(
            "Actor {0} is asking for the response of {1}",
            actor.getName(), cmd.getRequestLocator()));
    return erpClientAbility.request(cmd);
  }

  public static ResponseOfGetChargeItem forPrescription(DequeStrategyEnum deque) {
    return new ResponseOfGetChargeItem(deque);
  }

  public static ResponseOfGetChargeItem forLastPrescription() {
    return forPrescription(DequeStrategyEnum.LIFO);
  }
}
