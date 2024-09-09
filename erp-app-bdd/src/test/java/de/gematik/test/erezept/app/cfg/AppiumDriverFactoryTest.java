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

package de.gematik.test.erezept.app.cfg;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockConstruction;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.app.AppConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpAppConfigurationBase;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.util.stream.IntStream;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.openqa.selenium.SessionNotCreatedException;

class AppiumDriverFactoryTest {

  private ErpAppConfigurationBase configDto;
  private ErpAppConfiguration config;

  @BeforeEach
  void setUp() {
    configDto = ConfigurationReader.forAppConfiguration().create();
    config = ErpAppConfiguration.fromDto(configDto);
  }

  @Test
  void shouldThrowOnUnsupportedPlatform() {
    val devices = configDto.getDevices();
    val aliceDevice = config.getAppUserByName("Alice").getDevice();
    val device =
        IntStream.range(0, configDto.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(aliceDevice))
            .mapToObj(i -> configDto.getDevices().get(i))
            .findFirst()
            .orElseThrow();
    device.setPlatform("Desktop");

    val desktopConfig = new AppConfiguration();
    desktopConfig.setPlatform("Desktop");
    configDto.getApps().add(desktopConfig);

    assertThrows(
        UnsupportedPlatformException.class,
        () -> AppiumDriverFactory.forUser("scenario", "Alice", config));
  }

  @Test
  void shouldThrowOnConnectToAppiumWithIOS() {
    val devices = configDto.getDevices();
    val aliceDevice = config.getAppUserByName("Alice").getDevice();
    val device =
        IntStream.range(0, configDto.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(aliceDevice))
            .mapToObj(i -> configDto.getDevices().get(i))
            .findFirst()
            .orElseThrow();
    device.setPlatform("iOS");
    device.setAppium("local");
    configDto.getAppium().get(0).setUrl("http://127.0.0.1:1234");
    configDto.getAppium().get(0).setAccessKey("");

    // must fail because no local appium should be running
    assertThrows(
        SessionNotCreatedException.class,
        () -> AppiumDriverFactory.forUser("scenario", "Alice", config));
  }

  @Test
  void shouldConnectToAppiumWithIOS() {
    try (val iosDriver = mockConstruction(IOSDriver.class, (mock, context) -> {})) {

      val devices = configDto.getDevices();
      val aliceDevice = config.getAppUserByName("Alice").getDevice();
      val device =
          IntStream.range(0, configDto.getDevices().size())
              .filter(i -> devices.get(i).getName().equals(aliceDevice))
              .mapToObj(i -> configDto.getDevices().get(i))
              .findFirst()
              .orElseThrow();
      device.setPlatform("iOS");
      device.setAppium("local");
      configDto.getAppium().get(0).setUrl("http://127.0.0.1:1234");
      configDto.getAppium().get(0).setAccessKey("");

      val driver = AppiumDriverFactory.forUser("scenario", "Alice", config);
      assertNotNull(driver);
    }
  }

  @ParameterizedTest
  @CsvSource(value = {"true, true", "true, false", "false, true"})
  void shouldThrowOnConnectToAppiumWithAndroid(boolean hasNfc, boolean useVEgk) {
    val devices = configDto.getDevices();
    val user = config.getAppUserByName("Alice");
    val aliceDevice = user.getDevice();
    val device =
        IntStream.range(0, configDto.getDevices().size())
            .filter(i -> devices.get(i).getName().equals(aliceDevice))
            .mapToObj(i -> configDto.getDevices().get(i))
            .findFirst()
            .orElseThrow();
    device.setPlatform("Android");
    device.setAppium("local");

    // ensure no appium is accidentally running
    configDto.getAppium().get(0).setUrl("http://127.0.0.1:1234");

    // ensure path coverage for correct configurations
    device.setHasNfc(hasNfc);
    user.setUseVirtualEgk(useVEgk);
    configDto.setShouldLogCapabilityStatement(useVEgk); // just for coverage!

    // must fail because no local appium should be running
    assertThrows(
        SessionNotCreatedException.class,
        () -> AppiumDriverFactory.forUser("scenario", "Alice", config));
  }

  @Test
  void shouldConnectToAppiumWithAndroid() {
    try (val iosDriver = mockConstruction(AndroidDriver.class, (mock, context) -> {})) {

      val devices = configDto.getDevices();
      val aliceDevice = config.getAppUserByName("Alice").getDevice();
      val device =
          IntStream.range(0, configDto.getDevices().size())
              .filter(i -> devices.get(i).getName().equals(aliceDevice))
              .mapToObj(i -> configDto.getDevices().get(i))
              .findFirst()
              .orElseThrow();
      device.setPlatform("Android");
      device.setAppium("local");
      configDto.getAppium().get(0).setUrl("http://127.0.0.1:1234");
      configDto.getAppium().get(0).setAccessKey("");

      val driver = AppiumDriverFactory.forUser("scenario", "Alice", config);
      assertNotNull(driver);
    }
  }

  @Test
  void shouldThrowOnUsingRealEgkWithoutNfcCapability() {
    val nonNfcDevice =
        configDto.getDevices().stream().filter(d -> !d.isHasNfc()).findFirst().orElseThrow();
    val user = configDto.getUsers().get(0);
    user.setUseVirtualEgk(false);
    user.setDevice(nonNfcDevice.getName());

    // must fail because real eGK is used but the device does not support NFC
    val userName = user.getName();
    assertThrows(
        ConfigurationException.class,
        () -> AppiumDriverFactory.forUser("scenario", userName, config));
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(AppiumDriverFactory.class));
  }
}
