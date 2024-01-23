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

package de.gematik.test.smartcard.cfg;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.smartcard.*;
import java.util.*;
import lombok.*;
import org.junit.jupiter.api.*;

class SmartcardConfigDtoTest {

  @Test
  void shouldGetCorrectSmartcardType() {
    val smConf = new SmartcardConfigDto();
    Arrays.stream(SmartcardType.values())
        .forEach(
            smt -> {
              smConf.setType(smt.toString());
              assertEquals(smt, smConf.getCardType());
            });
  }

  @Test
  void shouldGetCorrectSmartcardTypeFromSimilarStrings() {
    val smConf = new SmartcardConfigDto();
    Arrays.stream(SmartcardType.values())
        .forEach(
            smt -> {
              smConf.setType(smt.toString().toUpperCase());
              assertEquals(smt, smConf.getCardType());
            });
  }

  @Test
  void shouldGetKeyStoreTypeP12() {
    val smConf = new SmartcardConfigDto();
    assertEquals(KeystoreType.P12, smConf.getKeystoreType());
  }
}
