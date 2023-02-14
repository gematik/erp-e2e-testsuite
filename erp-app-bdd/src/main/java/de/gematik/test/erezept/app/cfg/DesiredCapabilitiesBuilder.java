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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.app.exceptions.MissingRequiredConfigurationException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import io.appium.java_client.remote.AutomationName;
import io.appium.java_client.remote.MobileCapabilityType;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.remote.DesiredCapabilities;

@Slf4j
public class DesiredCapabilitiesBuilder {

  private final DesiredCapabilities caps;

  private DesiredCapabilitiesBuilder() {
    this.caps = new DesiredCapabilities();
    caps.setCapability("gematik:session-override", true);
    caps.setCapability("appium:noReset", false);
    caps.setCapability("appium:fullReset", true);
    caps.setCapability("gematik:instrumentApp", true);
  }

  public DesiredCapabilitiesBuilder app(AppConfiguration config) {
    caps.setCapability("gematik:applicationName", "E-Rezept");
    caps.setCapability("app", config.getAppFile());
    caps.setCapability("newCommandTimeout", 60 * 5);

    if (config.getPlatformType() == PlatformType.IOS) {
      // not required for Android
      caps.setCapability("appium:appPackage", config.getPackageName());
    }
    if (config.getPlatformType() == PlatformType.ANDROID) {
      // not required for iOS
      if (config.getEspressoServerUniqueName() == null) {
        throw new MissingRequiredConfigurationException(
            "If Android is selected as platform, it is necessary to provide the espressoServerUniqueName capability.");
      }
      caps.setCapability("espressoServerUniqueName", config.getEspressoServerUniqueName());
    }
    return this;
  }

  public DesiredCapabilitiesBuilder device(DeviceConfiguration config) {
    caps.setCapability(MobileCapabilityType.DEVICE_NAME, config.getName());
    caps.setCapability(MobileCapabilityType.UDID, config.getUdid());
    caps.setCapability("platformVersion", config.getPlatformVersion());
    caps.setCapability("appium:enforceAppInstall", config.isEnforceInstall());

    if (config.getPlatformType() == PlatformType.ANDROID) {
      caps.setCapability("platformName", "Android");
      caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, "espresso");
      caps.setCapability("showGradleLog", true); // will show the log of gradle in Appium
      caps.setCapability("appium:forceEspressoRebuild", false);
      caps.setCapability("appium:espressoBuildConfig", createEspressoBuildConfig());
    } else if (config.getPlatformType() == PlatformType.IOS) {
      caps.setCapability("platformName", "iOS");
      caps.setCapability(MobileCapabilityType.AUTOMATION_NAME, AutomationName.IOS_XCUI_TEST);
    } else {
      log.error(format("Given Platform {0} not yet supported", config.getPlatform()));
      throw new UnsupportedPlatformException(config.getPlatformType());
    }

    return this;
  }

  public DesiredCapabilitiesBuilder appium(AppiumConfiguration appiumConfiguration) {
    if (appiumConfiguration.getAccessKey() != null
        && !appiumConfiguration.getAccessKey().isEmpty()) {
      caps.setCapability("accessKey", appiumConfiguration.getAccessKey());
    }
    if (appiumConfiguration.getVersion() != null) {
      caps.setCapability("appium:appiumVersion", appiumConfiguration.getVersion());
    }
    return this;
  }

  @SneakyThrows
  public String asJson() {
    val mapper = new ObjectMapper();
    val objectNode = mapper.createObjectNode();

    caps.getCapabilityNames()
        .forEach(name -> objectNode.put(name, caps.getCapability(name).toString()));
    return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(objectNode);
  }

  public DesiredCapabilities create() {
    return caps;
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

  /*
    TODO - the jetpack compose dependencies
    {
    "toolsVersions": {
      "compileSdk": 32,
      "composeVersion": "1.2.0-beta03"
    },
    "additionalAndroidAppDependencies": [
      "androidx.compose.runtime:runtime:1.1.0",
      "androidx.compose.ui:ui-tooling:1.1.0",
      "androidx.compose.ui:ui-tooling:1.1.0",
      "io.realm.kotlin:library-base:0.10.2",
      "androidx.compose.foundation:foundation:1.1.0",
      "androidx.collection:collection:1.1.0",
      "androidx.startup:startup-runtime:1.1.0",
      "androidx.compose.material:material:1.1.0"
    ],
    "additionalAndroidTestDependencies": [
      "androidx.compose.runtime:runtime:1.1.0",
      "androidx.compose.ui:ui-tooling:1.1.0",
      "androidx.compose.foundation:foundation:1.1.0",
      "androidx.startup:startup-runtime:1.1.0",
      "io.realm.kotlin:library-base:0.10.2",
      "androidx.collection:collection:1.1.0",
      "androidx.compose.material:material:1.1.0"
    ]
  }

     */

  public static DesiredCapabilitiesBuilder init() {
    return new DesiredCapabilitiesBuilder();
  }
}
