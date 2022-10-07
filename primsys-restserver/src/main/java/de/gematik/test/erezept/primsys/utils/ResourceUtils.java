/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.primsys.utils;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.exceptions.ResourceFileException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class ResourceUtils {

  private static final ClassLoader CLASS_LOADER = ClassLoader.getSystemClassLoader();

  private ResourceUtils() {
    // utils class does not need a public constructor
  }

  /**
   * Get a file from the resources folder and returns the file as an InputStream
   *
   * @param fileName of the File to read
   * @return InputStream of the file content
   */
  public static InputStream getFileFromResourceAsStream(final String fileName) {
    // The class loader that loaded the class
    val inputStream = CLASS_LOADER.getResourceAsStream(fileName);

    // the stream holding the file content
    if (inputStream == null) {
      throw new ResourceFileException(format("File {0} not found in resources!", fileName));
    } else {
      return inputStream;
    }
  }

  /**
   * Reads a file from the resources folder and returns the content as a UTF-8 encoded String
   *
   * @param fileName of the File to read
   * @return the file content as a UTF-8 encoded String
   */
  public static String readFileFromResource(final String fileName) {
    val stream = ResourceUtils.getFileFromResourceAsStream(fileName);

    try {
      return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new ResourceFileException(
          format("Error while reading from resources file {0}", fileName));
    }
  }

  @SneakyThrows
  public static List<File> getResourceFilesInDirectory(final String path) {
    val pathUrl = Objects.requireNonNull(CLASS_LOADER.getResource(path)).toURI();
    val dir = Path.of(String.valueOf(Paths.get(pathUrl))).toFile();
    if (!dir.exists() || !dir.isDirectory()) {
      throw new ResourceFileException(
          format("Given path {0} does not exist or is not a directory", path));
    }

    return List.of(Objects.requireNonNull(dir.listFiles()));
  }
}
