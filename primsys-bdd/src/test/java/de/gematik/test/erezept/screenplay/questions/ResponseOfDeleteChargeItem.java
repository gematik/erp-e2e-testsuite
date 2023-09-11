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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.ChargeItemDeleteCommand;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.hl7.fhir.r4.model.Resource;

public class ResponseOfDeleteChargeItem extends FhirResponseQuestion<Resource> {

  private final DequeStrategy deque;

  private ResponseOfDeleteChargeItem(DequeStrategy deque) {
    super("DELETE /ChargeItem");
    this.deque = deque;
  }

  @Override
  public ErpResponse<Resource> answeredBy(Actor actor) {
    val dispensedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val prescriptionId = deque.chooseFrom(dispensedDrugs.getDispensedDrugsList());
    val cmd = new ChargeItemDeleteCommand(prescriptionId);
    return erpClient.request(cmd);
  }

  public static ResponseOfDeleteChargeItem fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static ResponseOfDeleteChargeItem fromStack(DequeStrategy deque) {
    return new ResponseOfDeleteChargeItem(deque);
  }
}
