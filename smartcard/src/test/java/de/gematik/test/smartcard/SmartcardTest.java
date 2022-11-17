/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.smartcard;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SmartcardTest {

  private static SmartcardArchive archive;

  @BeforeAll
  static void readArchive() {
    archive = SmartcardFactory.getArchive();
  }

  @Test
  void shouldToString() {
    List.of(
            archive.getEgkCards().get(0),
            archive.getHbaCards().get(0),
            archive.getSmcbCards().get(0))
        .forEach(
            card -> {
              assertTrue(card.toString().contains(card.getIccsn()));
              assertTrue(card.toString().contains(card.getType().toString()));
            });
  }

  @Test
  void shouldEqualSameObjects() {
    val egk = archive.getEgkCards().get(0);
    assertEquals(egk, egk);
  }

  @Test
  void shouldNotEqualOnDifferentClassTypes() {
    val egk = archive.getEgkCards().get(0);
    val hba = archive.getHbaCards().get(0);
    assertNotEquals(egk, hba);
    assertNotEquals(hba, egk);
  }
}
