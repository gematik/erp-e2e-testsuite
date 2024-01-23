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

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import java.util.Optional;
import java.util.function.Predicate;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.exceptions.FHIRException;
import org.hl7.fhir.r4.model.Bundle.BundleType;

@Slf4j
public class ProfileExtractor {

  private static final String PROFILE_LITERAL = "profile";
  private static final String META_LITERAL = "meta";
  private static final String TYPE_LITERAL = "type";
  private static final String ENTRY_LITERAL = "entry";
  private static final String VALUE_LITERAL = "value";

  private ProfileExtractor() {
    throw new IllegalStateException("Utility class");
  }

  public static Optional<String> extractProfile(String content) {
    val root = encodeContent(content);
    return root.flatMap(r -> extractProfileValue(r, content));
  }

  /**
   * For type hinting, it is required to know if the given resource is a searchset bundle without
   * any explicit profile. In such cases, a resource might contain entries from different profile
   * sets and thus needs to be validated differently
   *
   * @param content to be encoded
   * @return true if the content is a searchset without an explicit profile and false otherwise
   */
  public static boolean isUnprofiledSearchSet(String content) {
    return encodeContent(content)
        .filter(root -> extractProfileFromRoot(root).isEmpty())
        .map(root -> isOfType(root, BundleType.SEARCHSET))
        .orElse(false);
  }

  private static Optional<JsonNode> encodeContent(String content) {
    val encoding = EncodingType.guessFromContent(content);
    val mapper = encoding.choose(XmlMapper::new, JsonMapper::new);
    try {
      val root = mapper.readTree(content);
      return Optional.of(root);
    } catch (JsonProcessingException jpe) {
      log.warn(
          format(
              "Given content cannot be parsed as JSON/XML: {0}",
              shortenContentForLogging(content)));
      return Optional.empty();
    }
  }

  /**
   * This method will take the root node and extract, depending on the concrete type, the next
   * profile-node
   *
   * @param root node
   * @return the nearest profile-node from root or empty if no profile-node was found
   */
  private static Optional<JsonNode> extractProfileNode(JsonNode root) {
    if (isOfType(root, BundleType.COLLECTION)) {
      return extractProfileFromCollection(root);
    } else {
      return extractProfileFromRoot(root);
    }
  }

  /**
   * This method will extract the profile tag depending on the type of the resource, and if a
   * profile tag was found the concrete string-value will be extracted. In some cases, resources
   * might contain multiple profiles: this method will take always the first profile in such cases
   *
   * @param root node
   * @param content representing the raw resource
   * @return the found profile-string or empty of no profile was found
   */
  private static Optional<String> extractProfileValue(JsonNode root, String content) {
    val profileNode = extractProfileNode(root);

    // 1. on array take the first element, otherwise take the value by name
    // 2. map the profile-node to string value
    // 4. filter out empty strings as these won't ever give any benefit on choosing a validator
    // 5. log info about missing profile
    return profileNode
        .map(profile -> profile.isArray() ? profile.get(0) : profile.get(VALUE_LITERAL))
        .map(JsonNode::asText)
        .filter(Predicate.not(String::isEmpty))
        .or(
            () -> {
              log.info(
                  format(
                      "Given content does not contain a profile: {0}",
                      shortenContentForLogging(content)));
              return Optional.empty();
            });
  }

  /**
   * Special case for collection bundles: if no profile is found on root, extract the profile-node
   * from the first entry which has a non-empty profile
   *
   * @param root node
   * @return the JsonNode of the first found profile within the whole collection or empty if no
   *     profiles found
   */
  private static Optional<JsonNode> extractProfileFromCollection(JsonNode root) {
    return extractProfileFromRoot(root)
        .or(
            () ->
                root.findValues(ENTRY_LITERAL).stream()
                    .map(entry -> entry.findValue(META_LITERAL))
                    .filter(ProfileExtractor::filterEmptyProfiles)
                    .map(meta -> meta.findValue(PROFILE_LITERAL))
                    .findFirst());
  }

  /**
   * This is the most common way which is applied to most of the resources (exception collections).
   * In this case, the profile is strictly extracted from root.meta.profile without deep-diving into
   * child nodes
   *
   * @param root node
   * @return the JsonNode of the profile in the root node or empty if the root node does not have a
   *     profile
   */
  private static Optional<JsonNode> extractProfileFromRoot(JsonNode root) {
    // find the first meta-tag on the root
    val meta = root.get(META_LITERAL);
    return Optional.ofNullable(meta)
        .filter(ProfileExtractor::filterEmptyProfiles)
        .flatMap(m -> Optional.ofNullable(m.get(PROFILE_LITERAL)));
  }

  /**
   * filter all the found meta-tags for such tags which contain a non-empty profile value
   *
   * @param meta
   * @return
   */
  private static boolean filterEmptyProfiles(JsonNode meta) {
    var profile = meta.findValue(PROFILE_LITERAL);
    if (profile == null) return false;
    profile = profile.isArray() ? profile.get(0) : profile.get(VALUE_LITERAL);
    return !profile.asText().isEmpty();
  }

  private static boolean isOfType(JsonNode root, BundleType type) {
    val extractedType = extractBundleType(root);
    return extractedType.equals(type);
  }

  /**
   * Extracts the type of bundle resource
   *
   * @param root node
   * @return the BundleType which might be also NULL-Type from {@link
   *     org.hl7.fhir.r4.model.Bundle.BundleType}
   */
  @Nonnull
  @SuppressWarnings("java:S2637") // null is properly handled via BundleType.NULL here!
  private static BundleType extractBundleType(JsonNode root) {
    val typeNode = root.get(TYPE_LITERAL);

    try {
      return Optional.ofNullable(typeNode)
          .map(
              node ->
                  node.getNodeType().equals(JsonNodeType.OBJECT) ? node.get(VALUE_LITERAL) : node)
          .map(node -> BundleType.fromCode(node.asText()))
          .orElse(BundleType.NULL);
    } catch (FHIRException fe) {
      log.warn(format("Unable to extract FHIR BundleType from type-node {0}", typeNode));
      return BundleType.NULL;
    }
  }

  /**
   * The content which is parsed is usually very long and would pollute the log. In most cases, the
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
