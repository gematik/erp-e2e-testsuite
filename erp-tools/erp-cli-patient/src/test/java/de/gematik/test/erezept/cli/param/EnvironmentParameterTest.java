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

package de.gematik.test.erezept.cli.param;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.cli.cfg.ErpEnvironments;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junitpioneer.jupiter.ClearSystemProperty;
import picocli.CommandLine;

class EnvironmentParameterTest {

  @Test
  void shouldReadDefaultConfiguration() {
    val ep = new EnvironmentParameter();
    assertDoesNotThrow(ep::getEnvironment);
  }

  @ParameterizedTest(
      name = "[index] Read Environment Configuration for {0} from default Configuration")
  @ValueSource(strings = {"tu", "RU", "RU-dev"})
  void shouldReadGivenEnvironmentFromDefaultConfiguration(String env) {
    val ep = new EnvironmentParameter();
    val cmdline = new CommandLine(ep);
    assertDoesNotThrow(() -> cmdline.parseArgs("--env", env));

    val ec = ep.getEnvironment();
    assertTrue(env.equalsIgnoreCase(ec.getName()));
  }

  @Test
  void shouldFailOnNonExistentEnvironment() {
    val ep = new EnvironmentParameter();
    val cmdline = new CommandLine(ep);
    assertDoesNotThrow(() -> cmdline.parseArgs("--env", "hello"));

    assertThrows(ConfigurationException.class, ep::getEnvironment);
  }

  @ParameterizedTest(
      name =
          "[index] Read Environment Configuration for {0} from Configuration via System Property")
  @ValueSource(strings = {"tu", "RU", "RU-dev"})
  @ClearSystemProperty(key = ErpEnvironments.CONF_SYS_PROP)
  void shouldReadEnvironmentConfigurationViaSysProp(String env) {
    // prepare copy and system property
    val testConfiguration = copyDefaultConfiguration();
    System.setProperty(ErpEnvironments.CONF_SYS_PROP, testConfiguration.getAbsolutePath());

    // execute the testcase
    val ep = new EnvironmentParameter();
    val cmdline = new CommandLine(ep);
    assertDoesNotThrow(() -> cmdline.parseArgs("--env", env));

    val ec = ep.getEnvironment();
    assertTrue(env.equalsIgnoreCase(ec.getName()));
  }

  @Test
  @ClearSystemProperty(key = ErpEnvironments.CONF_SYS_PROP)
  void shouldFailOnMissingSysPropFile() {
    // prepare system property
    System.setProperty(ErpEnvironments.CONF_SYS_PROP, Path.of("a", "b", "c").toString());

    // execute the testcase
    val ep = new EnvironmentParameter();
    assertThrows(ConfigurationException.class, ep::getEnvironment);
  }

  @Test
  @ClearSystemProperty(key = ErpEnvironments.CONF_SYS_PROP)
  void shouldUseDefaultOnEmptySysProp() {
    // prepare empty system property
    System.setProperty(ErpEnvironments.CONF_SYS_PROP, "");

    // execute the testcase
    val ep = new EnvironmentParameter();
    assertDoesNotThrow(ep::getEnvironment);
  }

  @Test
  @ClearSystemProperty(key = ErpEnvironments.CONF_SYS_PROP)
  void shouldFailOnInvalidConfiguration() {
    val testConfiguration = createInvalidConfiguration();
    System.setProperty(ErpEnvironments.CONF_SYS_PROP, testConfiguration.getAbsolutePath());

    val ep = new EnvironmentParameter();
    assertThrows(ConfigurationException.class, ep::getEnvironment);
  }

  @SneakyThrows
  private File copyDefaultConfiguration() {
    try (val template =
        Objects.requireNonNull(this.getClass().getResourceAsStream("/erp_environments.yaml"))) {
      val outputFile = createOutputFilePath("test_envs");
      Files.copy(template, outputFile, StandardCopyOption.REPLACE_EXISTING);
      return outputFile.toFile();
    }
  }

  @SneakyThrows
  private File createInvalidConfiguration() {
    val outputFile = createOutputFilePath("invalid_test_envs");
    try (val writer = new FileOutputStream(outputFile.toFile())) {
      writer.write("HelloWorld".getBytes());
    }
    return outputFile.toFile();
  }

  private Path createOutputFilePath(String prefix) {
    val fileName = createYamlFilename(prefix);
    val path = Path.of(System.getProperty("user.dir"), "target", "tmp", fileName);
    path.getParent().toFile().mkdirs();
    return path;
  }

  private String createYamlFilename(String prefix) {
    val timestamp = new SimpleDateFormat("yyyy-mm-dd-hh-mm-ss").format(new Date());
    return format("{0}_{1}.yaml", prefix, timestamp);
  }
}
