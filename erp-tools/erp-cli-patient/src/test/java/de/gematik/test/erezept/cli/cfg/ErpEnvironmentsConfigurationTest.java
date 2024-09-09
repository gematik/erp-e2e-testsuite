/*
 * Copyright 2024 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ErpEnvironmentsConfigurationTest {

  @Test
  void shouldReadOnlyOnce() {
    val erpenvs = new ErpEnvironmentsConfiguration();
    val first = erpenvs.getEnvironment("TU");
    val second = erpenvs.getEnvironment("TU");
    assertEquals(first, second);
  }

  @Test
  @ClearSystemProperty(key = "erp.cli.patient.envs")
  void shouldThrowConcreteExceptionOnMissingConfig() {
    System.setProperty("erp.cli.patient.envs", "/a/b/c");
    val erpenvs = new ErpEnvironmentsConfiguration();
    assertThrows(ConfigurationException.class, erpenvs::getEnvironments);
  }

  @Test
  @ClearSystemProperty(key = "erp.cli.patient.envs")
  void shouldThrowConcreteExceptionOnDirectoryPath() {
    System.setProperty("erp.cli.patient.envs", System.getProperty("user.dir"));
    val erpenvs = new ErpEnvironmentsConfiguration();
    assertThrows(ConfigurationException.class, erpenvs::getEnvironments);
  }
}
