/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EgkTest {

  private static SmartcardArchive archive;

  // TODO: missing p12 files for these eGKs
  private static final List<String> FILTER_ICCSNS = List.of("80276881040001935352");

  @BeforeAll
  static void setupArchive() {
    archive = SmartcardFactory.getArchive();
  }

  @Test
  void shouldGetPrivateKeysAsBase64() {
    archive.getEgkCards().stream()
        .filter(egk -> !FILTER_ICCSNS.contains(egk.getIccsn()))
        .forEach(egk -> assertDoesNotThrow(egk::getPrivateKeyBase64));
  }

  @Test
  void shouldGetDefaultKey() {
    List.of(Algorithm.ECC_256, Algorithm.RSA_2048)
        .forEach(
            crpyto ->
                archive.getEgkCards().stream()
                    .filter(egk -> !FILTER_ICCSNS.contains(egk.getIccsn()))
                    .forEach(egk -> assertTrue(egk.getAutCertificate(crpyto).isPresent())));
  }
}
