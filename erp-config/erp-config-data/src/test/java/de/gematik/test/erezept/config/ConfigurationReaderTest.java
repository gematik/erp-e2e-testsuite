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

package de.gematik.test.erezept.config;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.app.ErpAppConfigurationBase;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorModuleConfigurationDto;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.config.dto.psp.PSPClientConfig;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ClearSystemProperty.ClearSystemProperties;

class ConfigurationReaderTest {

  @Test
  void shouldReadPrimSysConfiguration() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    assertDoesNotThrow(
        () -> ConfigurationReader.forPrimSysConfiguration().configFile(templatePath).create());
  }

  @Test
  void shouldReadPrimSysConfigurationWithWrapper() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    assertDoesNotThrow(
        () ->
            ConfigurationReader.forPrimSysConfiguration()
                .configFile(templatePath)
                // just for coverage, won't affect the configuration
                .registerSubtype(PSPClientConfig.class, "PSP")
                .wrappedBy(PrimSysTestWrapper::new, false));
  }

  @Test
  void shouldContainSecuritySubstitutions() {
    val templatePath =
        Path.of(System.getProperty("user.dir"), "..", "..", "config", "erp-app", "config.yaml");

    val config =
        ConfigurationReader.forAppConfiguration()
            .configFile(templatePath)
            .wrappedBy(AppTestWrapper::new, true);

    config.dto.getAppium().forEach(appium -> assertNotEquals("DUMMY VALUE", appium.getAccessKey()));
  }

  @Test
  void shouldReadAppConfiguration() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/app_config.yaml")
                .getPath());

    val config =
        assertDoesNotThrow(
            () -> ConfigurationReader.forAppConfiguration().configFile(templatePath).create());
    val app = config.getApps().get(0);
    assertEquals(app.getPlatform(), app.getName());
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.activeEnvironment")
  void shouldReadPrimSysConfigurationWithPcs() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    System.setProperty("erp.primsys.activeEnvironment", "RU");
    val cfg = ConfigurationReader.forPrimSysConfiguration().configFile(templatePath).create(true);
    assertEquals("RU", cfg.getActiveEnvironment());
  }

  @Test
  void shouldReadStandaloneKonnektorConfiguration() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/konnektors.yaml")
                .getPath());
    val cfg =
        ConfigurationReader.forKonnektorClient()
            .configFile(templatePath)
            .wrappedBy(KonnektorModuleTestWrapper::new);
    assertEquals(2, cfg.dto.getKonnektors().size());
  }

  @Test
  @ClearSystemProperty(key = "erp.primsys.activeEnvironment")
  void shouldReadPrimSysConfigurationWithoutPcs() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    System.setProperty("erp.primsys.activeEnvironment", "RU");
    val cfg = ConfigurationReader.forPrimSysConfiguration().configFile(templatePath).create(false);
    assertEquals("TU", cfg.getActiveEnvironment());
  }

  @Test
  @ClearSystemProperty(key = "erp.app.shouldLogCapabilityStatement")
  void shouldReadAppConfigurationWithPcs() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/app_config.yaml")
                .getPath());

    System.setProperty("erp.app.shouldLogCapabilityStatement", "false");
    val cfg = ConfigurationReader.forAppConfiguration().configFile(templatePath).create(true);
    assertFalse(cfg.isShouldLogCapabilityStatement());
    cfg.getAppium().forEach(appium -> assertNotNull(appium.getUrl()));
  }

  @Test
  @ClearSystemProperties(
      value = {
        @ClearSystemProperty(key = "erp.app.shouldLogCapabilityStatement"),
        @ClearSystemProperty(key = "erp.primsys.activeEnvironment")
      })
  void shouldReadAppAndPrimsysConfigurationsWithPcs() {
    val appConfigPath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/app_config.yaml")
                .getPath());

    val primsysConfigPath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    System.setProperty("erp.app.shouldLogCapabilityStatement", "false");
    System.setProperty("erp.primsys.activeEnvironment", "TU");
    val appCfg = ConfigurationReader.forAppConfiguration().configFile(appConfigPath).create(true);
    val primsysCfg =
        ConfigurationReader.forPrimSysConfiguration().configFile(primsysConfigPath).create(true);

    assertFalse(appCfg.isShouldLogCapabilityStatement());
    assertEquals("TU", primsysCfg.getActiveEnvironment());
  }

  @Test
  void shouldFailOnPathWithoutDefaultConfiguration() {
    val templatePath =
        Path.of(
                this.getClass()
                    .getClassLoader()
                    .getResource("configurations/primsys_config.yaml")
                    .getPath())
            .getParent();

    assertThrows(
        FileNotFoundException.class,
        () -> ConfigurationReader.forPrimSysConfiguration().configFile(templatePath).create());
  }

  @RequiredArgsConstructor
  private static class PrimSysTestWrapper extends ConfiguredFactory {
    private final PrimsysConfigurationDto dto;
  }

  @RequiredArgsConstructor
  private static class AppTestWrapper extends ConfiguredFactory {
    private final ErpAppConfigurationBase dto;
  }

  @RequiredArgsConstructor
  private static class KonnektorModuleTestWrapper extends ConfiguredFactory {
    private final KonnektorModuleConfigurationDto dto;
  }
}
