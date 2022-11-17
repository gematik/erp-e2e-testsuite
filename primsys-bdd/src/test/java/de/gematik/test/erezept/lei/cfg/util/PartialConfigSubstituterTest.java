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

package de.gematik.test.erezept.lei.cfg.util;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.Test;

class PartialConfigSubstituterTest {

  @Test
  void shouldPrependPrefixIfNotExistent() {
    val value = "my.property";
    val expected = format("{0}.{1}", PartialConfigSubstituter.SYS_PROP_PREFIX, value);
    val result = PartialConfigSubstituter.prependPrefix(value);
    assertEquals(expected, result);
  }

  @Test
  void shouldNotPrependIfExistent() {
    val value = format("{0}.{1}", PartialConfigSubstituter.SYS_PROP_PREFIX, "my.property");
    val result = PartialConfigSubstituter.prependPrefix(value);
    assertEquals(value, result);
  }
}