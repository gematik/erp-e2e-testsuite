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

package de.gematik.test.erezept.app.mocker;

import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.config.dto.app.DeviceConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpActorConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpAppConfigurationBase;
import java.util.LinkedList;
import lombok.val;

public class ConfigurationMocker {

  public static ErpAppConfiguration createDefaultTestConfiguration(
      String userName, String iccsn, boolean shouldUseVirtualEgk) {
    val cfgDto = createEmptyConfigurationBase();
    val deviceConfig = createDeviceConfiguration("iPhone");
    val userConfig = createErpActorConfig(userName, iccsn, shouldUseVirtualEgk);
    userConfig.setDevice(deviceConfig.getName());
    cfgDto.getUsers().add(userConfig);
    cfgDto.getDevices().add(deviceConfig);
    return ErpAppConfiguration.fromDto(cfgDto);
  }

  public static ErpAppConfigurationBase createEmptyConfigurationBase() {
    val cfg = new ErpAppConfigurationBase();
    cfg.setApps(new LinkedList<>());
    cfg.setUsers(new LinkedList<>());
    cfg.setDevices(new LinkedList<>());
    cfg.setAppium(new LinkedList<>());
    return cfg;
  }

  public static ErpActorConfiguration createErpActorConfig(
      String name, String iccsn, boolean shouldUseVeGK) {
    val cfg = new ErpActorConfiguration();
    cfg.setName(name);
    cfg.setEgkIccsn(iccsn);
    cfg.setUseVirtualEgk(shouldUseVeGK);
    return cfg;
  }

  public static DeviceConfiguration createDeviceConfiguration(String name) {
    return createDeviceConfiguration(name, false);
  }

  public static DeviceConfiguration createDeviceConfiguration(String name, boolean hasNfc) {
    val cfg = new DeviceConfiguration();
    cfg.setName(name);
    cfg.setHasNfc(hasNfc);
    return cfg;
  }
}
