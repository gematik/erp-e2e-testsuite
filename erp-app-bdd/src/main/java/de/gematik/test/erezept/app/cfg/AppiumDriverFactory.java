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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.cfg;

import static de.gematik.test.erezept.app.mobile.PlatformType.ANDROID;
import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.dto.app.AppConfiguration;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import de.gematik.test.erezept.config.dto.app.DeviceConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpActorConfiguration;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import java.net.URL;
import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.http.ClientConfig;

@Slf4j
@UtilityClass
public class AppiumDriverFactory {

  private static AppiumDriver appiumDriver;

  @SneakyThrows
  public static <T extends AppiumDriver> UseTheApp<T> forUser(
      String userName, ErpAppConfiguration config) {

    val userConfig = config.getAppUserByName(userName);
    val userDeviceConfig = config.getDeviceByName(userConfig.getDevice());
    val platform = PlatformType.fromString(userDeviceConfig.getPlatform());
    val appConfig = config.getAppConfiguration(platform);
    val appiumConfig = config.getAppiumConfiguration(userDeviceConfig.getAppium());

    if (appiumDriver != null) {
      log.info("Resuing existing Appium Driver");
      return createDriverAbility(appiumConfig, platform);
    }

    validateNfcUsage(userName, userConfig, userDeviceConfig);

    appiumDriver = createDriver(appiumConfig, appConfig, userDeviceConfig, config, platform);

    return createDriverAbility(appiumConfig, platform);
  }

  @SuppressWarnings({"unchecked"})
  private static <T extends AppiumDriver> UseTheApp<T> createDriverAbility(
      AppiumConfiguration appiumConfig, PlatformType platform) {
    log.info("Creating Driver Ability for platform={} at {}", platform, appiumConfig.getUrl());

    UseTheApp<?> driverAbility;

    if (platform.equals(ANDROID)) {
      driverAbility = new UseAndroidApp((AndroidDriver) appiumDriver, appiumConfig);
    } else {
      driverAbility = new UseIOSApp((IOSDriver) appiumDriver, appiumConfig);
    }

    return (UseTheApp<T>) driverAbility;
  }

  static AppiumDriver createDriver(
      AppiumConfiguration appiumConfig,
      AppConfiguration appConfig,
      DeviceConfiguration userDeviceConfig,
      ErpAppConfiguration config,
      PlatformType platform) {

    log.info("Creating Appium Driver for platform={} at {}", platform, appiumConfig.getUrl());

    val specificDriver =
        switch (platform) {
          case ANDROID -> createAndroidDriver(appiumConfig);
          case IOS -> createIOSDriver(appiumConfig, appConfig, userDeviceConfig, config);
          default -> throw new UnsupportedPlatformException(
              format("Given Platform {0} not yet supported", platform));
        };

    return connectDriver(() -> specificDriver);
  }

  @SneakyThrows
  private static AppiumDriver createAndroidDriver(AppiumConfiguration appiumConfig) {
    // TODO: not implemented yet for Android
    val caps = new DesiredCapabilities();

    val androidDriver = new AndroidDriver(new URL(appiumConfig.getUrl()), caps);
    androidDriver.setSetting("driver", "compose");

    return androidDriver;
  }

  @SneakyThrows
  private static AppiumDriver createIOSDriver(
      AppiumConfiguration appiumConfig,
      AppConfiguration appConfig,
      DeviceConfiguration userDeviceConfig,
      ErpAppConfiguration config) {
    val bundleId = appConfig.getPackageName() + appiumConfig.getProvisioningProfilePostfix();
    val appPath =
        Optional.ofNullable(appConfig.getAppFile())
            .orElseGet(() -> format("cloud:{0}", appConfig.getPackageName()));

    val xcuiTestOptions =
        new XCUITestOptions()
            .setUdid(userDeviceConfig.getUdid())
            .setBundleId(bundleId)
            .setApp(appPath)
            .setEnforceAppInstall(userDeviceConfig.isEnforceInstall())
            .setFullReset(userDeviceConfig.isFullReset())
            .setNoReset(false)
            .setAutoDismissAlerts(false) // could we set this to true?
            .setPlatformVersion(userDeviceConfig.getPlatformVersion())
            .setLanguage("de")
            .setNewCommandTimeout(Duration.ofMinutes(10));

    if (config.shouldLogCapabilityStatement()) {
      logCapabilities(appiumConfig, userDeviceConfig, xcuiTestOptions);
    }

    val clientConfig =
        ClientConfig.defaultConfig()
            .baseUrl(new URL(appiumConfig.getUrl()))
            .connectionTimeout(Duration.ofMinutes(3))
            .readTimeout(Duration.ofMinutes(10))
            .withRetries();

    return new IOSDriver(clientConfig, xcuiTestOptions);
  }

  @SneakyThrows
  private static void logCapabilities(
      AppiumConfiguration appiumConfig,
      DeviceConfiguration userDeviceConfig,
      XCUITestOptions xcuiTestOptions) {
    String json =
        new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValueAsString(xcuiTestOptions.toJson());
    log.info(
        """
        DesiredCapabilities for {} ({}) running on {}
        {}
        """,
        userDeviceConfig.getName(),
        userDeviceConfig.getPlatform(),
        appiumConfig.getUrl(),
        json);
  }

  private static void validateNfcUsage(
      String userName, ErpActorConfiguration userConfig, DeviceConfiguration userDeviceConfig) {
    if (!userDeviceConfig.isHasNfc() && !userConfig.isUseVirtualEgk()) {
      throw new ConfigurationException(
          format(
              "User {0} using {1} requires NFC for using a real eGK",
              userName, userDeviceConfig.getName()));
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
        format(
            "Failed to create driver session after {0} attempts with {1}",
            maxRetries, lastException.getMessage()),
        lastException);
  }

  public static void closeDriver() {
    if (appiumDriver == null) {
      log.info("No Appium Driver found");
      return;
    }

    appiumDriver.quit();
    appiumDriver = null;
    log.info("Quit Appium Driver");
  }
}
