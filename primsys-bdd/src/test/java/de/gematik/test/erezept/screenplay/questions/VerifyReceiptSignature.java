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

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategyEnum;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;
import net.thucydides.core.annotations.Step;

public class VerifyReceiptSignature implements Question<Boolean> {

  private final DequeStrategyEnum dequeStrategy;

  public VerifyReceiptSignature(DequeStrategyEnum dequeStrategy) {
    this.dequeStrategy = dequeStrategy;
  }

  @Override
  @Step("{0} prüft Signatur der #dequeStrategy Quittung mit dem Konnektor")
  public Boolean answeredBy(Actor actor) {
    val pharmacyPrescriptions = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

    val receipt = dequeStrategy.chooseFrom(pharmacyPrescriptions.getReceiptsList());
    val signature = receipt.getReceipt().getSignature().getDataElement().getValue();
    return konnektor.verifyDocument(signature);
  }

  public static VerifyReceiptSignature fromStack(String dequeStrategy) {
    return fromStack(DequeStrategyEnum.fromString(dequeStrategy));
  }

  public static VerifyReceiptSignature fromStack(DequeStrategyEnum dequeStrategy) {
    Object[] params = {dequeStrategy};
    return new Instrumented.InstrumentedBuilder<>(VerifyReceiptSignature.class, params)
        .newInstance();
  }
}