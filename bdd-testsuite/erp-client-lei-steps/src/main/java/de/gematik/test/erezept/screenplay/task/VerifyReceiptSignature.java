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

package de.gematik.test.erezept.screenplay.task;

import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.questions.VerifyDocumentResponse;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.util.*;
import lombok.*;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.*;
import net.serenitybdd.screenplay.*;
import net.serenitybdd.screenplay.ensure.Ensure;

public class VerifyReceiptSignature implements Task {

  private final DequeStrategy dequeStrategy;

  public VerifyReceiptSignature(DequeStrategy dequeStrategy) {
    this.dequeStrategy = dequeStrategy;
  }

  public static VerifyReceiptSignature fromStack(String dequeStrategy) {
    return fromStack(DequeStrategy.fromString(dequeStrategy));
  }

  public static VerifyReceiptSignature fromStack(DequeStrategy dequeStrategy) {
    Object[] params = {dequeStrategy};
    return new Instrumented.InstrumentedBuilder<>(VerifyReceiptSignature.class, params)
        .newInstance();
  }

  @Override
  @Step("{0} prüft Signatur der #dequeStrategy Quittung mit dem Konnektor")
  public <T extends Actor> void performAs(T actor) {
    val pharmacyPrescriptions = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);

    val receipt = dequeStrategy.chooseFrom(pharmacyPrescriptions.getReceiptsList());
    val signature = receipt.getReceipt().getSignature().getDataElement().getValue();

    actor.attemptsTo(
        Ensure.that(
                "the verifyDocument for the receipt",
                VerifyDocumentResponse.forGivenDocument(signature))
            .isTrue());
  }
}
