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

package de.gematik.test.erezept.cli.cmd.generate.param;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class MarkingFlagParameterTest {

  @Test
  void shouldNotRequireAnyOptions() {
    val mfp = new MarkingFlagParameter();
    val cmdline = new CommandLine(mfp);
    assertDoesNotThrow(() -> cmdline.parseArgs());

    assertFalse(mfp.getSubsidy());
    assertFalse(mfp.getInsuranceProvider());
    assertFalse(mfp.getTaxOffice());
  }

  @Test
  void shouldCreateMarkingFlags() {
    val mfp = new MarkingFlagParameter();
    val cmdline = new CommandLine(mfp);
    assertDoesNotThrow(() -> cmdline.parseArgs("--taxoffice", "--subsidy", "--insurance"));

    assertTrue(mfp.getSubsidy());
    assertTrue(mfp.getInsuranceProvider());
    assertTrue(mfp.getTaxOffice());

    val mf = mfp.createFlags();
    assertTrue(mf.isSubsidy());
    assertTrue(mf.isInsuranceProvider());
    assertTrue(mf.isTaxOffice());
  }
}
