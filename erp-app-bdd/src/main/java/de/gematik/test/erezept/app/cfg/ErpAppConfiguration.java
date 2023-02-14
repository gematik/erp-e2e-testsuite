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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.app.exceptions.ActorConfigurationNotFoundException;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@Slf4j
public class ErpAppConfiguration {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  private static final String PRODUCT_NAME = "erp-app";
  private static final String CONFIG_YAML = "config.yaml";
  private static final Path BASE_PATH = Path.of("config", PRODUCT_NAME, CONFIG_YAML);

  private static ErpAppConfiguration instance;

  private boolean shouldLogCapabilityStatement = false;
  private List<AppConfiguration> apps;
  private List<ErpActorConfiguration> users;
  private List<DeviceConfiguration> devices;
  private List<AppiumConfiguration> appium;

  public ErpActorConfiguration getAppUserByName(String name) {
    return users.stream()
        .filter(actor -> actor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new ActorConfigurationNotFoundException(
                    name,
                    users.stream()
                        .map(ErpActorConfiguration::getName)
                        .collect(Collectors.toList())));
  }

  public DeviceConfiguration getDeviceByName(String deviceName) {
    return devices.stream()
        .filter(device -> device.getName().equalsIgnoreCase(deviceName))
        .findFirst()
        .orElseThrow(
            () ->
                new ActorConfigurationNotFoundException(
                    deviceName,
                    devices.stream()
                        .map(DeviceConfiguration::getName)
                        .collect(Collectors.toList())));
  }

  public AppConfiguration getAppConfiguration(PlatformType platformType) {
    return this.apps.stream()
        .filter(app -> app.getPlatformType() == platformType)
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    platformType.name(),
                    apps.stream().map(AppConfiguration::getPlatform).collect(Collectors.toList())));
  }

  public AppConfiguration getAppConfigurationForUser(@NonNull String username) {
    val userDeviceName = this.getAppUserByName(username).getDevice();
    val device = this.getDeviceByName(userDeviceName);
    return getAppConfiguration(device.getPlatformType());
  }

  public AppiumConfiguration getAppiumConfiguration(@NonNull String name) {
    return appium.stream()
        .filter(a -> a.getId().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    name,
                    appium.stream().map(AppiumConfiguration::getId).collect(Collectors.toList())));
  }

  public static ErpAppConfiguration getInstance() {
    if (instance == null) {
      val ymlFile =
          BASE_PATH.toFile().exists()
              ? BASE_PATH.toFile()
              : Path.of("..").resolve(BASE_PATH).toFile();

      instance = getInstance(ymlFile);
    }

    return instance;
  }

  @SneakyThrows
  public static ErpAppConfiguration getInstance(File ymlFile) {
    return OBJECT_MAPPER.readValue(ymlFile, ErpAppConfiguration.class);
  }
}
