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

import de.gematik.test.erezept.config.dto.app.AppConfiguration;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import de.gematik.test.erezept.config.dto.app.DeviceConfiguration;
import de.gematik.test.erezept.config.exceptions.MissingRequiredConfigurationException;
import io.appium.java_client.remote.AutomationName;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EmptySource;
import org.junit.jupiter.params.provider.NullSource;

class DesiredCapabilitiesBuilderTest {

  @Test
  void shouldCreateIOSAppCapabilities() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("iOS");
    config.setPackageName("de.gematik.erezept");

    val dc = DesiredCapabilitiesBuilder.init().app(config, null).create();

    val bundleId = dc.getCapability("bundleId");
    assertEquals("de.gematik.erezept", bundleId);
  }

  @Test
  void shouldAppendMdcPostfixForNfc() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("iOS");
    config.setPackageName("de.gematik.erezept");

    val postfix = ".mdc2";
    val dc = DesiredCapabilitiesBuilder.init().app(config, postfix).create();

    val bundleId = dc.getCapability("bundleId");
    assertEquals("de.gematik.erezept" + postfix, bundleId);
  }

  @Test
  void shouldCreateAndroidAppCapabilities() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setEspressoServerUniqueName("initialOrchestratorTestAPK");

    val dc = DesiredCapabilitiesBuilder.init().app(config, null).create();

    val bundleId = dc.getCapability("bundleId");
    assertNull(bundleId);
  }

  @Test
  void shouldCreateIOSDeviceCapabilities() {
    val config = new DeviceConfiguration();
    config.setName("iPhone XY");
    config.setPlatform("iOS");
    config.setUdid("123");
    config.setPlatformVersion("16");

    val dc = DesiredCapabilitiesBuilder.init().device(config).create();

    val platform = dc.getCapability("platformName");
    assertEquals("IOS", platform.toString());

    val automationName = dc.getCapability("automationName");
    assertEquals(AutomationName.IOS_XCUI_TEST, automationName);
  }

  @Test
  void shouldCreateAndroidDeviceCapabilities() {
    val config = new DeviceConfiguration();
    config.setName("Samsung Galaxy");
    config.setPlatform("Android");
    config.setUdid("123");
    config.setPlatformVersion("10");

    val dc = DesiredCapabilitiesBuilder.init().device(config).create();

    val platform = dc.getCapability("platformName");
    assertEquals("ANDROID", platform.toString());

    val espressoBuildConfig = dc.getCapability("appium:espressoBuildConfig");
    assertNotNull(espressoBuildConfig);

    val automationName = dc.getCapability("automationName");
    assertEquals("Espresso", automationName);
  }

  @Test
  void shouldCreateAndroidAppiumCapabilitiesWithAccessKey() {
    val config = new AppiumConfiguration();
    config.setAccessKey("123");

    val dc = DesiredCapabilitiesBuilder.init().appium(config).create();
    assertEquals("123", dc.getCapability("accessKey"));
  }

  @ParameterizedTest
  @NullSource
  @EmptySource
  void shouldCreateAndroidAppiumCapabilitiesWithoutAccessKey(String emptyAccessKey) {
    val config = new AppiumConfiguration();
    config.setAccessKey(emptyAccessKey);

    val dc = DesiredCapabilitiesBuilder.init().appium(config).create();
    assertNull(dc.getCapability("accessKey"));
  }

  @Test
  void shouldCreateAndroidAppiumCapabilitiesWithAppiumVersion() {
    val config = new AppiumConfiguration();
    config.setVersion("1.2.3");

    val dc = DesiredCapabilitiesBuilder.init().appium(config).create();
    assertEquals("1.2.3", dc.getCapability("appiumVersion"));
  }

  @Test
  void shouldThrowOnMissingRequiredConfiguration() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    //    config.setEspressoServerUniqueName("initialOrchestratorTestAPK"); // this one is required!

    val builder = DesiredCapabilitiesBuilder.init();
    assertThrows(MissingRequiredConfigurationException.class, () -> builder.app(config, null));
  }

  @Test
  void shouldCreateAsJson() {
    val dc = DesiredCapabilitiesBuilder.init();
    assertDoesNotThrow(dc::asJson);
  }
}
