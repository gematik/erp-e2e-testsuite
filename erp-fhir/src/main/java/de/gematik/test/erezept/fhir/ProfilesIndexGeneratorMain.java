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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.fhir.parser.profiles.CustomProfiles;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ProfileSourceDto;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ProfilesIndex;
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

/** Use the Generator to update/generate a list of FHIR profiles contained in this module */
@Slf4j
public class ProfilesIndexGeneratorMain {

  private static final String PROFILES_DIR_NAME = "profiles";

  @SuppressWarnings({"java:S6300"}) // writing to File by intention; not an issue!
  public static void main(String[] args) throws IOException {
    val configPath =
        Path.of("erp-fhir", "src", "main", "resources", "profiles.yaml").toAbsolutePath();
    log.info("Create Profiles Index in " + configPath);

    try (val writer = new FileWriter(configPath.toString(), false)) {
      val profileDirs = ResourceUtils.getResourceFilesInDirectory(PROFILES_DIR_NAME);
      val profilesList = new LinkedList<ProfileSourceDto>();
      profileDirs.stream()
          .filter(File::isDirectory)
          .forEach(
              dir -> {
                log.info(format("Write profiles index for {0}", dir.getAbsolutePath()));
                val pdto = generateProfileDto(dir);
                profilesList.add(pdto);
              });
      val profilesConfig = new ProfilesIndex();
      profilesConfig.setProfiles(profilesList);

      val mapper = new ObjectMapper(new YAMLFactory());
      mapper.writeValue(writer, profilesConfig);
    }
  }

  private static ProfileSourceDto generateProfileDto(File profileParent) {
    val packageDir = Objects.requireNonNull(profileParent.listFiles())[0];
    if (packageDir.exists() && packageDir.isDirectory()) {
      val files = List.of(Objects.requireNonNull(packageDir.listFiles()));
      val customProfile = CustomProfiles.fromName(profileParent.getName());
      val version =
          ProfileVersion.fromString(customProfile.getVersionClass(), profileParent.getName());
      val profileDto = new ProfileSourceDto();
      profileDto.setName(customProfile.getName());
      profileDto.setVersion(version.getVersion());
      val profileFiles = new LinkedList<String>();

      for (val f : files) {
        if (!f.getName().equals("package.json")) {
          val path = Path.of(PROFILES_DIR_NAME, profileParent.getName(), "package", f.getName());
          profileFiles.add(path.toString());
        } else {
          log.trace(format("Skip {0}", f));
        }
      }

      profileDto.setFiles(profileFiles);
      return profileDto;
    }
    return null;
  }
}
