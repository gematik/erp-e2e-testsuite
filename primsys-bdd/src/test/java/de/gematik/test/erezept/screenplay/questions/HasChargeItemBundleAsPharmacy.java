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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.common.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class HasChargeItemBundleAsPharmacy implements Question<Boolean> {

  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(final Actor actor) {
    val response =
        actor.asksFor(ResponseOfGetChargeItemBundleAsPharmacy.forPrescription(deque).build());
    if (response.getResourceOptional().isEmpty()) {
      return false; // unexpected response
    }

    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();

    val pharmacyStack = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val smcb = SafeAbility.getAbility(actor, UseSMCB.class);
    val dispensed = deque.chooseFrom(pharmacyStack.getClosedPrescriptions());

    if (!chargeItem.getPrescriptionId().equals(dispensed.getPrescriptionId())) {
      log.warn(
          "Prescription Id {} of charge item do not match to " + "prescription Id {} of the task.",
          chargeItem.getPrescriptionId(),
          dispensed.getPrescriptionId());
      return false;
    }

    if (chargeItemBundle.getReceipt().isPresent()) {
      log.warn("Charge item bundle contains a receipt resource.");
      return false;
    }

    if (chargeItem.getAccessCode().isPresent()) {
      log.warn("Charge Item contains the access code {}.", chargeItem.getAccessCode().get());
      return false;
    }

    return chargeItem.getEntererTelematikId().getValue().equals(smcb.getTelematikID());
  }

  public static HasChargeItemBundleAsPharmacy forLastDispensedDrug() {
    return forPrescription(DequeStrategy.LIFO);
  }

  public static HasChargeItemBundleAsPharmacy forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static HasChargeItemBundleAsPharmacy forPrescription(DequeStrategy deque) {
    return new HasChargeItemBundleAsPharmacy(deque);
  }
}
