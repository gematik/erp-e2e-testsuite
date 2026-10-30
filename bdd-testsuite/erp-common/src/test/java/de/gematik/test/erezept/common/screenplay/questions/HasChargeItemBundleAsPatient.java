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

import de.gematik.test.erezept.common.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.common.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public class HasChargeItemBundleAsPatient implements Question<Boolean> {

  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(final Actor actor) {
    val response = actor.asksFor(ResponseOfGetChargeItemBundleAsPatient.forPrescription(deque));
    if (response.getResourceOptional().isEmpty()) {
      return false; // unexpected response
    }

    val chargeItemBundle = response.getExpectedResource();
    val chargeItem = chargeItemBundle.getChargeItem();

    val egk = SafeAbility.getAbility(actor, ProvideEGK.class);
    // store the fetched charge item on local stack
    val chargeItemAbility = SafeAbility.getAbility(actor, ManageChargeItems.class);
    chargeItemAbility.update(chargeItem);

    if (chargeItem.getReceiptReference().isEmpty() && chargeItemBundle.getReceipt().isEmpty()) {
      return false;
    }

    return chargeItem.getSubjectKvnr().equals(egk.getKvnr());
  }

  public static HasChargeItemBundleAsPatient forLastDispensedDrug() {
    return forPrescription(DequeStrategy.LIFO);
  }

  public static HasChargeItemBundleAsPatient forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static HasChargeItemBundleAsPatient forPrescription(DequeStrategy deque) {
    return new HasChargeItemBundleAsPatient(deque);
  }
}
