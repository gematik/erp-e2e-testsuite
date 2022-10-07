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

package de.gematik.test.erezept.fhir;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.CustomProfileSupport;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/** Use the Generator to update/generate a list of FHIR profiles contained in this module */
@Slf4j
public class ProfileListGenerator {

  private static final String PROFILES_DIR_NAME = "profiles";

  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  public static void main(String[] args) throws IOException {
    val generatedPath =
        Path.of("erp-fhir", "src", "main", "resources", CustomProfileSupport.CUSTOM_PROFILES_INDEX)
            .toAbsolutePath();
    log.info("Create Profiles file in " + generatedPath);

    try (val writer = new FileWriter(generatedPath.toString(), false)) {
      writer.write(format("// File automatically generated {0}\n", new Date()));

      val profileDirs = ResourceUtils.getResourceFilesInDirectory(PROFILES_DIR_NAME);
      profileDirs.stream().filter(File::isDirectory).forEach(f -> writeProfilesFromDir(f, writer));

      writer.flush();
    }
  }

  @SneakyThrows
  private static void writeProfilesFromDir(File profileParent, FileWriter writer) {
    val packageDir = Objects.requireNonNull(profileParent.listFiles())[0];
    log.info("Read from " + profileParent);
    if (packageDir.exists() && packageDir.isDirectory()) {
      val files = List.of(Objects.requireNonNull(packageDir.listFiles()));
      writer.append(format("\n// {0}\n", profileParent.getName()));
      for (val f : files) {
        if (!f.getName().equals("package.json")) {
          writer.append(
              format(
                  "{0}\n",
                  Path.of(PROFILES_DIR_NAME, profileParent.getName(), "package", f.getName())));
        }
      }
    }
  }
}
