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

package de.gematik.test.erezept.fhir.parser.profiles.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.util.List;
import java.util.Objects;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;

@Data
public class ParserConfigurations {

  private static final ObjectMapper MAPPER = new ObjectMapper(new YAMLFactory());
  private static ParserConfigurations instance;

  private List<ProfileSettingConfig> parsers;

  @SneakyThrows
  public static ParserConfigurations getInstance() {
    if (instance == null) {
      val profilesConfig =
          Objects.requireNonNull(ClassLoader.getSystemResourceAsStream("parsers.yaml"));

      instance = MAPPER.readValue(profilesConfig, ParserConfigurations.class);
    }

    return instance;
  }

  @Data
  public static class ProfileSettingConfig {
    private String name;
    private String note;
    private List<ProfileDto> profiles;
  }
}
