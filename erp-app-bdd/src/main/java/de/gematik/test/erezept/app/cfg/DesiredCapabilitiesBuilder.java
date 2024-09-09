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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.dto.app.AppConfiguration;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import de.gematik.test.erezept.config.dto.app.DeviceConfiguration;
import de.gematik.test.erezept.config.exceptions.MissingRequiredConfigurationException;
import io.appium.java_client.remote.AutomationName;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.thucydides.core.webdriver.MobilePlatform;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;

@Slf4j
public class DesiredCapabilitiesBuilder {

  private final DesiredCapabilities caps;

  private DesiredCapabilitiesBuilder(@Nullable String scenarioName) {
    this.caps = new DesiredCapabilities();
    if (scenarioName != null) caps.setCapability("testName", scenarioName);
  }

  /**
   * This method should make sure the Android App is instrumented with the correct Gradle settings.
   * However, this needs to be evaluated and ensured to be necessary and correct
   *
   * <p><a href="https://github.com/appium/appium-espresso-driver#espresso-build-config">Espresso
   * build config</a>
   */
  @SneakyThrows
  private static String createEspressoBuildConfig() {
    val mapper = new ObjectMapper();
    val espressoBuildConfig = mapper.createObjectNode();
    espressoBuildConfig.put("gradle", "8.1.1");
    espressoBuildConfig.put("kotlin", "1.8.10");
    espressoBuildConfig.put("compose", "1.5.0-beta02");

    val additionals = mapper.createArrayNode();
    additionals
        .add("androidx.lifecycle:lifecycle-extensions:2.2.0")
        .add("androidx.activity:activity:1.3.1")
        .add("androidx.fragment:fragment:1.3.4");

    espressoBuildConfig.putIfAbsent("additionalAndroidTestDependencies", additionals);

    return mapper.writeValueAsString(espressoBuildConfig);
  }

  public static DesiredCapabilitiesBuilder initForScenario(String scenarioName) {
    return new DesiredCapabilitiesBuilder(scenarioName);
  }

  public static DesiredCapabilitiesBuilder init() {
    return new DesiredCapabilitiesBuilder(null);
  }

  public DesiredCapabilitiesBuilder app(
      AppConfiguration config, @Nullable String provisioningProfilePostfix) {
    caps.setCapability("appium:newCommandTimeout", 60 * 5);

    if (config.getAppFile() == null) {
      // no app file given: use the packageName and let appium/MDC decide about the latest available
      // version
      caps.setCapability("app", format("cloud:{0}", config.getPackageName()));
    } else {
      // concrete appFile given in configuration, use this one instead!
      caps.setCapability("app", config.getAppFile());
    }

    if (PlatformType.IOS.is(config.getPlatform())) {
      // appending provisioning profile postfix is required to use the app with dedicated
      // provisioning profile for iOS NFC
      val bundleId =
          Optional.ofNullable(provisioningProfilePostfix)
              .map(postfix -> config.getPackageName() + postfix)
              .orElse(config.getPackageName());
      caps.setCapability("bundleId", bundleId);
    }

    if (PlatformType.ANDROID.is(config.getPlatform())) {
      // not required for iOS
      if (config.getEspressoServerUniqueName() == null) {
        throw new MissingRequiredConfigurationException(
            "If Android is selected as platform, it is necessary to provide the"
                + " espressoServerUniqueName capability.");
      }
      caps.setCapability("espressoServerUniqueName", config.getEspressoServerUniqueName());
    }
    return this;
  }

  public DesiredCapabilitiesBuilder device(DeviceConfiguration config) {
    caps.setCapability("udid", config.getUdid());
    caps.setCapability("enforceAppInstall", config.isEnforceInstall());
    caps.setCapability("fullReset", config.isFullReset());
    caps.setCapability("noReset", false);

    if (PlatformType.ANDROID.is(config.getPlatform())) {
      caps.setCapability(CapabilityType.PLATFORM_NAME, MobilePlatform.ANDROID);
      caps.setCapability("automationName", AutomationName.ESPRESSO);
      caps.setCapability("showGradleLog", true); // will show the log of gradle in Appium
      caps.setCapability("appium:forceEspressoRebuild", false);
      caps.setCapability("appium:espressoBuildConfig", createEspressoBuildConfig());
    } else if (PlatformType.IOS.is(config.getPlatform())) {
      caps.setCapability(CapabilityType.PLATFORM_NAME, MobilePlatform.IOS);
      caps.setCapability("automationName", AutomationName.IOS_XCUI_TEST);
      caps.setCapability("autoDissmissAlerts", true);
    }

    return this;
  }

  public DesiredCapabilitiesBuilder appium(AppiumConfiguration appiumConfiguration) {
    if (appiumConfiguration.getAccessKey() != null
        && !appiumConfiguration.getAccessKey().isEmpty()) {
      caps.setCapability("accessKey", appiumConfiguration.getAccessKey());
    }
    if (appiumConfiguration.getVersion() != null) {
      caps.setCapability("appiumVersion", appiumConfiguration.getVersion());
    }

    caps.setCapability("attachCrashLogToReport", true);
    return this;
  }

  @SneakyThrows
  public String asJson() {
    val mapper = new ObjectMapper();
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(caps.asMap());
  }

  public DesiredCapabilities create() {
    return caps;
  }
}
