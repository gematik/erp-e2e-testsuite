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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.smartcard.Egk;
import lombok.val;
import org.junit.jupiter.api.Test;

class ProvideEGKTest {

  @Test
  void shouldReturnKvnrFromEgk() {
    val kvnr = "X123456789";
    val mockEgk = mock(Egk.class);
    when(mockEgk.getKvnr()).thenReturn(kvnr);
    val ability = ProvideEGK.sheOwns(mockEgk);
    assertEquals(kvnr, ability.getKvnr());
  }

  @Test
  void shouldHaveToString() {
    val kvnr = "X123456789";
    val iccsn = "00000";
    val mockEgk = mock(Egk.class);
    when(mockEgk.getKvnr()).thenReturn(kvnr);
    when(mockEgk.getIccsn()).thenReturn(iccsn);
    val ability = ProvideEGK.sheOwns(mockEgk);
    assertTrue(ability.toString().contains(kvnr));
    assertTrue(ability.toString().contains(iccsn));
  }
}
