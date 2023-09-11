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

package de.gematik.test.erezept.cli.converter;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.cli.converter.StringListConverter;
import de.gematik.test.erezept.cli.exceptions.CliException;
import lombok.val;
import org.junit.jupiter.api.Test;

class StringListConverterTest {

  @Test
  void shouldSplitCommaList() throws Exception {
    val input = "first,second,third";
    val converter = new StringListConverter();

    val out = converter.convert(input);
    assertEquals(3, out.size());
    assertTrue(out.contains("first"));
    assertTrue(out.contains("second"));
    assertTrue(out.contains("third"));
  }

  @Test
  void shouldSplitCommaListWithSpaces() throws Exception {
    val input = "first, second, third";
    val converter = new StringListConverter();

    val out = converter.convert(input);
    assertEquals(3, out.size());
    assertTrue(out.contains("first"));
    assertTrue(out.contains("second"));
    assertTrue(out.contains("third"));
  }

  @Test
  void shouldSplitSingleValue() throws Exception {
    val input = "first";
    val converter = new StringListConverter();

    val out = converter.convert(input);
    assertEquals(1, out.size());
    assertTrue(out.contains("first"));
  }

  @Test
  void shouldRemoveEmptyValues() throws Exception {
    val input = "first,,second,";
    val converter = new StringListConverter();

    val out = converter.convert(input);
    assertEquals(2, out.size());
    assertTrue(out.contains("first"));
    assertTrue(out.contains("second"));
  }

  @Test
  void shouldThrowOnEmptyString() {
    val converter = new StringListConverter();

    assertThrows(CliException.class, () -> converter.convert(""));
    assertThrows(CliException.class, () -> converter.convert(" "));
    assertThrows(CliException.class, () -> converter.convert(null));
  }
}
