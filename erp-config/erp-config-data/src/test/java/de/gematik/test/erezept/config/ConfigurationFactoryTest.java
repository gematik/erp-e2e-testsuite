package de.gematik.test.erezept.config;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.dto.BaseConfigurationWrapper;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationBase;
import de.gematik.test.erezept.config.dto.psp.PSPClientConfig;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;
import org.junitpioneer.jupiter.ClearSystemProperty.ClearSystemProperties;

class ConfigurationFactoryTest {

  @Test
  void shouldReadPrimSysConfiguration() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/primsys_config.yaml")
                .getPath());

    assertDoesNotThrow(
        () -> ConfigurationFactory.forPrimSysConfiguration().configFile(templatePath).create());
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
            ConfigurationFactory.forPrimSysConfiguration()
                .configFile(templatePath)
                // just for coverage, won't affect the configuration
                .registerSubtype(PSPClientConfig.class, "PSP")
                .wrappedBy(PrimSysTestWrapper::new, false));
  }

  @Test
  void shouldReadAppConfiguration() {
    val templatePath =
        Path.of(
            this.getClass()
                .getClassLoader()
                .getResource("configurations/app_config.yaml")
                .getPath());

    assertDoesNotThrow(
        () -> ConfigurationFactory.forAppConfiguration().configFile(templatePath).create());
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
    val cfg = ConfigurationFactory.forPrimSysConfiguration().configFile(templatePath).create(true);
    assertEquals("RU", cfg.getActiveEnvironment());
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
    val cfg = ConfigurationFactory.forPrimSysConfiguration().configFile(templatePath).create(false);
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
    val cfg = ConfigurationFactory.forAppConfiguration().configFile(templatePath).create(true);
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
    val appCfg = ConfigurationFactory.forAppConfiguration().configFile(appConfigPath).create(true);
    val primsysCfg =
        ConfigurationFactory.forPrimSysConfiguration().configFile(primsysConfigPath).create(true);

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
        () -> ConfigurationFactory.forPrimSysConfiguration().configFile(templatePath).create());
  }

  @RequiredArgsConstructor
  private static class PrimSysTestWrapper implements BaseConfigurationWrapper {

    private final PrimsysConfigurationBase dto;
  }
}
