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

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ProfileExtractor {

  private ProfileExtractor() {
    throw new IllegalStateException("Utility class");
  }

  public static Optional<String> extractProfile(String content) {
    val encoding = EncodingType.guessFromContent(content);
    val mapper = encoding.choose(XmlMapper::new, JsonMapper::new);
    return extract(mapper, content);
  }

  private static Optional<String> extract(ObjectMapper mapper, String content) {
    JsonNode root;
    try {
      root = mapper.readTree(content);
    } catch (JsonProcessingException jpe) {
      log.warn(
          format(
              "Could not parse Profile from given content: {0}",
              shortenContentForLogging(content)));
      return Optional.empty();
    }

    val meta = root.get("meta");
    if (meta == null || meta.isNull() || meta.isEmpty()) {
      log.warn(
          format("Given content does not have a meta-tag: {0}", shortenContentForLogging(content)));
      return Optional.empty();
    }

    val profile = meta.get("profile");
    if (profile == null || profile.isNull() || profile.isEmpty()) {
      log.warn(
          format(
              "Given content does not have a profile-tag in meta: {0}",
              shortenContentForLogging(meta.toString())));
      return Optional.empty();
    }

    if (profile.isArray()) {
      val url = profile.get(0).asText();
      return Optional.of(url);
    } else {
      val url = profile.get("value").asText();
      return Optional.of(url);
    }
  }

  /**
   * The content which is parsed is usually very long and would pollute the log. In most cases the
   * type of the resource and the profile-tag are at the very beginning within a range of
   * approximately 200 characters which will give enough information for debugging purposes
   *
   * @param content which is given to read the profile from
   * @return a shortened content string which should provide just enough information without
   *     polluting the log
   */
  private static String shortenContentForLogging(String content) {
    if (content.length() <= 200) {
      return content;
    } else {
      return format("{0}...", content.substring(0, 200).strip().replaceAll("\\s{2,}", ""));
    }
  }
}
