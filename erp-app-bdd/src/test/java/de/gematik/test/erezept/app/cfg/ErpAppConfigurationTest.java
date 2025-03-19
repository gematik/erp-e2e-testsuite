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

package de.gematik.test.erezept.app.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErpAppConfigurationTest {

  private static ErpAppConfiguration config;

  @BeforeAll
  static void readConfig() {
    config = ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
  }

  @Test
  void shouldNotHaveSingleInstance() {
    val c2 = ConfigurationReader.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
    assertNotEquals(config, c2);
  }

  @Test
  void shouldGetUserByName() {
    val user = config.getAppUserByName("Alice");
    assertNotNull(user);
  }

  @Test
  void shouldThrowOnUnknownUser() {
    assertThrows(ConfigurationMappingException.class, () -> config.getAppUserByName("Charlie"));
  }

  @Test
  void shouldGetDeviceByName() {
    val device = config.getDeviceByName("iPhone 7 Plus Simulator");
    assertNotNull(device);
  }

  @Test
  void shouldThrowOnUnknownDevice() {
    assertThrows(
        ConfigurationMappingException.class, () -> config.getDeviceByName("Bad Device Name"));
  }

  @Test
  void shouldGetAppConfiguration() {
    val platforms = List.of(PlatformType.ANDROID, PlatformType.IOS);
    platforms.forEach(
        p -> {
          val appConfig = config.getAppConfiguration(p);
          assertNotNull(appConfig);
        });
  }

  @Test
  void shouldThrowOnInvalidPlatform() {
    assertThrows(
        ConfigurationMappingException.class,
        () -> config.getAppConfiguration(PlatformType.DESKTOP));
  }

  @Test
  void shouldGetAppConfigurationForUser() {
    val appConfig = config.getAppConfigurationForUser("Alice");
    val userDevice = config.getAppUserByName("Alice").getDevice();
    val expected = config.getDeviceByName(userDevice).getPlatform();
    assertEquals(expected, appConfig.getPlatform());
    assertEquals(
        PlatformType.fromString(expected), PlatformType.fromString(appConfig.getPlatform()));
  }

  @Test
  void shouldThrowOnNullUser() {
    assertThrows(
        ConfigurationMappingException.class,
        () -> config.getAppConfigurationForUser(null)); // NOSONAR null by intention
  }

  @Test
  void shouldGetAppiumConfigurationByName() {
    val appium = config.getAppiumConfiguration("local");
    assertNotNull(appium);
  }

  @Test
  void shouldThrowOnNullAppiumConfiguration() {
    assertThrows(
        ConfigurationMappingException.class,
        () -> config.getAppiumConfiguration(null)); // NOSONAR null by intention
  }

  @Test
  void shouldThrowOnInvalidAppiumConfigurationName() {
    assertThrows(
        ConfigurationMappingException.class, () -> config.getAppiumConfiguration("remote"));
  }
}
