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

import java.io.*;
import java.nio.file.*;
import lombok.*;
import picocli.CommandLine.*;

@NoArgsConstructor
public class OutputDirectoryParameter {

  @Parameters(
      arity = "0..1",
      paramLabel = "OutputPath",
      description =
          """
          Path to directory where files should be written to.
          If the output directory is omitted and only an input directory is given, the input and output directories will result in the same path.
          If neither an input nor an output directory was given, the current user directory will be chosen for the output.
          """)
  protected Path out;

  public OutputDirectoryParameter(Path out) {
    this.out = out;
  }

  public boolean hasSetPath() {
    return out != null;
  }

  public Path getOut() {
    if (!this.hasSetPath()) {
      out = Path.of(System.getProperty("user.dir"));
    }
    return out;
  }

  private Path getAbsolutePath() {
    return this.getOut().toAbsolutePath();
  }

  private String getAbsolutePathString() {
    return this.getAbsolutePath().toString();
  }

  public void writeFile(String filename, String content) {
    val writePath = Path.of(this.getAbsolutePathString(), filename).toFile();
    writeFile(writePath, content);
  }

  public void writeFile(String filename, String subdirectory, String content) {
    checkInit(subdirectory);
    val writePath = Path.of(this.getAbsolutePathString(), subdirectory, filename).toFile();
    writeFile(writePath, content);
  }

  @SneakyThrows
  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  public void writeFile(File file, String content) {
    checkInit();
    try (val writer = new BufferedWriter(new FileWriter(file))) {
      writer.write(content);
    }
  }

  private void checkInit() {
    checkInit("");
  }

  @SuppressWarnings("ResultOfMethodCallIgnored")
  private void checkInit(String subdirectory) {
    val fullPath = Path.of(this.getAbsolutePathString(), subdirectory);
    fullPath.toAbsolutePath().toFile().mkdirs();
  }
}
