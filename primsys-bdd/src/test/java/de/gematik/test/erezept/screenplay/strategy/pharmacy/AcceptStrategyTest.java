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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import lombok.val;
import org.junit.jupiter.api.Test;

class AcceptStrategyTest {

  @Test
  void shouldGetFIFO() {
    val stack = ManagePharmacyPrescriptions.itWorksWith();

    val mockDmc1 = mock(DmcPrescription.class);
    val mockDmc2 = mock(DmcPrescription.class);
    stack.appendAssignedPrescription(mockDmc1);
    stack.appendAssignedPrescription(mockDmc2);

    val strategy = AcceptStrategy.fromStack(DequeStrategy.FIFO);
    strategy.initialize(stack);
    val fetched = strategy.getDmcPrescription();
    assertEquals(mockDmc1, fetched);
    strategy.teardown();
    assertEquals(1, stack.getAssignedList().size());
  }

  @Test
  void shouldGetLIFO() {
    val stack = ManagePharmacyPrescriptions.itWorksWith();

    val mockDmc1 = mock(DmcPrescription.class);
    val mockDmc2 = mock(DmcPrescription.class);
    stack.appendAssignedPrescription(mockDmc1);
    stack.appendAssignedPrescription(mockDmc2);

    val strategy = AcceptStrategy.fromStack(DequeStrategy.LIFO);
    strategy.initialize(stack);
    val fetched = strategy.getDmcPrescription();
    assertEquals(mockDmc2, fetched);
    strategy.teardown();
    assertEquals(1, stack.getAssignedList().size());
  }
}
