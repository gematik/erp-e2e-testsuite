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

package de.gematik.test.erezept.remotefdv.cfg;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.config.dto.remotefdv.RemoteFdVActorConfiguration;
import de.gematik.test.erezept.config.dto.remotefdv.RemoteFdVConfiguration;
import de.gematik.test.erezept.config.dto.remotefdv.RemoteFdVConfigurationBase;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class ErpRemoteFdVConfigurationTest {
  @Test
  void shouldCreateRemoteFdVConfig() {
    val userName = GemFaker.fakerName();
    val fdvName = GemFaker.fakerName();
    val actorConfig = new RemoteFdVActorConfiguration();
    val fdvConfig = new RemoteFdVConfiguration();
    val configBase = new RemoteFdVConfigurationBase();

    actorConfig.setName(userName);
    fdvConfig.setName(fdvName);
    configBase.setUsers(List.of(actorConfig));
    configBase.setRemoteFdVs(List.of(fdvConfig));

    val config = ErpRemoteFdVConfiguration.fromDto(configBase);
    assertEquals(actorConfig, config.getAppUserByName(userName));
    assertEquals(fdvConfig, config.getRemoteFdVConfiguration(fdvName));
  }
}
