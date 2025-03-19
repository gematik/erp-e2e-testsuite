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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import lombok.val;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;
import picocli.CommandLine.UnmatchedArgumentException;

class InputDirectoryParamTest {

  @Test
  void shouldFailOnMissingRequiredPath() {
    val cmdline = new CommandLine(new InputDirectoryParameter());
    assertThrows(MissingParameterException.class, cmdline::parseArgs);
  }

  @Test
  void shouldNotAllowUnmatchedArguments() {
    val cmdline = new CommandLine(new InputDirectoryParameter());
    assertThrows(UnmatchedArgumentException.class, () -> cmdline.parseArgs("/a/b/c", "/d/e/f"));
  }

  @Test
  void shouldThrowOnInvalidPath() {
    val cmdline = new CommandLine(new InputDirectoryParameter());
    val parseResult = cmdline.parseArgs("/a/b/c");
    assertTrue(parseResult.hasMatchedPositional(0));
    val input = parseResult.matchedPositional(0).stringValues();
    assertEquals("/a/b/c", input.get(0));
  }

  @Test
  void shouldThrowOnNextWithoutHasNext() {
    val inputDirectoryParam = new InputDirectoryParameter();
    val cmdline = new CommandLine(inputDirectoryParam);
    cmdline.parseArgs("/a/b/c");
    assertThrows(NoSuchElementException.class, inputDirectoryParam::next);
  }

  @Test
  void shouldThrowOnEmptyNext() {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "empty_next");
    emptyTargetDir.toFile().mkdirs();

    val inputDirectoryParam = new InputDirectoryParameter();
    val cmdline = new CommandLine(inputDirectoryParam);
    cmdline.parseArgs(emptyTargetDir.toAbsolutePath().toString());
    assertFalse(inputDirectoryParam.hasNext());
    assertThrows(NoSuchElementException.class, inputDirectoryParam::next);
  }

  @Test
  void shouldWalkNextFile() throws IOException {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "walk_next_file");
    emptyTargetDir.toFile().mkdirs();

    val emptyFile = Path.of(emptyTargetDir.toString(), "test.txt");
    emptyFile.toFile().createNewFile();

    val inputDirectoryParam = new InputDirectoryParameter();
    val cmdline = new CommandLine(inputDirectoryParam);
    cmdline.parseArgs(emptyTargetDir.toAbsolutePath().toString());
    assertTrue(inputDirectoryParam.hasNext());
    assertTrue(inputDirectoryParam.hasNext()); // asking a second should not have negative effects
    val next = inputDirectoryParam.next();
    assertNotNull(next);
    assertEquals("test.txt", next.getName());
    assertFalse(inputDirectoryParam.hasNext()); // asking after consuming should result in false
  }

  @Test
  void shouldWalkSingleFile() throws IOException {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "walk_single_file");
    emptyTargetDir.toFile().mkdirs();

    val emptyFile = Path.of(emptyTargetDir.toString(), "test.txt");
    emptyFile.toFile().createNewFile();

    val inputDirectoryParam = new InputDirectoryParameter();
    val cmdline = new CommandLine(inputDirectoryParam);
    cmdline.parseArgs(emptyFile.toAbsolutePath().toString());
    assertTrue(inputDirectoryParam.hasNext());
    assertTrue(inputDirectoryParam.hasNext()); // asking a second should not have negative effects
    val next = inputDirectoryParam.next();
    assertNotNull(next);
    assertEquals("test.txt", next.getName());
    assertFalse(inputDirectoryParam.hasNext()); // asking after consuming should result in false
    // use the parent directory of the single file instead of the file path directly
    assertEquals(emptyTargetDir, inputDirectoryParam.getInputDirectory());
  }

  @Test
  void shouldWalkOnlyXmlFile() throws IOException {
    val emptyTargetDir = Path.of(System.getProperty("user.dir"), "target", "walk_only_xml");
    emptyTargetDir.toFile().mkdirs();

    Path.of(emptyTargetDir.toString(), "test.xml").toFile().createNewFile();
    Path.of(emptyTargetDir.toString(), "test.json").toFile().createNewFile();

    val inputDirectoryParam = new InputDirectoryParameter();
    val cmdline = new CommandLine(inputDirectoryParam);
    cmdline.parseArgs("-t=XML", emptyTargetDir.toAbsolutePath().toString());
    assertTrue(inputDirectoryParam.hasNext());
    val next = inputDirectoryParam.next();
    assertNotNull(next);
    assertEquals("test.xml", next.getName());
    assertFalse(inputDirectoryParam.hasNext()); // asking after consuming should result in false
  }
}
