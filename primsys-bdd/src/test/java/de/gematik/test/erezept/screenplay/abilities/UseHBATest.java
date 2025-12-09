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

package de.gematik.test.erezept.screenplay.abilities;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.smartcards.Hba;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseHBATest {
  @Test
  void shouldGetTelematikId() {
    val mockHba = mock(Hba.class);
    when(mockHba.getTelematikId()).thenReturn("123");

    val ability = UseHBA.itHasAccessTo(mockHba);
    assertEquals("123", ability.getTelematikId());
  }

  @Test
  void shouldToStringCorrectly() {
    val mockHba = mock(Hba.class);
    when(mockHba.getTelematikId()).thenReturn("123");
    when(mockHba.getIccsn()).thenReturn("000");

    val ability = UseHBA.itHasAccessTo(mockHba);
    assertTrue(ability.toString().contains("Telematik-ID 123"));
    assertTrue(ability.toString().contains("(ICCSN 000)"));
  }
}
