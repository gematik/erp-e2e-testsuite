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

package de.gematik.test.erezept.config.dto.actor;

import de.gematik.test.erezept.config.dto.INamedConfigurationElement;
import lombok.Data;

@Data
public abstract class BaseActorConfiguration implements INamedConfigurationElement {

  /** Just an identifier to distinguish from other clients within config.yaml */
  private String name;

  /** which algorithm to use for the Smartcards */
  private String algorithm = "ECC";

  private String acceptMime = "application/fhir+xml";
  private String sendMime = "application/fhir+xml";
  private String acceptCharset = "utf-8";

  private boolean validateRequest = false;
  private boolean validateResponse = true;
}
