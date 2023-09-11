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

package de.gematik.test.konnektor.soap.mock.vsdm;

import de.gematik.test.konnektor.cfg.VsdmServiceConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class VsdmServiceTest {

  private static VsdmService vsdmService;

  @BeforeAll
  public static void setup() {
    vsdmService = VsdmServiceConfiguration.createCfg().createDefault();
  }

  @Test
  void invalidEgk() {
    for (VsdmUpdateReason reason : VsdmUpdateReason.values()) {
      log.debug(reason.toString());
      Assertions.assertThrows(
          NullPointerException.class, () -> vsdmService.requestFor(null, reason));
    }
  }
}
