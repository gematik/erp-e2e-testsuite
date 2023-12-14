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

package de.gematik.test.erezept.config.dto;

import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;

import java.util.List;

/**
 * This base class is required to mark BaseConfigurationWrappers (and restrict for the
 * ConfigurationReader) which themselves wrap a DTO configuration and can enrich these with further
 * functionality
 */
public abstract class ConfiguredFactory {

  protected <T extends INamedConfigurationElement> T getConfig(String name, List<T> configs) {
    return configs.stream()
        .filter(actor -> actor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new ConfigurationMappingException(name, configs));
  }
}
