/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import static java.text.MessageFormat.*;

import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.dataformat.yaml.*;
import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.ValidatorMode;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import java.beans.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;

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
                () -> new FhirValidatorException(format("No Profiles found for {0}", source)));

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
                new FhirValidatorException(
                    format("No ValidatorConfigurations available with Version {0}", version)));
  }

  public Optional<ProfileSettingConfig> getDefaultConfiguration() {
    val envSetVersion = System.getProperty(SYS_PROP_TOGGLE, System.getenv(ENV_TOGGLE));
    if (envSetVersion != null && !envSetVersion.isEmpty() && !envSetVersion.isBlank()) {
      return Optional.of(getValidatorConfiguration(envSetVersion));
    } else {
      return Optional.empty();
    }
  }

  @Data
  public static class ProfileSettingConfig {
    private String id;
    private String note;
    private ValidatorMode mode = ValidatorMode.getDefault();
    private List<ProfileDto> profiles;
    private List<String> errorFilter;

    @Transient
    public ProfileDto getVersionedProfile(String name) {
      return getOptionalVersionedProfile(name)
          .orElseThrow(
              () ->
                  new FhirValidatorException(
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
