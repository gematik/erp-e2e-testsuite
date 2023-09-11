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

package de.gematik.test.erezept.fhir.parser.profiles;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

import java.util.Optional;

import static java.text.MessageFormat.format;

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

  /**
   * For type hinting it is required to know if the given resource is a searchset or a collection
   * bundle. In such cases we cannot simply hint the type by the first hit, thus type hinting must
   * be skipped for such resources
   *
   * @param content to be encoded
   * @return true if the content is a searchset or collection bundle and false otherwise
   */
  public static boolean isSearchSetOrCollection(String content) {
    val encoding = EncodingType.guessFromContent(content);
    val mapper = encoding.choose(XmlMapper::new, JsonMapper::new);

    try {
      val root = mapper.readTree(content);
      val typeNode = root.get("type");

      var type = "";
      if (typeNode != null && typeNode.getNodeType() == JsonNodeType.OBJECT) {
        val value = typeNode.get("value");
        type = value != null ? value.asText("") : "";
      } else if (typeNode != null) {
        type = typeNode.asText("");
      }

      return (type.equalsIgnoreCase("searchset")) || type.equalsIgnoreCase("collection");
    } catch (JsonProcessingException jpe) {
      log.warn(
          format(
              "Given content cannot be parsed and thus cannot be a searchset or collection bundle: {0}",
              shortenContentForLogging(content)));
      return false;
    }
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

    // find the meta tags within the resource
    val metas = root.findValues("meta");
    if (metas.isEmpty()) {
      log.warn(
          format(
              "Given content does not have any meta-tag: {0}", shortenContentForLogging(content)));
      return Optional.empty();
    }

    // find the first meta tag with a defined profile
    val firstProfileMeta = metas.stream().filter(m -> m.findValue("profile") != null).findFirst();
    if (firstProfileMeta.isEmpty()) {
      log.warn(
          format(
              "Given content does not contain any profiles: {0}",
              shortenContentForLogging(content)));
      return Optional.empty();
    }

    val meta = firstProfileMeta.get();
    val profile = meta.get("profile");

    JsonNode profileValue;
    if (profile.isArray()) {
        profileValue = profile.get(0);
    } else {
        profileValue = profile.get("value");
    }
      return (profileValue != null) ? Optional.ofNullable(profileValue.asText()) : Optional.empty();
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
