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

package de.gematik.test.konnektor.cfg;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.exceptions.MissingKonnektorKonfigurationException;
import java.io.File;
import java.util.List;
import lombok.Data;
import lombok.NonNull;
import lombok.SneakyThrows;
import lombok.val;

/** This class is intended to be used only if the konnektor-client module is used separately */
@Data
public class KonnektorModuleConfiguration {

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

  static {
    OBJECT_MAPPER.registerSubtypes(new NamedType(RemoteKonnetorConfiguration.class, "remote"));
    OBJECT_MAPPER.registerSubtypes(new NamedType(LocalKonnektorConfiguration.class, "local"));
  }

  private static final String CONFIG_YAML = "config.yaml";

  private static KonnektorModuleConfiguration instance;

  private List<KonnektorConfiguration> konnektors;

  public KonnektorConfiguration getKonnektorConfiguration(@NonNull String name) {
    return konnektors.stream()
        .filter(konnektor -> konnektor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new MissingKonnektorKonfigurationException(name));
  }

  public Konnektor instantiateKonnektorClient(String name) {
    val cfg = getKonnektorConfiguration(name);
    return cfg.create();
  }

  @SneakyThrows
  public static KonnektorModuleConfiguration getInstance() {
    if (instance == null) {
      val is = KonnektorModuleConfiguration.class.getClassLoader().getResourceAsStream(CONFIG_YAML);
      instance = OBJECT_MAPPER.readValue(is, KonnektorModuleConfiguration.class);
    }

    return instance;
  }

  @SneakyThrows
  public static KonnektorModuleConfiguration getInstance(File ymlFile) {
    return OBJECT_MAPPER.readValue(ymlFile, KonnektorModuleConfiguration.class);
  }

  @SneakyThrows
  public static KonnektorModuleConfiguration getInstance(String ymlContent) {
    return OBJECT_MAPPER.readValue(ymlContent, KonnektorModuleConfiguration.class);
  }
}
