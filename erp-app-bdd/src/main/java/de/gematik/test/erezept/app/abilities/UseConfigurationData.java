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
 */

package de.gematik.test.erezept.app.abilities;

import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.config.dto.app.DeviceConfiguration;
import de.gematik.test.erezept.config.dto.app.ErpActorConfiguration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.val;
import net.serenitybdd.screenplay.Ability;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class UseConfigurationData implements Ability {

  @Delegate private final ErpActorConfiguration userConfig;
  @Delegate private final DeviceConfiguration deviceConfig;

  public static UseConfigurationData forUser(String userName, ErpAppConfiguration config) {
    val user = config.getAppUserByName(userName);
    val device = config.getDeviceByName(user.getDevice());
    return new UseConfigurationData(user, device);
  }

  public static UseConfigurationData asCoUser(
      String userName, String deviceName, ErpAppConfiguration config) {
    val user = config.getAppUserByName(userName);
    user.setDevice(deviceName);
    return forUser(userName, config);
  }
}
