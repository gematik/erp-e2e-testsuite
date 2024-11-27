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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import java.net.URL;
import java.time.Duration;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.http.ClientConfig;

@Slf4j
public class AppiumDriverFactory {

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

    if (!userDeviceConfig.isHasNfc() && !userConfig.isUseVirtualEgk()) {
      throw new ConfigurationException(
          format(
              "User {0} using {1} requires NFC for using a real eGK",
              userName, userDeviceConfig.getName()));
    }

    val capsBuilder =
        DesiredCapabilitiesBuilder.initForScenario(scenarioName)
            .app(appConfig, appiumConfig.getProvisioningProfilePostfix())
            .device(userDeviceConfig)
            .appium(appiumConfig);
    val caps = capsBuilder.create();

    if (config.shouldLogCapabilityStatement()) {
      val json = capsBuilder.asJson();
      log.info(
          format(
              "DesiredCapabilities for {0} ({1}) running on {2}\n{3}",
              userDeviceConfig.getName(),
              userDeviceConfig.getPlatform(),
              userDeviceConfig.getAppium(),
              json));
    }

    log.info(
        format("Create AppiumDriver for Platform {0} at {1}", platform, appiumConfig.getUrl()));
    if (platform == PlatformType.ANDROID) {
      val driver = new AndroidDriver(new URL(appiumConfig.getUrl()), caps);
      driver.setSetting("driver", "compose");
      return (UseTheApp<T>) new UseAndroidApp(driver, appiumConfig);
    } else if (platform == PlatformType.IOS) {
      val clientConfig =
          ClientConfig.defaultConfig()
              .baseUrl(new URL(appiumConfig.getUrl()))
              .connectionTimeout(Duration.ofMinutes(3))
              .readTimeout(Duration.ofMinutes(10))
              .withRetries();
      val driver = connectDriver(() -> new IOSDriver(clientConfig, caps));
      log.info("Driver connected for XCUITest");
      return (UseTheApp<T>) new UseIOSApp(driver, appiumConfig);
    } else {
      log.error(format("Given Platform {0} not yet supported", platform));
      throw new UnsupportedPlatformException(platform);
    }
  }

  private static <D> D connectDriver(Supplier<D> driverSupplier) {
    val maxRetries = Integer.parseInt(System.getProperty("appFailRetry", "1"));
    var currentTry = 0;
    Exception lastException;
    do {
      try {
        return driverSupplier.get();
      } catch (Exception e) {
        lastException = e;
        currentTry++;
        log.warn(
            "{} / {} Failed to connect driver with {}", currentTry, maxRetries, e.getMessage());
      }
    } while (currentTry < maxRetries);

    throw new SessionNotCreatedException(
        format("Failed to create driver session after {0} attempts", maxRetries), lastException);
  }
}
