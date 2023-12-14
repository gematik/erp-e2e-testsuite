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

package de.gematik.test.erezept.fhir.util;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import java.io.*;
import java.nio.file.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ResourceUtilsTest {

  @Test
  void shouldFailReadingStreamFromInvalidFile() {
    assertThrows(
        ResourceFileException.class,
        () -> ResourceUtils.getFileFromResourceAsStream("hello/world.txt"));
  }

  @Test
  void shouldFailReadingFromInvalidFile() {
    val fileName = "hello.txt";
    val file = Path.of(fileName).toFile();
    assertThrows(ResourceFileException.class, () -> ResourceUtils.readFileFromResource(fileName));
    assertThrows(ResourceFileException.class, () -> ResourceUtils.readFileFromResource(file));
  }

  @Test
  void shouldFailOnReadingFromInvalidDirectory() {
    assertThrows(
        NullPointerException.class, () -> ResourceUtils.getResourceFilesInDirectory("hell/"));
    assertThrows(
        NullPointerException.class,
        () -> ResourceUtils.getResourceFilesDirectoryStructure("hell/"));
  }

  @Test
  void shouldProvideDirectoryStructure() {
    val resources = ResourceUtils.getResourceFilesDirectoryStructure("profiles");
    val directories = resources.stream().filter(File::isDirectory).count();
    assertTrue(directories > 0);
  }

  @Test
  void shouldThrowOnNonDirectory() {
    assertThrows(
        ResourceFileException.class,
        () -> ResourceUtils.getResourceFilesDirectoryStructure("profiles.yaml"));
  }
}
