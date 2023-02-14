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

package de.gematik.test.erezept.fhir.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.parser.IParser;
import de.gematik.test.erezept.fhir.exceptions.UnsupportedEncodingException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class EncodingTypeTest {

  @Test
  void chooseFromXmlString() {
    val inputs = List.of("Xml", "XML", "some XML", "test.xml");
    inputs.forEach(input -> assertEquals(EncodingType.XML, EncodingType.fromString(input)));
  }

  @Test
  void chooseFromJsonString() {
    val inputs = List.of("Json", "JSON", "some JSON", "test.json");
    inputs.forEach(input -> assertEquals(EncodingType.JSON, EncodingType.fromString(input)));
  }

  @Test
  void toFileExtension() {
    assertEquals("xml", EncodingType.XML.toFileExtension());
    assertEquals("json", EncodingType.JSON.toFileExtension());
  }

  @Test
  void shouldChooseParser() {
    val xmlParser = mock(IParser.class);
    val jsonParser = mock(IParser.class);

    assertEquals(xmlParser, EncodingType.chooseAppropriateParser("xml", xmlParser, jsonParser));
    assertEquals(jsonParser, EncodingType.chooseAppropriateParser("json", xmlParser, jsonParser));
  }

  @Test
  void chooseThrowOnInvalid() {
    val inputs = List.of("Gson", "GSON", "some GSON", "test.gson", "HTML", "test.html");
    inputs.forEach(
        input ->
            assertThrows(UnsupportedEncodingException.class, () -> EncodingType.fromString(input)));
  }
}
