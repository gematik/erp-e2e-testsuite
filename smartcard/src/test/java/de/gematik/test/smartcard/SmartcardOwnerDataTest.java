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

import static org.junit.jupiter.api.Assertions.assertEquals;

import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SmartcardOwnerDataTest {
  private static SmartcardArchive sca;

  @BeforeAll
  static void setup() {
    sca = SmartcardFactory.getArchive();
  }

  @Test
  void shouldReturnOwnerName() {
    val hbaRsa = sca.getHbaCards().get(0);
    String expected = hbaRsa.getOwner().getOwnerName();
    assertEquals(expected, hbaRsa.getOwner().toString());
  }

  @Test
  void shouldReturnOwnerNameWithTitle() {
    val mockOwner =
        SmartcardOwnerData.builder().givenName("Bernd").surname("Claudius").title("Dr.").build();
    assertEquals("Dr. Bernd, Claudius", mockOwner.getOwnerName());
  }
}
