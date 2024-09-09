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

package de.gematik.test.konnektor.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.config.dto.konnektor.LocalKonnektorConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.RemoteKonnektorConfiguration;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;

class KonnektorModuleFactoryTest {

  @Test
  void shouldWrapFromKonnektorConfigList() {
    val lkc = new LocalKonnektorConfiguration();
    lkc.setName("Soft-Konn");

    val rkc = new RemoteKonnektorConfiguration();
    rkc.setName("Remote-Konn");
    rkc.setProtocol("https");
    rkc.setAddress("localhost:443");
    rkc.setProfile("KONSIM");

    val factory = KonnektorModuleFactory.fromKonnektorConfigs(List.of(lkc, rkc));
    assertNotNull(factory);

    val softKonn = factory.createSoftKon();
    assertNotNull(softKonn);
    assertEquals("Soft-Konn", softKonn.getName());
  }
}
