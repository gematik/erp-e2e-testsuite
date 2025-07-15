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

package de.gematik.test.konnektor.soap.mock.vsdm;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.vsdm.VsdmCheckDigitVersion;
import java.time.Instant;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@Slf4j
class VsdmServiceTest {

  private static VsdmService vsdmService;
  private static Egk egk;

  @BeforeAll
  static void setup() {
    vsdmService = VsdmService.instantiateWithTestKey();
    val archive = SmartcardArchive.fromResources();
    egk = archive.getEgk(0);
  }

  @ParameterizedTest
  @EnumSource(value = VsdmService.CheckDigitConfiguration.class)
  void shouldRequestV1Successful(VsdmService.CheckDigitConfiguration cfg) {
    assertDoesNotThrow(
        () -> vsdmService.requestCheckDigitFor(cfg, egk, VsdmCheckDigitVersion.V1, Instant.now()));
    val checkdigit =
        vsdmService.requestCheckDigitFor(cfg, egk, VsdmCheckDigitVersion.V1, Instant.now());
    assertEquals(VsdmCheckDigitVersion.V1, VsdmCheckDigitVersion.fromData(checkdigit));
  }

  @ParameterizedTest
  @EnumSource(value = VsdmService.CheckDigitConfiguration.class)
  void shouldRequestV2Successful(VsdmService.CheckDigitConfiguration cfg) {
    assertDoesNotThrow(
        () -> vsdmService.requestCheckDigitFor(cfg, egk, VsdmCheckDigitVersion.V2, Instant.now()));

    val checkdigit =
        vsdmService.requestCheckDigitFor(cfg, egk, VsdmCheckDigitVersion.V2, Instant.now());
    assertEquals(VsdmCheckDigitVersion.V2, VsdmCheckDigitVersion.fromData(checkdigit));
  }

  @Test
  void shouldRequestWithoutIatTimestamp() {
    assertDoesNotThrow(
        () ->
            vsdmService.requestCheckDigitFor(
                VsdmService.CheckDigitConfiguration.DEFAULT, egk, VsdmCheckDigitVersion.V2, null));
    assertDoesNotThrow(
        () ->
            vsdmService.requestCheckDigitFor(
                VsdmService.CheckDigitConfiguration.DEFAULT, egk, VsdmCheckDigitVersion.V1, null));
  }
}
