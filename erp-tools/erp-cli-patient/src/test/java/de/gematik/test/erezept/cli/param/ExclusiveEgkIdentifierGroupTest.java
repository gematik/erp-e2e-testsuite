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

package de.gematik.test.erezept.cli.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class ExclusiveEgkIdentifierGroupTest {

  @Test
  void shouldHaveKvnrs() {
    val eegkip = new ExclusiveEgkIdentifierGroup();
    val cmdline = new CommandLine(eegkip);
    assertDoesNotThrow(() -> cmdline.parseArgs("--kvnr", "X123123123"));
    assertTrue(eegkip.hasKvnrs());
    assertFalse(eegkip.hasIccsns());
  }

  @Test
  void shouldHaveIccsns() {
    val eegkip = new ExclusiveEgkIdentifierGroup();
    val cmdline = new CommandLine(eegkip);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iccsn", "123,456,890"));
    assertFalse(eegkip.hasKvnrs());
    assertTrue(eegkip.hasIccsns());
  }

  @Test
  void shouldAllowBoth() {
    val sca = SmartcardFactory.getArchive();
    val egk1 = sca.getEgkCards().get(0);
    val egk2 = sca.getEgkCards().get(0);

    val eegkip = new ExclusiveEgkIdentifierGroup();
    val cmdline = new CommandLine(eegkip);
    assertDoesNotThrow(
        () -> cmdline.parseArgs("--iccsn", egk1.getIccsn(), "--kvnr", egk2.getKvnr()));
    assertTrue(eegkip.hasKvnrs());
    assertTrue(eegkip.hasIccsns());

    val chosen = eegkip.getEgks(sca);
    assertEquals(2, chosen.size());
    assertEquals(egk1, chosen.get(0));
    assertEquals(egk2, chosen.get(1));
  }
}
