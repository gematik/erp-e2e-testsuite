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
 */

package de.gematik.test.erezept.cli.cfg;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.config.PartialConfigSubstituter;
import de.gematik.test.erezept.config.TestsuiteconfigurationScope;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import java.io.*;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ErpEnvironmentsConfiguration {

  public static final String CONF_SYS_PROP = "erp.cli.patient.envs";
  public static final String CON_ENV_VAR = "ERP_CLI_PATIENT_ENVS";

  private ErpEnvironmentsList environments;

  public EnvironmentConfiguration getEnvironment(String name) {
    return this.getEnvironments().stream()
        .filter(env -> env.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () -> new ConfigurationException(format("No Environment called {0} was found", name)));
  }

  public List<EnvironmentConfiguration> getEnvironments() {
    if (this.environments == null) {
      this.readEnvironmentConfigurations();
    }

    return environments.getEnvironments();
  }

  private void readEnvironmentConfigurations() {
    val envProperty = System.getProperty(CONF_SYS_PROP, System.getenv(CON_ENV_VAR));
    File configFile;
    String readFrom;
    if (envProperty != null && !envProperty.isEmpty()) {
      configFile = getFromCustomPath(Path.of(envProperty));
      readFrom = envProperty;
    } else {
      configFile = getDefault();
      readFrom = "packaged resource";
    }

    val mapper = new ObjectMapper(new YAMLFactory());

    try {
      val template = mapper.readTree(configFile);
      val finalConfig =
          PartialConfigSubstituter.forScope(TestsuiteconfigurationScope.ERP_PRIMSYS)
              .applyUpdates(configFile, template)
              .toString();
      this.environments = mapper.readValue(finalConfig, ErpEnvironmentsList.class);
      log.info(format("Read Environment Configurations from {0}", readFrom));
    } catch (IOException e) {
      throw new ConfigurationException(
          format("Environment Configurations read from ''{0}'' are invalid", readFrom));
    }
  }

  private File getDefault() {
    val envFileUrl = this.getClass().getResource("/erp_environments.yaml");

    if (envFileUrl == null) {
      // this case will happen when
      // 1. no environment configuration file is provided via system property or environment
      // 2. AND no erp_environments.yaml is packaged into jar
      // variable
      throw new ConfigurationException(
          "No Environment Configuration was configured and no default configuration is packaged");
    }

    return getFromCustomPath(Path.of(envFileUrl.getPath()));
  }

  private File getFromCustomPath(Path path) {
    val file = path.toFile();

    if (!file.exists() || !file.isFile()) {
      throw new ConfigurationException(
          format("Environment Configuration {0} was not found", path.toAbsolutePath()));
    }
    return file;
  }
}
