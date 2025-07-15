/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.config;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.gematik.test.erezept.config.exceptions.PcsExpressionException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

// NOTE: PartialConfigSubstituter can/should be package private
@Slf4j
public class PartialConfigSubstituter {

  private static final String FILE_EXTENSION = ".pcs";
  private final ConfigurationScope scope;

  private PartialConfigSubstituter(ConfigurationScope scope) {
    this.scope = scope;
  }

  public static PartialConfigSubstituter forScope(ConfigurationScope scope) {
    return new PartialConfigSubstituter(scope);
  }

  /**
   * This method will partially substitute configuration values from a config. 1. a machine specific
   * .pcs files (living in the same directory as the config file) will be read 2. the values from
   * .pcs files will be additionally extended/overwritten with given system properties 3. the
   * resulting entries will be applied to the original config file
   *
   * @param parent either the config file or the directory where to look for the machine specific
   *     .pcs file
   * @param root is source root containing the original content of the config file
   * @return an updated config file root with all the substitutions
   */
  public JsonNode applyUpdates(File parent, JsonNode root) {
    val privateKvpList = readPrivateUpdatesFor(parent);
    privateKvpList.forEach(kvp -> applyUpdate(root, kvp));

    val kvpList = readUpdatesFor(parent);
    kvpList.forEach(kvp -> applyUpdate(root, kvp));
    return root;
  }

  private String prependPrefix(String value) {
    String result = value;
    if (!result.startsWith(scope.getScopePrefix())) {
      result = format("{0}.{1}", scope.getScopePrefix(), value);
    }
    return result;
  }

  private void applyUpdate(JsonNode root, KeyValuePair kvp) {
    log.trace("Traverse: {}", kvp.getNonPrefixedKey());
    val leafNodeParents = traverse(root, kvp);
    log.trace("Found {} leaf elements for key {}", leafNodeParents.size(), kvp.getNonPrefixedKey());
    leafNodeParents.forEach(
        p -> {
          val value = kvp.value.equalsIgnoreCase("null") ? null : kvp.value;
          ((ObjectNode) p).put(kvp.getLeafKey(), value);
        });
  }

  private List<JsonNode> traverse(JsonNode root, KeyValuePair kvp) {
    val keyElements = kvp.tokenizeParent();

    List<JsonNode> leafNodes = new ArrayList<>();
    leafNodes.add(root);
    for (val element : keyElements) {
      leafNodes = traverseElement(element, leafNodes);
    }
    return leafNodes;
  }

  private List<JsonNode> traverseElement(String element, List<JsonNode> leafNodes) {
    val nextNodes = new ArrayList<JsonNode>();
    for (var node : leafNodes) {
      if (element.startsWith("#")) {
        val idx = Integer.parseInt(element.replace("#", ""));
        val chosenNode =
            Optional.ofNullable(node.get(idx))
                .orElseThrow(
                    () ->
                        new PcsExpressionException(
                            format(
                                "Could not find element {0} in configuration: probably the index"
                                    + " {1} is out of range",
                                element, idx)));
        nextNodes.add(chosenNode);
      } else if (element.equals("*")) {
        for (var i = 0; i < node.size(); i++) {
          nextNodes.add(node.get(i));
        }
      } else {
        nextNodes.add(node.get(element));
      }
    }
    return nextNodes;
  }

  private List<KeyValuePair> readUpdatesFor(File configFile) {
    // are any additional config files next to the main config file?
    val parent = configFile.isFile() ? configFile.getParentFile() : configFile;
    val additionalFiles = Objects.requireNonNull(parent.listFiles());

    // look for the Partial Configuration Substitution file containing the Hostname
    val useAutoPcs = Boolean.parseBoolean(System.getProperty("erp.pcs.auto", "true"));
    Optional<File> optionalPcsFile = Optional.empty();

    if (useAutoPcs) {
      optionalPcsFile =
          Arrays.stream(additionalFiles)
              .filter(f -> f.getName().endsWith(FILE_EXTENSION))
              .filter(this::isUserMatchingPcsFile)
              .findFirst();
    }

    optionalPcsFile.ifPresent(pcs -> log.info("Read .pcs-file from {}", pcs));

    AtomicReference<List<KeyValuePair>> kvpList = new AtomicReference<>();
    optionalPcsFile.ifPresentOrElse(
        f -> kvpList.set(mergeSystemProperties(f)), () -> kvpList.set(readSystemProperties()));
    return kvpList.get();
  }

