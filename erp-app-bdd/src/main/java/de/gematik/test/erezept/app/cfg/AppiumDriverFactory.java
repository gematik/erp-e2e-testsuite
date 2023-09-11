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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.thucydides.core.webdriver.ThucydidesWebDriverSupport;

@Slf4j
public class AppiumDriverFactory extends ThucydidesWebDriverSupport {

  private AppiumDriverFactory() {
    throw new AssertionError("Do not instantiate");
  }

  @SneakyThrows
  @SuppressWarnings({"unchecked"})
  public static <T extends AppiumDriver> UseTheApp<T> forUser(
      String scenarioName, String userName, ErpAppConfiguration config) {

    val userConfig = config.getAppUserByName(userName);
    val userDeviceConfig = config.getDeviceByName(userConfig.getDevice());
    val platform = PlatformType.fromString(userDeviceConfig.getPlatform());
    val appConfig = config.getAppConfiguration(platform);
    val appiumConfig = config.getAppiumConfiguration(userDeviceConfig.getAppium());

    val capsBuilder =
        DesiredCapabilitiesBuilder.initForScenario(scenarioName)
            .app(appConfig)
            .device(userDeviceConfig)
            .appium(appiumConfig);
    val caps = capsBuilder.create();

    if (config.isShouldLogCapabilityStatement()) {
      val json = capsBuilder.asJson();
      log.info(
          format(
              "DesiredCapabilities for {0} ({1})\n{2}",
              userDeviceConfig.getName(), userDeviceConfig.getPlatform(), json));
    }

    log.info(
        format("Create AppiumDriver for Platform {0} at {1}", platform, appiumConfig.getUrl()));
    if (platform == PlatformType.ANDROID) {
      val driver = new AndroidDriver(new URL(appiumConfig.getUrl()), caps);
      useDriver(driver);
      driver.setSetting("driver", "compose");
      return (UseTheApp<T>) new UseAndroidApp(driver, appiumConfig);
    } else if (platform == PlatformType.IOS) {
      val driver = new IOSDriver(new URL(appiumConfig.getUrl()), caps);
      useDriver(driver);
      return (UseTheApp<T>) new UseIOSApp(driver, appiumConfig);
    } else {
      throw new UnsupportedPlatformException(platform);
    }
  }
}
