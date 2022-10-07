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

import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.AmountAdverb;
import de.gematik.test.erezept.screenplay.strategy.AmountStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

public class HasDataMatrixCodes implements Question<Boolean> {

  private final Predicate<List<DmcPrescription>> strategy;

  private HasDataMatrixCodes(Predicate<List<DmcPrescription>> strategy) {
    this.strategy = strategy;
  }

  @Override
  public Boolean answeredBy(final Actor actor) {
    val dmcManager = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    return this.strategy.test(dmcManager.getDmcList());
  }

  public static HasDataMatrixCodes atLeastOne() {
    return atLeast(1);
  }

  public static HasDataMatrixCodes atLeast(long amount) {
    return of(AmountAdverb.AT_LEAST, amount);
  }

  public static HasDataMatrixCodes atMostOne() {
    return atMost(1);
  }

  public static HasDataMatrixCodes atMost(long amount) {
    return of(AmountAdverb.AT_MOST, amount);
  }

  public static HasDataMatrixCodes exactly(long amount) {
    return of(AmountAdverb.EXACTLY, amount);
  }

  public static HasDataMatrixCodes of(String adverb, long amount) {
    return of(AmountAdverb.fromString(adverb), amount);
  }

  public static HasDataMatrixCodes of(AmountAdverb adverb, long amount) {
    return new HasDataMatrixCodes(new AmountStrategy<>(adverb, amount));
  }
}
