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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.valuesets.IdentifierTypeDe;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class KvnrParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val kvidp = new KvnrParameter();
    val cmdline = new CommandLine(kvidp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertNotNull(kvidp.getKvnr());
    assertNotNull(kvidp.getInsuranceType());
    assertNotNull(kvidp.getIdentifierTypeDe());
  }

  @Test
  void shouldSetOptionValues() {
    val kvidp = new KvnrParameter();
    val cmdline = new CommandLine(kvidp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--kvnr", "X123456789", "--insurance-type", "GKV"));

    assertEquals("X123456789", kvidp.getKvnr().getValue());
    assertEquals(VersicherungsArtDeBasis.GKV, kvidp.getInsuranceType());
    assertEquals(VersicherungsArtDeBasis.GKV, kvidp.getInsuranceType(VersicherungsArtDeBasis.PKV));
    assertEquals(IdentifierTypeDe.GKV, kvidp.getIdentifierTypeDe());
  }

  @Test
  void shouldOverwriteDefaultVersicherungsArt() {
    val kvidp = new KvnrParameter();
    val cmdline = new CommandLine(kvidp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertEquals(VersicherungsArtDeBasis.PKV, kvidp.getInsuranceType(VersicherungsArtDeBasis.PKV));
    assertEquals(IdentifierTypeDe.GKV, kvidp.getIdentifierTypeDe());
  }

  @Test
  void shouldSetPKVIdentifierOnOtherThanGkv() {
    val kvidp = new KvnrParameter();
    val cmdline = new CommandLine(kvidp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--insurance-type", "PPV"));
    assertEquals(VersicherungsArtDeBasis.PPV, kvidp.getInsuranceType());
    assertEquals(IdentifierTypeDe.PKV, kvidp.getIdentifierTypeDe());
  }
}
