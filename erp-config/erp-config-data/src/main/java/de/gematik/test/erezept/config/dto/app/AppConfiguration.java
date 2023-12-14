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

package de.gematik.test.erezept.config.dto.app;

import de.gematik.test.erezept.config.dto.INamedConfigurationElement;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class AppConfiguration implements INamedConfigurationElement {

  private String platform;
  private String appFile;
  private String espressoServerUniqueName;
  private String packageName;

  @Override
  public String getName() {
    // Note: temporary workaround to satisfy the INamedConfigurationElement
    // in the future we might require multiple AppConfigurations for a single platform
    return platform;
  }
}
