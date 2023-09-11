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

import java.nio.file.*;
import lombok.experimental.*;
import picocli.CommandLine.*;

public class InputOutputDirectoryParameter {

  @Delegate @Mixin InputDirectoryParameter input;

  @Delegate(excludes = DelegateExclude.class)
  @Mixin
  OutputDirectoryParameter output;

  public Path getOut() {
    this.ensureOutputDirectory();
    return output.getOut();
  }

  private void ensureOutputDirectory() {
    if (output.out == null) {
      // if no output path given, use the same as the input path
      output.out = Path.of(input.getInputDirectory().toAbsolutePath().toString(), "out");
    }
  }

  public void writeFile(String filename, String content) {
    this.ensureOutputDirectory();
    this.output.writeFile(filename, content);
  }

  public void writeFile(String filename, String subdirectory, String content) {
    this.getOut();
    this.output.writeFile(filename, subdirectory, content);
  }

  private interface DelegateExclude {
    Path getOut();

    void writeFile(String filename, String subdirectory, String content);

    void writeFile(String filename, String content);
  }
}
