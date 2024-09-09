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

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.app.*;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErpAppConfiguration extends ConfiguredFactory {

  private final ErpAppConfigurationBase dto;

  public boolean shouldLogCapabilityStatement() {
    return dto.isShouldLogCapabilityStatement();
  }

  public ErpActorConfiguration getAppUserByName(String name) {
    return this.getConfig(name, dto.getUsers());
  }

  public DeviceConfiguration getDeviceByName(String deviceName) {
    return this.getConfig(deviceName, dto.getDevices());
  }

  public AppConfiguration getAppConfiguration(PlatformType platformType) {
    return dto.getApps().stream()
        .filter(app -> platformType.is(app.getPlatform()))
        .findFirst()
        .orElseThrow(() -> new ConfigurationMappingException(platformType.name(), dto.getApps()));
  }

  public AppConfiguration getAppConfigurationForUser(String username) {
    val userDeviceName = this.getAppUserByName(username).getDevice();
    val device = this.getDeviceByName(userDeviceName);
    return getAppConfiguration(PlatformType.fromString(device.getPlatform()));
  }

  public AppiumConfiguration getAppiumConfiguration(String name) {
    return this.getConfig(name, dto.getAppium());
  }

  public static ErpAppConfiguration fromDto(ErpAppConfigurationBase dto) {
    return new ErpAppConfiguration(dto);
  }
}
