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
import de.gematik.test.erezept.fhir.parser.profiles.version.ProfileVersion;
import java.beans.Transient;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Data
@Slf4j
public class ParserConfigurations {

  public static final String ENV_TOGGLE = "ERP_FHIR_PROFILE";
  public static final String SYS_PROP_TOGGLE = "erp.fhir.profile";
  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
  private static ParserConfigurations instance;

  private List<ProfileSettingConfig> profileSettings;

  @SneakyThrows
  public static ParserConfigurations getInstance() {
    if (instance == null) {
      val profilesConfig =
          Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("parsers.yaml"));

      instance = MAPPER.readValue(profilesConfig, ParserConfigurations.class);
    }

    return instance;
  }

  @SuppressWarnings({"unchecked"})
  public <T extends ProfileVersion<?>, S extends ProfileVersion<?>> T getAppropriateVersion(
      Class<T> counterpart, S source) {
    val sourceName = source.getCustomProfile().getName();
    val sourceVersion = source.getVersion();
    val setting =
        profileSettings.stream()
            .filter(
                s ->
                    s.getProfiles().stream()
                        .anyMatch(
                            profile ->
                                profile.getName().equals(sourceName)
                                    && profile.getVersion().equals(sourceVersion)))
            .findFirst()
            .orElseThrow(
                () -> new FhirProfileException(format("No Profiles found for {0}", source)));

    val template = counterpart.getEnumConstants()[0];
    val d = setting.getVersionedProfile(template.getCustomProfile().getName());
    return (T) d.getVersionedProfile().getProfileVersion();
  }

  public ProfileSettingConfig getValidatorConfiguration(String version) {
    return profileSettings.stream()
        .filter(p -> p.getId().equalsIgnoreCase(version))
        .findFirst()
        .orElseThrow(
            () ->
                new FhirProfileException(
                    format("No ValidatorConfigurations available with Version {0}", version)));
  }

  public Optional<ProfileSettingConfig> getDefaultConfiguration() {
    val envSetVersion = System.getProperty(SYS_PROP_TOGGLE, System.getenv(ENV_TOGGLE));
    if (envSetVersion != null) {
      return Optional.of(getValidatorConfiguration(envSetVersion));
    } else {
      return Optional.empty();
    }
  }

  @Data
  public static class ProfileSettingConfig {
    private String id;
    private String note;
    private List<ProfileDto> profiles;
    private List<String> errorFilter;

    @Transient
    public ProfileDto getVersionedProfile(String name) {
      return getOptionalVersionedProfile(name)
          .orElseThrow(
              () ->
                  new FhirProfileException(
                      format(
                          "Profile configuration {0} does not contain a profile named {1}",
                          id, name)));
    }

    @Transient
    public Optional<ProfileDto> getOptionalVersionedProfile(String name) {
      return profiles.stream()
          .filter(profile -> profile.getName().equalsIgnoreCase(name))
          .findFirst();
    }
  }
}
