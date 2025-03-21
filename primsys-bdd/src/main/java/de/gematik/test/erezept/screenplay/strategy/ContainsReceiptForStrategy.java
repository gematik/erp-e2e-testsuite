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
 */

package de.gematik.test.erezept.screenplay.strategy;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;

public class ContainsReceiptForStrategy implements Predicate<List<DispenseReceipt>> {

  private final KVNR kvnr;
  private final AmountStrategy<ErxReceipt> strategy;

  public ContainsReceiptForStrategy(AmountAdverb adverb, long amount, KVNR kvnr) {
    this.kvnr = kvnr;
    this.strategy = new AmountStrategy<>(adverb, amount);
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param erxReceipts the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(final List<DispenseReceipt> erxReceipts) {
    val forKvnr =
        erxReceipts.stream()
            .filter(dispensed -> dispensed.getReceiverKvnr().equals(kvnr))
            .map(DispenseReceipt::getReceipt)
            .toList();
    return strategy.test(forKvnr);
  }
}
