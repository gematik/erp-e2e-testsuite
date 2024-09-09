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

package de.gematik.test.erezept.cli.cmd.generate;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ExitCode;

class ChargeItemGeneratorTest {
  @Test
  void shouldGenerateWithDefaults() {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "out");
    val cig = new ChargeItemGenerator();
    val cmdline = new CommandLine(cig);
    val ret = cmdline.execute(emptyTargetDir.toString());
    assertEquals(ExitCode.OK, ret);
  }
}
