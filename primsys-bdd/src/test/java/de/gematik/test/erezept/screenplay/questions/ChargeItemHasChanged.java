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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ChargeItemHasChanged implements Question<Boolean> {

  private final DequeStrategy deque;

  @Override
  public Boolean answeredBy(Actor actor) {
    val chargeItemAbility = SafeAbility.getAbility(actor, ManageChargeItems.class);
    val myChargeItem = deque.chooseFrom(chargeItemAbility.getChargeItems());

    val question = ResponseOfGetChargeItemBundle.forPrescription(deque).asPatient();
    val getResponse = question.answeredBy(actor);
    val fdChargeItemBundle = getResponse.getExpectedResource();
    val fdChargeItem = fdChargeItemBundle.getChargeItem();
    assertEquals(myChargeItem.getPrescriptionId(), fdChargeItem.getPrescriptionId());

    // for now, check only if the accesscode has changed!
    return !(myChargeItem.getAccessCode().orElse(null)
        == fdChargeItem.getAccessCode().orElse(null));
  }

  @Override
  public String getSubject() {
    return "Prüfe, ob das ChargeItem geändert wurde";
  }

  public static ChargeItemHasChanged forPrescription(String order) {
    return forPrescription(DequeStrategy.fromString(order));
  }

  public static ChargeItemHasChanged forPrescription(DequeStrategy deque) {
    return new ChargeItemHasChanged(deque);
  }
}
