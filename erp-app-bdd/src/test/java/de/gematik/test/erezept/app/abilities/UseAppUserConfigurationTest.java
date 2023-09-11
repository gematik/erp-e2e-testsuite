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

package de.gematik.test.erezept.app.abilities;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.app.cfg.ErpAppConfiguration;
import de.gematik.test.erezept.config.ConfigurationFactory;
import lombok.val;
import org.junit.jupiter.api.Test;

class UseAppUserConfigurationTest {

  @Test
  void shouldInstantiate() {
    val config = ConfigurationFactory.forAppConfiguration().wrappedBy(ErpAppConfiguration::fromDto);
    val user = config.getUsers().get(0);
    val ability = UseAppUserConfiguration.forUser(user.getName(), config);
    assertEquals(user.getName(), ability.getName());
    assertEquals(user.getDevice(), ability.getDevice());
  }
}
