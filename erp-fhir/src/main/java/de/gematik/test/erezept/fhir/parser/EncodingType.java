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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.parser.IParser;
import de.gematik.test.erezept.fhir.exceptions.UnsupportedEncodingException;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;

/** The only Encodings we support for FHIR representation */
public enum EncodingType {
  XML,
  JSON;

  public static EncodingType fromString(@NonNull String value) {
    EncodingType ret;
    if (value.toLowerCase().contains("xml")) {
      ret = XML;
    } else if (value.toLowerCase().contains("json")) {
      ret = JSON;
    } else {
      throw new UnsupportedEncodingException(
          format("Given encoding {0} is not supported or invalid", value));
    }
    return ret;
  }

  public EncodingType flipEncoding() {
    EncodingType ret;
    if (this == EncodingType.XML) {
      ret = EncodingType.JSON;
    } else {
      ret = EncodingType.XML;
    }
    return ret;
  }

  public String toFileExtension() {
    return this.name().toLowerCase();
  }

  /**
   * Choose one of the two given parsers as a function of the given encoding as a String
   *
   * @param encoding defines which encoding to choose e.g. by a file extension like .xml or simply
   *     json
   * @param xml the XML Parser
   * @param json the JSON Parser
   * @return either the XML or the JSON parser dependent of the given encoding
   */
  public static IParser chooseAppropriateParser(
      @NonNull String encoding, final IParser xml, final IParser json) {
    val type = EncodingType.fromString(encoding);
    return type.chooseAppropriateParser(xml, json);
  }

  /**
   * Choose one of the two given parsers as a function of the actual {@link EncodingType}
   *
   * @param xml the XML Parser
   * @param json the JSON Parser
   * @return either the XML or the JSON parser depending on the actual {@link EncodingType}
   */
  public IParser chooseAppropriateParser(final IParser xml, final IParser json) {
    return chooseAppropriateParser(() -> xml, () -> json);
  }

  public IParser chooseAppropriateParser(Supplier<IParser> xml, Supplier<IParser> json) {
    return choose(xml, json);
  }

  public <T> T choose(Supplier<T> xml, Supplier<T> json) {
    if (this == XML) {
      return xml.get();
    } else if (this == JSON) {
      return json.get();
    } else {
      // should never happen!
      throw new UnsupportedEncodingException(
          format("No appropriate choice for encoding {0}", this));
    }
  }

  /**
   * Try to guess the encoding from the content itself. Use with caution as might be very unreliable
   * in many cases!
   *
   * @param content the content which needs to be parsed
   * @return XML if the content starts with the char {@literal <} (indicating XML) and JSON
   *     otherwise
   */
  public static EncodingType guessFromContent(@NonNull String content) {
    EncodingType ret;
    if (content.startsWith("<")) {
      ret = XML;
    } else {
      ret = JSON;
    }
    return ret;
  }
}
