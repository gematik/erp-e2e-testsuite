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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import static java.text.MessageFormat.format;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.erezept.fhir.exceptions.FhirProfileException;
import de.gematik.test.erezept.fhir.parser.profiles.version.VersionedProfile;
import java.beans.Transient;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@Data
public class ProfilesIndex {

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
  private static ProfilesIndex instance;

  private List<ProfileSourceDto> profiles;

  @SneakyThrows
  public static ProfilesIndex getInstance() {
    if (instance == null) {
      val profilesConfig =
          Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("profiles.yaml"));

      instance = MAPPER.readValue(profilesConfig, ProfilesIndex.class);
    }

    return instance;
  }

  @Transient
  public ProfileSourceDto getProfile(VersionedProfile<?> profile) {
    return profiles.stream()
        .filter(p -> p.getVersionedProfile().equals(profile))
        .findFirst()
        .orElseThrow(
            () ->
                new FhirProfileException(
                    format("Index does not contain matching profiles for {0}", profile)));
  }
}
