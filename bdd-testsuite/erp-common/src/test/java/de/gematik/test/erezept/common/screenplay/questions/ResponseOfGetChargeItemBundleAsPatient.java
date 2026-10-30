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

package de.gematik.test.erezept.common.screenplay.questions;

import static java.text.MessageFormat.*;

import de.gematik.bbriccs.rest.fd.FhirBResponse;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.fhir.r4.erp.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.screenplay.*;

@Slf4j
public class ResponseOfGetChargeItemBundleAsPatient
    extends FhirResponseQuestion<ErxChargeItemBundle> {

  private final DequeStrategy deque;

  private ResponseOfGetChargeItemBundleAsPatient(DequeStrategy deque) {
    this.deque = deque;
  }

  public static ResponseOfGetChargeItemBundleAsPatient forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static ResponseOfGetChargeItemBundleAsPatient forPrescription(DequeStrategy deque) {
    return new ResponseOfGetChargeItemBundleAsPatient(deque);
  }

  public static ResponseOfGetChargeItemBundleAsPatient forLastPrescription() {
    return forPrescription(DequeStrategy.LIFO);
  }

  @Override
  public FhirBResponse<ErxChargeItemBundle> answeredBy(Actor actor) {
    val erpClientAbility = SafeAbility.getAbility(actor, UseTheErpClient.class);

    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    val dispensationInformation = deque.chooseFrom(receivedDrugs.getDispensedDrugsList());
    val cmd = new ChargeItemGetByIdCommand(dispensationInformation.prescriptionId());

    log.info(
        format(
            "Actor {0} is asking for the response of {1}",
            actor.getName(), cmd.getRequestLocator()));
    return erpClientAbility.request(cmd);
  }
}
