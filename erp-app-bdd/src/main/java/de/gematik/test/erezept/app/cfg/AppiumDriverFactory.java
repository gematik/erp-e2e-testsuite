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

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.SessionNotCreatedException;
import org.openqa.selenium.remote.DesiredCapabilities;
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

    // build default
    val bundleIdCapability =
        appConfig.getPackageName() + appiumConfig.getProvisioningProfilePostfix();
    val appPathCapability =
        Optional.ofNullable(appConfig.getAppFile())
            .orElseGet(() -> format("cloud:{0}", appConfig.getPackageName()));
    val xcuiTestOptions =
        new XCUITestOptions()
            .setUdid(userDeviceConfig.getUdid())
            .setBundleId(bundleIdCapability)
            .setApp(appPathCapability)
            .setEnforceAppInstall(userDeviceConfig.isEnforceInstall())
            .setFullReset(userDeviceConfig.isFullReset())
            .setNoReset(false)
            .setAutoDismissAlerts(false) // could we set this to true?
            .setPlatformVersion(userDeviceConfig.getPlatformVersion());

    val vendorOptions = new HashMap<String, String>();
    vendorOptions.put("appiumVersion", appiumConfig.getVersion());
    vendorOptions.put("accessKey", appiumConfig.getAccessKey());
    vendorOptions.put("testName", scenarioName);

    xcuiTestOptions.setCapability("digitalai:options", vendorOptions);
    xcuiTestOptions.setCapability("language", "de");

    if (config.shouldLogCapabilityStatement()) {
      val json =
          new ObjectMapper()
              .writerWithDefaultPrettyPrinter()
              .writeValueAsString(xcuiTestOptions.toJson());
      log.info(
          "DesiredCapabilities for {} ({}) running on {}\n{}",
          userDeviceConfig.getName(),
          userDeviceConfig.getPlatform(),
          userDeviceConfig.getAppium(),
          json);
    }

    log.info("Create AppiumDriver for Platform {} at {}", platform, appiumConfig.getUrl());
    if (platform == PlatformType.ANDROID) {
      // TODO: not implemented yed to Android
      val caps = new DesiredCapabilities();
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
      val driver = connectDriver(() -> new IOSDriver(clientConfig, xcuiTestOptions));
      log.info("Driver connected for XCUITest");
      return (UseTheApp<T>) new UseIOSApp(driver, appiumConfig);
    } else {
      log.error("Given Platform {} not yet supported", platform);
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
        format(
            "Failed to create driver session after {0} attempts with {1}",
            maxRetries, lastException.getMessage()),
        lastException);
  }
}
