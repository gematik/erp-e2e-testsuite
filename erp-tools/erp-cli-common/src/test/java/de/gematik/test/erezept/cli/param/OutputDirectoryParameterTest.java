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
 */

package de.gematik.test.erezept.cli.param;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;

class OutputDirectoryParameterTest {

  private void cleanDirectory(Path path) {
    val files = path.toFile().listFiles();
    if (files != null) {
      for (var f : files) {
        if (f.isDirectory()) {
          this.cleanDirectory(f.toPath());
        } else {
          f.delete();
        }
      }
    }
    path.toFile().delete();
  }

  @Test
  void shouldHaveDefaultPath() {
    val odp = new OutputDirectoryParameter();
    val cmdline = new CommandLine(odp);
    assertDoesNotThrow(() -> cmdline.parseArgs());
  }

  @Test
  void shouldCreateOutputDir() {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "out");
    val outputFile = Path.of(emptyTargetDir.toString(), "test.txt").toFile();
    this.cleanDirectory(emptyTargetDir.getParent());

    val odp = new OutputDirectoryParameter();
    val cmdline = new CommandLine(odp);

    assertFalse(emptyTargetDir.toFile().exists());
    assertFalse(outputFile.exists());
    assertDoesNotThrow(() -> cmdline.parseArgs(emptyTargetDir.toString()));
    odp.writeFile(outputFile, "Hello World");
    assertTrue(emptyTargetDir.toFile().exists());
    assertTrue(outputFile.exists());
  }

  @Test
  void shouldReuseOutputDir() {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "out");
    emptyTargetDir.toFile().mkdirs();
    val outputFile = Path.of(emptyTargetDir.toString(), "test2.txt").toFile();
    outputFile.delete();

    val odp = new OutputDirectoryParameter();
    val cmdline = new CommandLine(odp);

    assertTrue(emptyTargetDir.toFile().exists());
    assertFalse(outputFile.exists());
    assertDoesNotThrow(() -> cmdline.parseArgs(emptyTargetDir.toString()));
    odp.writeFile(outputFile.getName(), "Hello World");
    assertTrue(emptyTargetDir.toFile().exists());
    assertTrue(outputFile.exists());
  }

  @Test
  void shouldReuseOutputSubDir() {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "tmp", "out");
    emptyTargetDir.toFile().mkdirs();
    val outputFile = Path.of(emptyTargetDir.toString(), "subdir", "test2.txt").toFile();
    outputFile.delete();

    val odp = new OutputDirectoryParameter();
    val cmdline = new CommandLine(odp);

    assertTrue(emptyTargetDir.toFile().exists());
    assertFalse(outputFile.exists());
    assertDoesNotThrow(() -> cmdline.parseArgs(emptyTargetDir.toString()));
    odp.writeFile(outputFile.getName(), "subdir", "Hello World");
    assertTrue(emptyTargetDir.toFile().exists());
    assertTrue(outputFile.exists());
  }
}
