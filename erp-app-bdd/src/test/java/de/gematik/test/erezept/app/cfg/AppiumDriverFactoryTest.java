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

package de.gematik.test.erezept.app.cfg;

import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import java.lang.reflect.Field;
import java.util.stream.IntStream;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.SessionNotCreatedException;

class AppiumDriverFactoryTest {

  private ErpAppConfiguration config;

  @BeforeEach
  void setUp() {
    config = ErpAppConfiguration.getInstance();
  }

  @SneakyThrows
  @AfterEach
  void tearDown() {
    Field instance;
    instance = ErpAppConfiguration.class.getDeclaredField("instance");
    instance.setAccessible(true);
    instance.set(null, null);
  }

  @Test
  void shouldThrowOnUnsupportedPlatform() {
    val devices = config.getDevices();
    val userDevice = config.getAppUserByName("Alice").getDevice();
    val devIdx =
        IntStream.range(0, config.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(userDevice))
            .findFirst()
            .orElse(-1);
    config.getDevices().get(devIdx).setPlatform("Desktop");

    val desktopConfig = new AppConfiguration();
    desktopConfig.setPlatform("Desktop");
    config.getApps().add(desktopConfig);

    assertThrows(
        UnsupportedPlatformException.class, () -> AppiumDriverFactory.forUser("Alice", config));
  }

  @Test
  void shouldThrowOnConnectToAppiumWithIOS() {
    val devices = config.getDevices();
    val userDevice = config.getAppUserByName("Alice").getDevice();
    val devIdx =
        IntStream.range(0, config.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(userDevice))
            .findFirst()
            .orElse(-1);
    config.getDevices().get(devIdx).setPlatform("iOS");
    config.getDevices().get(devIdx).setAppium("local");
    config.getAppium().get(0).setUrl("http://127.0.0.1:1234");

    // must fail because no local appium should be running
    assertThrows(
        SessionNotCreatedException.class, () -> AppiumDriverFactory.forUser("Alice", config));
  }

  @Test
  void shouldThrowOnConnectToAppiumWithAndroid() {
    val devices = config.getDevices();
    val userDevice = config.getAppUserByName("Alice").getDevice();
    val devIdx =
        IntStream.range(0, config.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(userDevice))
            .findFirst()
            .orElse(-1);
    config.getDevices().get(devIdx).setPlatform("Android");
    config.getDevices().get(devIdx).setAppium("local");
    config.getAppium().get(0).setUrl("http://127.0.0.1:1234");

    // must fail because no local appium should be running
    assertThrows(
        SessionNotCreatedException.class, () -> AppiumDriverFactory.forUser("Alice", config));
  }
}
