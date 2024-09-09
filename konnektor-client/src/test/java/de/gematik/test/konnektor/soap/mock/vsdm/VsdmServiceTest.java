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

package de.gematik.test.konnektor.soap.mock.vsdm;

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
class VsdmServiceTest {

  private static VsdmService vsdmService;

  @BeforeAll
  static void setup() {
    vsdmService = VsdmService.instantiateWithTestKey();
  }

  @Test
  void checksumWithInvalidManufacturer() {
    assertEquals('y', vsdmService.checksumWithInvalidManufacturer("A111111111").getIdentifier());
  }

  @Test
  void checksumWithInvalidVersion() {
    assertEquals('0', vsdmService.checksumWithInvalidVersion("A111111111").getVersion());
  }
}
