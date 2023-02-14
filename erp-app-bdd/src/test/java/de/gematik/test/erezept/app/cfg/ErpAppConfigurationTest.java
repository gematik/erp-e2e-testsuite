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

package de.gematik.test.erezept.app.cfg;

import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.databind.DatabindException;
import de.gematik.test.erezept.app.exceptions.ActorConfigurationNotFoundException;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import java.nio.file.Path;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErpAppConfigurationTest {

  private static ErpAppConfiguration config;

  @BeforeAll
  static void readConfig() {
    config = ErpAppConfiguration.getInstance();
  }

  @Test
  void shouldHaveSingleInstance() {
    val c2 = ErpAppConfiguration.getInstance();
    assertEquals(config, c2);
  }

  @Test
  void shouldThrowOnInvalidConfigurationFile() {
    val path = Path.of("..", "config", "primsys", "config.yaml"); // primsys config!
    assertThrows(DatabindException.class, () -> ErpAppConfiguration.getInstance(path.toFile()));
  }

  @Test
  void shouldGetUserByName() {
    val user = config.getAppUserByName("Alice");
    assertNotNull(user);
  }

  @Test
  void shouldThrowOnUnknownUser() {
    assertThrows(ActorConfigurationNotFoundException.class, () -> config.getAppUserByName("Bob"));
  }

  @Test
  void shouldGetDeviceByName() {
    val device = config.getDeviceByName("Android 12 Emulator");
    assertNotNull(device);
  }

  @Test
  void shouldThrowOnUnknownDevice() {
    assertThrows(
        ActorConfigurationNotFoundException.class, () -> config.getDeviceByName("Bad Device Name"));
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
    val expected = config.getDeviceByName(userDevice).getPlatformType();
    assertEquals(expected, appConfig.getPlatformType());
  }

  @Test
  void shouldThrowOnNullUser() {
    assertThrows(
        NullPointerException.class,
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
        NullPointerException.class,
        () -> config.getAppiumConfiguration(null)); // NOSONAR null by intention
  }

  @Test
  void shouldThrowOnInvalidAppiumConfigurationName() {
    assertThrows(
        ConfigurationMappingException.class, () -> config.getAppiumConfiguration("remote"));
  }
}
