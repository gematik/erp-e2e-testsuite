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

package de.gematik.test.erezept.cli.param;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class EgkParameterTest {

  @Test
  void shouldThrowOnMissingKvnrs() {
    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertThrows(CommandLine.MissingParameterException.class, cmdline::parseArgs);
  }

  @Test
  void shouldGetEgkByKvnr() {
    val sca = SmartcardFactory.getArchive();
    val egk = sca.getEgkCards().get(0);

    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--kvnr", egk.getKvnr()));

    val chosen = egkp.getEgks(sca);
    assertEquals(1, chosen.size());
    assertEquals(egk, chosen.get(0));
  }

  @Test
  void shouldGetMultipleEgkByKvnr() {
    val sca = SmartcardFactory.getArchive();
    val egk1 = sca.getEgkCards().get(0);
    val egk2 = sca.getEgkCards().get(0);

    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertDoesNotThrow(
        () -> cmdline.parseArgs("--kvnr", format("{0},{1}", egk1.getKvnr(), egk2.getKvnr())));

    val chosen = egkp.getEgks(sca);
    assertEquals(2, chosen.size());
    assertEquals(egk1, chosen.get(0));
    assertEquals(egk2, chosen.get(1));
  }

  @Test
  void shouldGetEgkByIccsn() {
    val sca = SmartcardFactory.getArchive();
    val egk = sca.getEgkCards().get(0);

    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--iccsn", egk.getIccsn()));

    val chosen = egkp.getEgks(sca);
    assertEquals(1, chosen.size());
    assertEquals(egk, chosen.get(0));
  }

  @Test
  void shouldGetMultipleEgkByIccsn() {
    val sca = SmartcardFactory.getArchive();
    val egk1 = sca.getEgkCards().get(0);
    val egk2 = sca.getEgkCards().get(0);

    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertDoesNotThrow(
        () -> cmdline.parseArgs("--iccsn", format("{0},{1}", egk1.getIccsn(), egk2.getIccsn())));

    val chosen = egkp.getEgks(sca);
    assertEquals(2, chosen.size());
    assertEquals(egk1, chosen.get(0));
    assertEquals(egk2, chosen.get(1));
  }

  @Test
  void shouldNotAllowBoth() {
    val sca = SmartcardFactory.getArchive();
    val egk1 = sca.getEgkCards().get(0);
    val egk2 = sca.getEgkCards().get(0);

    val egkp = new EgkParameter();
    val cmdline = new CommandLine(egkp);
    assertThrows(
        CommandLine.MutuallyExclusiveArgsException.class,
        () -> cmdline.parseArgs("--iccsn", egk1.getIccsn(), "--kvnr", egk2.getKvnr()));
  }
}
