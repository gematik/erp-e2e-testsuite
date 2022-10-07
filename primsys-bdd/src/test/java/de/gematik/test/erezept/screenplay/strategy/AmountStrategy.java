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

package de.gematik.test.erezept.screenplay.strategy;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.lei.exceptions.NegativeAmountException;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;

public class AmountStrategy<T> implements Predicate<List<T>> {

  private final AmountAdverb adverb;
  private final long amount;

  public AmountStrategy(AmountAdverb adverb, long amount) {
    if (amount < 0) throw new NegativeAmountException(this.getClass(), amount);
    this.adverb = adverb;
    this.amount = amount;
  }

  /**
   * Evaluates this predicate on the given argument.
   *
   * @param objects the input argument
   * @return {@code true} if the input argument matches the predicate, otherwise {@code false}
   */
  @Override
  public boolean test(final List<T> objects) {
    boolean ret;
    val size = objects.size();
    switch (adverb) {
      case AT_LEAST:
        ret = size >= amount;
        break;
      case AT_MOST:
        ret = size <= amount;
        break;
      case EXACTLY:
        ret = size == amount;
        break;
      default:
        throw new NotImplementedException(
            format(
                "{0} of Type {1} is not implemented", adverb.getClass().getSimpleName(), adverb));
    }
    return ret;
  }
}
