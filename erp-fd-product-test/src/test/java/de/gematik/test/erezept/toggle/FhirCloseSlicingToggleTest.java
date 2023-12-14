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

package de.gematik.test.erezept.toggle;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class FhirCloseSlicingToggleTest {

  @Test
  void shouldHaveKey() {
    val fcst = new FhirCloseSlicingToggle();
    assertFalse(fcst.getKey().isBlank());
    assertFalse(fcst.getKey().isEmpty());
  }

  @Test
  void shouldHaveDefaultValue() {
    val fcst = new FhirCloseSlicingToggle();
    assertEquals(true, fcst.getDefaultValue());
  }

  @Test
  void shouldHaveConverter() {
    val fcst = new FhirCloseSlicingToggle();
    val converter = fcst.getConverter();
    assertEquals(true, converter.apply("Yes"));
    assertEquals(true, converter.apply("true"));
    assertEquals(true, converter.apply("1"));
    assertEquals(false, converter.apply("0"));
  }
}
