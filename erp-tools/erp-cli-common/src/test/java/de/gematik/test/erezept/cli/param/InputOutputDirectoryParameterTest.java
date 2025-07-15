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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.cli.param;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import lombok.*;
import org.junit.jupiter.api.*;
import picocli.*;
import picocli.CommandLine.*;

class InputOutputDirectoryParameterTest {

  @Test
  void shouldFailOnMissingRequiredPath() {
    val cmdline = new CommandLine(new InputOutputDirectoryParameter());
    assertThrows(MissingParameterException.class, cmdline::parseArgs);
  }

  @Test
  void shouldUseInputAsOutputOnMissingOutputPath() {
    val io = new InputOutputDirectoryParameter();
    val cmdline = new CommandLine(io);
    val inputDirectory = "/a/b/c";
    assertDoesNotThrow(() -> cmdline.parseArgs(inputDirectory));
    val expectedOut = Path.of(inputDirectory, "out");
    assertEquals(expectedOut, io.getOut());
  }

  @Test
  void shouldSetOutputPath() {
    val io = new InputOutputDirectoryParameter();
    val cmdline = new CommandLine(io);
    assertDoesNotThrow(() -> cmdline.parseArgs("/a/b/c", "/d/e/f"));
    assertEquals("/a/b/c", io.getIn().toString());
    assertEquals("/d/e/f", io.getOut().toString());
  }

  @Test
  void shouldWriteToSubdirectory() {
    val io = new InputOutputDirectoryParameter();
    val cmdline = new CommandLine(io);
    val outputDir = Path.of(System.getProperty("user.dir"), "target", "tmp").toString();
    assertDoesNotThrow(() -> cmdline.parseArgs(outputDir, outputDir));
    assertEquals(outputDir, io.getIn().toString());
    assertEquals(outputDir, io.getOut().toString());

    io.writeFile("hello_world.txt", "test", "Hello World");
    assertTrue(Files.exists(Path.of(outputDir, "test", "hello_world.txt")));
  }
}
