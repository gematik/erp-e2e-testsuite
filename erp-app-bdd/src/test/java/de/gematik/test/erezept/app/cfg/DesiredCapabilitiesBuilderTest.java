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

import de.gematik.test.erezept.app.exceptions.MissingRequiredConfigurationException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.val;
import org.junit.jupiter.api.Test;

class DesiredCapabilitiesBuilderTest {

  @Test
  void shouldCreateIOSAppCapabilities() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("iOS");
    config.setPackageName("de.gematik.erezept");

    val dc = DesiredCapabilitiesBuilder.init().app(config).create();

    val appName = dc.getCapability("gematik:applicationName");
    assertEquals("E-Rezept", appName);

    val packageName = dc.getCapability("appium:appPackage");
    assertEquals("de.gematik.erezept", packageName);
  }

  @Test
  void shouldCreateAndroidAppCapabilities() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    config.setEspressoServerUniqueName("initialOrchestratorTestAPK");

    val dc = DesiredCapabilitiesBuilder.init().app(config).create();

    val appName = dc.getCapability("gematik:applicationName");
    assertEquals("E-Rezept", appName);

    val packageName = dc.getCapability("appium:appPackage");
    assertNull(packageName);
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

    val platformVersion = dc.getCapability("platformVersion");
    assertEquals("16", platformVersion);

    val automationName = dc.getCapability(MobileCapabilityType.AUTOMATION_NAME);
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

    val platformVersion = dc.getCapability("platformVersion");
    assertEquals("10", platformVersion);

    val espressoBuildConfig = dc.getCapability("appium:espressoBuildConfig");
    assertNotNull(espressoBuildConfig);

    val automationName = dc.getCapability(MobileCapabilityType.AUTOMATION_NAME);
    assertEquals("espresso", automationName);
  }

  @Test
  void shouldCreateAndroidAppiumCapabilitiesWithAccessKey() {
    val config = new AppiumConfiguration();
    config.setAccessKey("123");

    val dc = DesiredCapabilitiesBuilder.init().appium(config).create();
    assertEquals("123", dc.getCapability("accessKey"));
  }

  @Test
  void shouldCreateAndroidAppiumCapabilitiesWithAppiumVersion() {
    val config = new AppiumConfiguration();
    config.setVersion("1.2.3");

    val dc = DesiredCapabilitiesBuilder.init().appium(config).create();
    assertEquals("1.2.3", dc.getCapability("appium:appiumVersion"));
  }

  @Test
  void shouldThrowOnUnsupportedPlatform() {
    val config = new DeviceConfiguration();
    config.setName("AdV");
    config.setPlatform("Desktop");
    config.setUdid("123");
    config.setPlatformVersion("10");

    val builder = DesiredCapabilitiesBuilder.init();
    assertThrows(UnsupportedPlatformException.class, () -> builder.device(config));
  }

  @Test
  void shouldThrowOnMissingRequiredConfiguration() {
    val config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setPackageName("de.gematik.erezept");
    //    config.setEspressoServerUniqueName("initialOrchestratorTestAPK"); // this one is required!

    val builder = DesiredCapabilitiesBuilder.init();
    assertThrows(MissingRequiredConfigurationException.class, () -> builder.app(config));
  }

  @Test
  void shouldCreateAsJson() {
    val dc = DesiredCapabilitiesBuilder.init();
    assertDoesNotThrow(dc::asJson);
  }
}
