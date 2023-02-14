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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.smartcard.SmcB;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseSMCBTest {

  @Test
  void shouldGetTelematikId() {
    val mockSmcb = mock(SmcB.class);
    when(mockSmcb.getTelematikId()).thenReturn("123");

    val ability = UseSMCB.itHasAccessTo(mockSmcb);
    assertEquals("123", ability.getTelematikID());
  }

  @Test
  void shouldToStringCorrectly() {
    val mockSmcb = mock(SmcB.class);
    when(mockSmcb.getTelematikId()).thenReturn("123");
    when(mockSmcb.getIccsn()).thenReturn("000");

    val ability = UseSMCB.itHasAccessTo(mockSmcb);
    assertTrue(ability.toString().contains("Telematik-ID 123"));
    assertTrue(ability.toString().contains("(ICCSN 000)"));
  }
}