  private List<KeyValuePair> readPrivateUpdatesFor(File configFile) {
    // are any additional config files next to the main config file?
    val parent = configFile.isFile() ? configFile.getParentFile() : configFile;
    val additionalFiles = Objects.requireNonNull(parent.listFiles());

    AtomicReference<List<KeyValuePair>> kvpList = new AtomicReference<>();
    Arrays.stream(additionalFiles)
        .filter(f -> f.getName().equals("secret.ppcs"))
        .findFirst()
        .ifPresentOrElse(
            ppcs -> {
              log.info("Read secrets from {}", ppcs);
              kvpList.set(mergeSystemProperties(ppcs));
            },
            () -> kvpList.set(readSystemProperties()));

    return kvpList.get();
  }

  @SneakyThrows
  private List<KeyValuePair> mergeSystemProperties(File pcsFile) {
    val ret = new LinkedList<KeyValuePair>();

    try (val br = new BufferedReader(new FileReader(pcsFile))) {
      br.lines()
          .filter(line -> !line.startsWith("#")) // filter comment lines
          .filter(line -> !line.isBlank()) // filter empty lines
          .map(this::prependPrefix)
          .map(this::splitLine)
          .forEach(ret::add);
    }

    // add system properties afterward as these must override the .pcs values
    ret.addAll(readSystemProperties());
    return ret;
  }

  /**
   * this will read only the system properties in case no pcs-file was found
   *
   * @return erp.config related system properties
   */
  private List<KeyValuePair> readSystemProperties() {
    return System.getProperties().entrySet().stream()
        .filter(entry -> ((String) entry.getKey()).startsWith(this.scope.getScopePrefix()))
        .map(
            entry ->
                KeyValuePair.create(this.scope, (String) entry.getKey(), (String) entry.getValue()))
        .toList();
  }

  private KeyValuePair splitLine(String line) {
    val separatorIdx = line.indexOf("=");

    if (separatorIdx == -1) {
      throw new IllegalArgumentException(format("Given line in .pcs File is invalid: {0}", line));
    }
    val key = line.substring(0, separatorIdx).strip();
    val value = line.substring(separatorIdx + 1).strip();

    return KeyValuePair.create(this.scope, key, value);
  }

  private boolean isUserMatchingPcsFile(File file) {
    val fileName = file.getName().toLowerCase();
    return fileName.contains(getHostName().toLowerCase())
        || fileName.contains(getUsername().toLowerCase());
  }

  private String getHostName() {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.warn(
          "Could not find name of the machine: possibly desired configuration diff files cannot be"
              + " used!");
      hostName = UUID.randomUUID().toString(); // just return something random!
    }

    return hostName.split("\\.")[0];
  }

  private String getUsername() {
    return System.getProperty("user.name");
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static class KeyValuePair {
    private final ConfigurationScope scope;
    private final String key;
    private final String value;

    public String getNonPrefixedKey() {
      return key.replace(format("{0}.", scope.getScopePrefix()), "");
    }

    private String[] tokenize() {
      return this.getNonPrefixedKey().split("\\.");
    }

    public String getLeafKey() {
      val elements = tokenize();
      val leafIdx = elements.length - 1;
      return elements[leafIdx];
    }

    public String[] tokenizeParent() {
      val elements = tokenize();
      if (elements.length > 1) {
        val parentLen = elements.length - 1;
        return Arrays.copyOfRange(elements, 0, parentLen);
      } else {
        // key is already a leaf node, no parent keys
        return new String[] {};
      }
    }

    /**
     * create the key value pair if a value for the key was given via system property the given
     * value will be ignored
     *
     * @param key is the property key
     * @param value is the value which will be taken if no system property was given
     * @return a key value pair
     */
    private static KeyValuePair create(ConfigurationScope scope, String key, String value) {
      val keyStripped = key.strip();
      val propertyValue = System.getProperty(keyStripped, value.strip());
      return new KeyValuePair(scope, keyStripped, propertyValue);
    }
  }
}
