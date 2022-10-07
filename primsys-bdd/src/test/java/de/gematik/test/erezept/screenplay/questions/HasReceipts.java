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
import de.gematik.test.erezept.screenplay.strategy.AmountAdverb;
import de.gematik.test.erezept.screenplay.strategy.AmountStrategy;
import de.gematik.test.erezept.screenplay.strategy.ContainsReceiptForStrategy;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HasReceipts implements Question<Boolean> {

  private final Predicate<List<DispenseReceipt>> strategy;

  private HasReceipts(Predicate<List<DispenseReceipt>> strategy) {
    this.strategy = strategy;
  }

  @Override
  public Boolean answeredBy(final Actor actor) {
    val prescriptionManager = SafeAbility.getAbility(actor, ManagePharmacyPrescriptions.class);
    return strategy.test(prescriptionManager.getReceiptsList());
  }

  public static HasReceipts atLeastOne() {
    return atLeast(1);
  }

  public static HasReceipts atLeast(long amount) {
    return of(AmountAdverb.AT_LEAST, amount);
  }

  public static HasReceipts atMostOne() {
    return atMost(1);
  }

  public static HasReceipts atMost(long amount) {
    return of(AmountAdverb.AT_MOST, amount);
  }

  public static HasReceipts exactly(long amount) {
    return of(AmountAdverb.EXACTLY, amount);
  }

  public static HasReceipts of(String adverb, long amount) {
    return of(AmountAdverb.fromString(adverb), amount);
  }

  public static HasReceipts of(AmountAdverb adverb, long amount) {
    return new HasReceipts(new AmountStrategy<>(adverb, amount));
  }

  public static HasReceipts forPatient(String adverb, long amount, String kvid) {
    return forPatient(AmountAdverb.fromString(adverb), amount, kvid);
  }

  public static HasReceipts forPatient(AmountAdverb adverb, long amount, String kvid) {
    return new HasReceipts(new ContainsReceiptForStrategy(adverb, amount, kvid));
  }
}
