/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.screenplay.questions;

import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs.ReceivedDispensedDrugInformation;
import de.gematik.test.erezept.screenplay.strategy.AmountAdverb;
import de.gematik.test.erezept.screenplay.strategy.AmountStrategy;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HasDispensedDrugs implements Question<Boolean> {

  private final Predicate<List<ReceivedDispensedDrugInformation>> strategy;

  private HasDispensedDrugs(Predicate<List<ReceivedDispensedDrugInformation>> strategy) {
    this.strategy = strategy;
  }

  @Override
  public Boolean answeredBy(final Actor actor) {
    val receivedDrugs = SafeAbility.getAbility(actor, ReceiveDispensedDrugs.class);
    return strategy.test(receivedDrugs.getDispensedDrugsList());
  }

  public static HasDispensedDrugs atLeastOne() {
    return atLeast(1);
  }

  public static HasDispensedDrugs atLeast(long amount) {
    return of(AmountAdverb.AT_LEAST, amount);
  }

  public static HasDispensedDrugs atMostOne() {
    return atMost(1);
  }

  public static HasDispensedDrugs atMost(long amount) {
    return of(AmountAdverb.AT_MOST, amount);
  }

  public static HasDispensedDrugs exactly(long amount) {
    return of(AmountAdverb.EXACTLY, amount);
  }

  public static HasDispensedDrugs of(String adverb, long amount) {
    return of(AmountAdverb.fromString(adverb), amount);
  }

  public static HasDispensedDrugs of(AmountAdverb adverb, long amount) {
    return new HasDispensedDrugs(new AmountStrategy<>(adverb, amount));
  }
}
