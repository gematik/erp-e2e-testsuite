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

package de.gematik.test.fuzzing.string;

import static java.text.MessageFormat.format;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class XmlRegExpFactory {

  public static RegExp betweenXmlTag(String tagName) {
    val tag = tagName.replace("<", "").replace(">", "");
    val builder = range(format("<{0}\\s*[^>]*>", tag), format("</{0}>", tag));
    return builder.exclusive();
  }

  public static RangeFilter range(String startElement, String endElement) {
    return new RangeFilter(startElement, endElement);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class RangeFilter {
    private final String startElement;
    private final String endElement;

    public RegExp inclusive() {
      return new RegExp(format("(?<target>{0}.+?{1})", startElement, endElement));
    }

    public RegExp exclusive() {
      return new RegExp(format("({0})(?<target>.+?)({1})", startElement, endElement));
    }
  }
}
