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

package de.gematik.test.erezept.fhir.parser.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.ICodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpWorkflowCodeSystem implements ICodeSystem {
  FLOW_TYPE("https://gematik.de/fhir/CodeSystem/Flowtype"),
  AVAILABILITY_STATUS("https://gematik.de/fhir/CodeSystem/AvailabilityStatus"),
  DOCUMENT_TYPE("https://gematik.de/fhir/CodeSystem/Documenttype"),
  CONSENT_TYPE("https://gematik.de/fhir/CodeSystem/Consenttype"),
  ;

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
