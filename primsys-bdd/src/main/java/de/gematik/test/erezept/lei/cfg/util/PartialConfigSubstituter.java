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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PartialConfigSubstituter {

  static final String SYS_PROP_PREFIX = "erp.config";
  private static final String FILE_EXTENSION = ".pcs";

  private PartialConfigSubstituter() {
    throw new IllegalArgumentException("static class");
  }

  public static String prependPrefix(String value) {
    String result = value;
    if (!result.startsWith(SYS_PROP_PREFIX)) {
      result = format("{0}.{1}", SYS_PROP_PREFIX, value);
    }
    return result;
  }

  /**
   * This method will partially substitute configuration values from a config. 1. a machine specific
   * .pcs file (living in the same directory as the config file) will be read 2. the values from
   * .pcs file will be additionally extended/overwritten with given system properties 3. the
   * resulting entries will be applied to original config file
   *
   * @param parent either the config file or the directory where to look for the machine specific
   *     .pcs file
   * @param root is source root containing the original content of the config file
   * @return an updated config file root with all the substitutions
   */
  public static JsonNode applyUpdates(File parent, JsonNode root) {
    val kvpList = readUpdatesFor(parent);
    kvpList.forEach(kvp -> applyUpdate(root, kvp));
    return root;
  }

  private static void applyUpdate(JsonNode root, KeyValuePair kvp) {
    log.trace(format("Traverse: {0}", kvp.getNonPrefixedKey()));
    val leafNodeParents = traverse(root, kvp);
    log.trace(
        format(
            "Found {0} leaf elements for key {1}",
            leafNodeParents.size(), kvp.getNonPrefixedKey()));
    leafNodeParents.forEach(p -> ((ObjectNode) p).put(kvp.getLeafKey(), kvp.value));
  }

  private static List<JsonNode> traverse(JsonNode root, KeyValuePair kvp) {
    val keyElements = kvp.tokenizeParent();

    List<JsonNode> leafNodes = new ArrayList<>();
    leafNodes.add(root);
    for (val element : keyElements) {
      leafNodes = traverseElement(element, leafNodes);
    }
    return leafNodes;
  }

  private static List<JsonNode> traverseElement(String element, List<JsonNode> leafNodes) {
    val nextNodes = new ArrayList<JsonNode>();
    for (var node : leafNodes) {
      if (element.startsWith("#")) {
        val idx = Integer.parseInt(element.replace("#", ""));
        nextNodes.add(node.get(idx));
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

  private static List<KeyValuePair> readUpdatesFor(File configFile) {
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
              .filter(PartialConfigSubstituter::isUserMatchingPcsFile)
              .findFirst();
    }

    optionalPcsFile.ifPresent(pcs -> log.info(format("Read .pcs-file from {0}", pcs)));

    AtomicReference<List<KeyValuePair>> kvpList = new AtomicReference<>();
    optionalPcsFile.ifPresentOrElse(
        f -> kvpList.set(mergeSystemProperties(f)), () -> kvpList.set(readSystemProperties()));
    return kvpList.get();
  }

  @SneakyThrows
  private static List<KeyValuePair> mergeSystemProperties(File pcsFile) {
    val ret = new LinkedList<KeyValuePair>();

    try (val br = new BufferedReader(new FileReader(pcsFile))) {
      br.lines()
          .filter(line -> !line.startsWith("#")) // filter comment lines
          .filter(line -> !line.isEmpty() && !line.isBlank()) // filter empty lines
          .map(PartialConfigSubstituter::prependPrefix)
          .map(PartialConfigSubstituter::splitLine)
          .forEach(ret::add);
    }

    // add system properties afterwards as these must override the .pcs values
    ret.addAll(readSystemProperties());
    return ret;
  }

  /**
   * this will read only the system properties in case no pcs-file was found
   *
   * @return erp.config related system properties
   */
  private static List<KeyValuePair> readSystemProperties() {
    return System.getProperties().entrySet().stream()
        .filter(entry -> ((String) entry.getKey()).startsWith(SYS_PROP_PREFIX))
        .map(entry -> KeyValuePair.create((String) entry.getKey(), (String) entry.getValue()))
        .toList();
  }

  private static KeyValuePair splitLine(String line) {
    val pair = line.split("=");
    if (pair.length != 2) {
      throw new IllegalArgumentException(format("Given line in .pcs File is invalid: {0}", line));
    }
    return KeyValuePair.create(pair[0], pair[1]);
  }

  private static boolean isUserMatchingPcsFile(File file) {
    val fileName = file.getName().toLowerCase();
    return fileName.contains(getHostName().toLowerCase())
        || fileName.contains(getUsername().toLowerCase());
  }

  private static String getHostName() {
    String hostName;
    try {
      hostName = InetAddress.getLocalHost().getHostName();
    } catch (UnknownHostException e) {
      log.warn(
          "Could not find name of the machine: possibly desired configuration diff files cannot be used!");
      hostName = UUID.randomUUID().toString(); // just return something random!
    }

    return hostName.split("\\.")[0];
  }

  private static String getUsername() {
    return System.getProperty("user.name");
  }

  @AllArgsConstructor(access = AccessLevel.PRIVATE)
  private static class KeyValuePair {
    private final String key;
    private final String value;

    /**
     * create the key value pair if a value for the key was given via system property the given
     * value will be ignored
     *
     * @param key is the property key
     * @param value is the value which will be taken if no system property was given
     * @return a key value pair
     */
    private static KeyValuePair create(String key, String value) {
      val keyStripped = key.strip();
      val propertyValue = System.getProperty(keyStripped, value.strip());
      return new KeyValuePair(keyStripped, propertyValue);
    }

    public String getNonPrefixedKey() {
      return key.replace(format("{0}.", SYS_PROP_PREFIX), "");
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
  }
}
