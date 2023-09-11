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

package de.gematik.test.erezept.cli.cfg;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ErpEnvironments {

  public static final String CONF_SYS_PROP = "erp.cli.patient.envs";
  public static final String CON_ENV_VAR = "ERP_CLI_PATIENT_ENVS";
  private List<EnvironmentConfiguration> environments;

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
    
    return environments;
  }

  private void readEnvironmentConfigurations() {
    val envProperty = System.getProperty(CONF_SYS_PROP, System.getenv(CON_ENV_VAR));
    InputStream configFile;
    String readFrom;
    if (envProperty != null && !envProperty.isEmpty()) {
      configFile = readFromFile(Path.of(envProperty));
      readFrom = envProperty;
    } else {
      configFile = readDefault();
      readFrom = "packaged resource";
    }

    val mapper = new ObjectMapper(new YAMLFactory());

    try {
      this.environments =
          mapper.readValue(configFile, new TypeReference<List<EnvironmentConfiguration>>() {});
      log.info(format("Read Environment Configurations from {0}", readFrom));
    } catch (IOException e) {
      throw new ConfigurationException(format("Environment Configurations read from ''{0}'' are invalid", readFrom));
    }
  }

  private InputStream readDefault() {
    val stream = this.getClass().getResourceAsStream("/erp_environments.yaml");
    if (stream == null) {
      // this case will happen when
      // 1. no erp_environments.yaml is packaged into jar
      // 2. no environment configuration file is provided via system property or environment variable
      throw new ConfigurationException("No Environment Configuration was configured and no default configuration is packaged");
    }
    return stream;
  }
  
  private InputStream readFromFile(Path path) {
    try {
      return new FileInputStream(path.toFile());
    } catch (FileNotFoundException e) {
      throw new ConfigurationException(format("Environment Configuration {0} was not found", path.toAbsolutePath()));
    }
  }
}
