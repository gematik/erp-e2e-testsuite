/*
 * Copyright 2025 gematik GmbH
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

package de.gematik.test.erezept.config.dto.actor;

import de.gematik.test.erezept.config.dto.INamedConfigurationElement;
import lombok.Data;

@Data
public class ApothecaryConfiguration implements INamedConfigurationElement {

  private String name;
  private String description = "Ein Apotheker der QES-Signaturen mittels seines HBA erstellen kann";
  private String hbaIccsn;
  private String algorithm = "ECC";
  private String konnektor;
}
