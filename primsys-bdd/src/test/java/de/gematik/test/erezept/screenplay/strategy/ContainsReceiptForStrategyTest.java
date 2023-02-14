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

package de.gematik.test.erezept.screenplay.strategy;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ContainsReceiptForStrategyTest {

  @Test
  void shouldHaveContainedReceipt() {
    val kvid = "X123456789";
    val strategy = new ContainsReceiptForStrategy(AmountAdverb.EXACTLY, 1, kvid);

    val mockReceipt = mock(DispenseReceipt.class);
    when(mockReceipt.getReceiverKvid()).thenReturn(kvid);
    assertTrue(strategy.test(List.of(mockReceipt)));
  }

  @Test
  void shouldNotHaveContainedReceipt() {
    val kvid = "X123456789";
    val strategy = new ContainsReceiptForStrategy(AmountAdverb.AT_LEAST, 1, kvid);

    val mockReceipt = mock(DispenseReceipt.class);
    when(mockReceipt.getReceiverKvid()).thenReturn("M123456789");
    assertFalse(strategy.test(List.of(mockReceipt)));
  }
}
