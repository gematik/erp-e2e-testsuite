/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.konnektor.cfg;

import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorModuleConfigurationDto;
import de.gematik.test.konnektor.*;
import lombok.*;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/** Configured Factory for the Konnektor-Client Module */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class KonnektorModuleFactory extends ConfiguredFactory {

  @Delegate
  private final KonnektorModuleConfigurationDto dto;

  public KonnektorConfiguration getKonnektorConfiguration(String name) {
    return this.getConfig(name, dto.getKonnektors());
  }

  public Konnektor createKonnektorClient(String name) {
    val cfg = getKonnektorConfiguration(name);
    return KonnektorFactory.createKonnektor(cfg);
  }

  public Konnektor createSoftKon() {
    return KonnektorFactory.createSoftKon();
  }

  public static KonnektorModuleFactory fromKonnektorConfigs(List<KonnektorConfiguration> konnektors) {
    val dto = new KonnektorModuleConfigurationDto();
    dto.setKonnektors(konnektors);
    return fromDto(dto);
  }

  public static KonnektorModuleFactory fromDto(KonnektorModuleConfigurationDto dto) {
    return new KonnektorModuleFactory(dto);
  }
}
