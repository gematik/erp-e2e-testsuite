/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.Map;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.Test;

class AmountAdverbTest {

  @Test
  void shouldGetAmountAdverbFromString() {
    val m =
        Map.of(
            "mindestens", AmountAdverb.AT_LEAST,
            "Mindestens", AmountAdverb.AT_LEAST,
            "maximal", AmountAdverb.AT_MOST,
            "MAXIMAL", AmountAdverb.AT_MOST,
            "höchstens", AmountAdverb.AT_MOST,
            "genau", AmountAdverb.EXACTLY,
            "genAU", AmountAdverb.EXACTLY,
            "exakt", AmountAdverb.EXACTLY);
    m.forEach((k, v) -> assertEquals(v, AmountAdverb.fromString(k)));
  }

  @Test
  void shouldThrowOnInvalidValue() {
    val m = List.of("max", "min", "abcd", "");
    m.forEach(v -> assertThrows(NotImplementedException.class, () -> AmountAdverb.fromString(v)));
  }
}
