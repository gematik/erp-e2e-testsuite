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

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.remote.DesiredCapabilities;

@Slf4j
public class AppiumDriverFactory {

  private AppiumDriverFactory() {
    throw new AssertionError("Do not instantiate");
  }

  public static UseTheApp forUser(String userName) {
    val config = ErpAppConfiguration.getInstance();
    val userConfig = config.getAppUserByName(userName);
    val userDeviceConfig = config.getDeviceByName(userConfig.getDevice());
    val appConfig = config.getAppConfiguration(userDeviceConfig.getPlatformType());
    val appiumConfig = config.getAppiumConfiguration(userDeviceConfig.getAppium());

    val caps = initCommonCapabilities();
    addAppCapabilities(caps, appConfig);
    addDeviceCapabilities(caps, userDeviceConfig);

    if (config.isShouldLogCapabilityStatement()) {
      logCaps(caps, userDeviceConfig);
    }

    val driver = createDriver(caps, appiumConfig, userDeviceConfig.getPlatformType());

    return UseTheApp.with(driver, userDeviceConfig.getPlatformType(), appConfig);
  }

  private static AppiumDriver createDriver(
      DesiredCapabilities caps, AppiumConfiguration config, PlatformType platform) {
    AppiumDriver driver;
    log.info(format("Create AppiumDriver for Platform {0} at {1}", platform, config.getUrl()));
    if (platform == PlatformType.ANDROID) {
      driver = new AndroidDriver(config.getUrl(), caps);
      driver.setSetting("driver", "compose");
    } else if (platform == PlatformType.IOS) {
      driver = new IOSDriver(config.getUrl(), caps);
    } else {
      throw new UnsupportedPlatformException(platform);
    }
    return driver;
  }

  @SneakyThrows
  private static void logCaps(DesiredCapabilities caps, DeviceConfiguration config) {
    val mapper = new ObjectMapper();
    val objectNode = mapper.createObjectNode();

    caps.getCapabilityNames()
        .forEach(name -> objectNode.put(name, caps.getCapability(name).toString()));
    String json = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
    log.info(
        format(
            "DesiredCapabilities for {0} ({1})\n{2}",
            config.getName(), config.getPlatformType(), json));
  }

  private static DesiredCapabilities initCommonCapabilities() {
    val caps = new DesiredCapabilities();
    caps.setCapability("gematik:session-override", true);
    caps.setCapability("appium:noReset", false);
    caps.setCapability("appium:fullReset", true);
    caps.setCapability("gematik:instrumentApp", true);

    return caps;
  }

  private static void addAppCapabilities(DesiredCapabilities caps, AppConfiguration config) {
    caps.setCapability("gematik:applicationName", "E-Rezept");
    caps.setCapability("app", config.getAppFile());
    caps.setCapability("newCommandTimeout", 60 * 5);

    if (config.getPlatformType() == PlatformType.IOS) {
      // not required for Android
      caps.setCapability("appium:appPackage", config.getPackageName());
    }
  }

  private static void addDeviceCapabilities(DesiredCapabilities caps, DeviceConfiguration config) {
    caps.setCapability(MobileCapabilityType.DEVICE_NAME, config.getName());
    caps.setCapability(MobileCapabilityType.UDID, config.getUdid());
    caps.setCapability("platformVersion", config.getPlatformVersion());
    caps.setCapability("appium:enforceAppInstall", true); // TODO: make configurable

    if (config.getPlatformType() == PlatformType.ANDROID) {
      caps.setCapability("platformName", "Android");
      caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "espresso");
      caps.setCapability("showGradleLog", true); // will show the log of gradle in Appium
      caps.setCapability("appium:forceEspressoRebuild", false); // TODO: make configurable
      caps.setCapability("appium:espressoBuildConfig", createEspressoBuildConfig());
    } else if (config.getPlatformType() == PlatformType.IOS) {
      caps.setCapability("platformName", "iOS");
      caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
    } else {
      log.error(format("Given Platform {0} not yet supported", config.getPlatform()));
      throw new UnsupportedPlatformException(config.getPlatformType());
    }
  }

  /**
   * This method should make sure the Android App is instrumented with the correct gradle settings.
   * However, this needs be evaluated and ensured to be necessary and correct
   *
   * <p>https://github.com/appium/appium-espresso-driver#espresso-build-config
   */
  @SneakyThrows
  private static String createEspressoBuildConfig() {
    val mapper = new ObjectMapper();
    val espressoBuildConfig = mapper.createObjectNode();
    espressoBuildConfig.put("gradle", "7.0.2");
    espressoBuildConfig.put("kotlin", "1.6.10");
    espressoBuildConfig.put("compose", "1.1.0-rc01");

    val additionals = mapper.createArrayNode();
    additionals
        .add("androidx.lifecycle:lifecycle-extensions:2.2.0")
        .add("androidx.activity:activity:1.3.1")
        .add("androidx.fragment:fragment:1.3.4");

    espressoBuildConfig.putIfAbsent("additionalAndroidTestDependencies", additionals);

    return mapper.writeValueAsString(espressoBuildConfig);
  }
}
