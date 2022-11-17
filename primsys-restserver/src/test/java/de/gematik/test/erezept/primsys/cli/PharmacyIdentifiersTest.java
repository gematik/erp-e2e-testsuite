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

package de.gematik.test.erezept.primsys.cli;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

@Disabled
class PharmacyIdentifiersTest {

  @Test
  @SneakyThrows
  void shouldRunWithoutExceptions() {
    val subcommand = new PharmacyIdentifiers();
    try {
      assertDoesNotThrow(subcommand::call);
    } finally {
      // reset the config singleton!
      Field instance;
      instance = TestsuiteConfiguration.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    }
  }

  @Test
  @SneakyThrows
  void shouldHandleTailingSlashesCorrectly() {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    val stream = new PrintStream(baos);
    val subcommand = new PharmacyIdentifiers(stream);
    val cmdline = new CommandLine(subcommand);

    val urlRegex =
        "(http(s)?:\\/\\/)(www)?([-a-zA-Z0-9@:%._\\+~#=]{2,256})\\.[a-z]{2,6}\\/(\\w+)\\/(\\w+)";
    val urlPattern = Pattern.compile(urlRegex);
    val baseUrl = "http://hello.world";

    try {
      List.of(baseUrl, baseUrl + "/")
          .forEach(
              url -> {
                cmdline.parseArgs("--url", url);
                assertDoesNotThrow(subcommand::call);
                Arrays.stream(baos.toString().split("\\n"))
                    .filter(line -> line.startsWith("\t"))
                    .map(line -> line.substring(line.indexOf(url)))
                    .forEach(
                        line -> {
                          // make sure the produced URL is correct and matches the regex
                          assertTrue(
                              urlPattern.matcher(line).matches(),
                              format("Produced URL {0} does not match expectation", line));
                        });
              });
    } finally {
      // reset the config singleton!
      Field instance;
      instance = TestsuiteConfiguration.class.getDeclaredField("instance");
      instance.setAccessible(true);
      instance.set(null, null);
    }
  }
}
