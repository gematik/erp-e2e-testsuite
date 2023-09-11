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

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.dto.BaseConfigurationWrapper;
import de.gematik.test.erezept.config.dto.app.*;
import de.gematik.test.erezept.config.exceptions.ActorConfigurationNotFoundException;
import de.gematik.test.erezept.exceptions.ConfigurationMappingException;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErpAppConfiguration implements BaseConfigurationWrapper {

  @Delegate private final ErpAppConfigurationBase dto;

  public ErpActorConfiguration getAppUserByName(String name) {
    return this.getUsers().stream()
        .filter(actor -> actor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new ActorConfigurationNotFoundException(
                    name, this.getUsers().stream().map(ErpActorConfiguration::getName).toList()));
  }

  public DeviceConfiguration getDeviceByName(String deviceName) {
    return this.getDevices().stream()
        .filter(device -> device.getName().equalsIgnoreCase(deviceName))
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    deviceName,
                    this.getDevices().stream().map(DeviceConfiguration::getName).toList()));
  }

  public AppConfiguration getAppConfiguration(PlatformType platformType) {
    return this.getApps().stream()
        .filter(app -> platformType.is(app.getPlatform()))
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    platformType.name(),
                    this.getApps().stream().map(AppConfiguration::getPlatform).toList()));
  }

  public AppConfiguration getAppConfigurationForUser(@NonNull String username) {
    val userDeviceName = this.getAppUserByName(username).getDevice();
    val device = this.getDeviceByName(userDeviceName);
    return getAppConfiguration(PlatformType.fromString(device.getPlatform()));
  }

  public AppiumConfiguration getAppiumConfiguration(@NonNull String name) {
    return this.getAppium().stream()
        .filter(a -> a.getId().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(
            () ->
                new ConfigurationMappingException(
                    name, this.getAppium().stream().map(AppiumConfiguration::getId).toList()));
  }

  public static ErpAppConfiguration fromDto(ErpAppConfigurationBase dto) {
    return new ErpAppConfiguration(dto);
  }
}
