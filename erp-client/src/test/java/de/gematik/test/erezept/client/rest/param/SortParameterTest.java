/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.client.rest.param;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class SortParameterTest {

  private static final SortParameter sortParameter = new SortParameter("date");

  @Test
  void shouldGetEncode() {
    assertEquals("_sort=-date", sortParameter.encode());
  }

  @Test
  void shouldGetParameter() {
    assertEquals("_sort", sortParameter.parameter());
  }

  @Test
  void shouldGetValue() {
    assertEquals("-date", sortParameter.value());
  }
}