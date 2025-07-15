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

package de.gematik.test.erezept.screenplay.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class AmountStrategyTest {

  @Test
  void shouldPassAmountStrategy() {
    val atLeastStrategy = new AmountStrategy<String>(AmountAdverb.AT_LEAST, 1);
    val exactStrategy = new AmountStrategy<String>(AmountAdverb.EXACTLY, 2);
    val atMostStrategy = new AmountStrategy<String>(AmountAdverb.AT_MOST, 3);

    val list = List.of("one", "two");
    assertTrue(atLeastStrategy.test(list));
    assertTrue(exactStrategy.test(list));
    assertTrue(atMostStrategy.test(list));
  }

  @Test
  void shouldFailAmountStrategy() {
    val atLeastStrategy = new AmountStrategy<String>(AmountAdverb.AT_LEAST, 3);
    val exactStrategy = new AmountStrategy<String>(AmountAdverb.EXACTLY, 3);
    val atMostStrategy = new AmountStrategy<String>(AmountAdverb.AT_MOST, 1);

    val list = List.of("one", "two");
    assertFalse(atLeastStrategy.test(list));
    assertFalse(exactStrategy.test(list));
    assertFalse(atMostStrategy.test(list));
  }
}
