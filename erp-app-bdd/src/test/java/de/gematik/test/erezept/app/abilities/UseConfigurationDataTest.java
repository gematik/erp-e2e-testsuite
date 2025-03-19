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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.config.ConfigurationReader;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseConfigurationDataTest {

  @Test
  void shouldInstantiate() {
    val dto = ConfigurationReader.forAppConfiguration().create();
    val config = ErpAppConfiguration.fromDto(dto);
    val user = dto.getUsers().get(0);
    val ability = UseConfigurationData.forUser(user.getName(), config);
    assertEquals(user.getName(), ability.getName());
    assertEquals(user.getDevice(), ability.getDevice());
  }

  @Test
  void shouldGetAbilityForCoUser() {
    val dto = ConfigurationReader.forAppConfiguration().create();
    val config = ErpAppConfiguration.fromDto(dto);
    val user =
        dto.getUsers().stream()
            .filter(u -> u.getDevice() == null)
            .findAny()
            .orElse(dto.getUsers().get(1));
    val device = dto.getDevices().get(0);
    val ability = UseConfigurationData.asCoUser(user.getName(), device.getName(), config);
    assertEquals(user.getName(), ability.getName());
    assertEquals(device.getName(), ability.getDevice());
  }
}
