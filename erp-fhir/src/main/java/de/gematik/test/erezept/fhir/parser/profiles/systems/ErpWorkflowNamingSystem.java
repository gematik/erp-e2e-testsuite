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

import de.gematik.test.erezept.fhir.parser.profiles.INamingSystem;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErpWorkflowNamingSystem implements INamingSystem {
  PRESCRIPTION_ID("https://gematik.de/fhir/NamingSystem/PrescriptionID"),
  PRESCRIPTION_ID_121(
      "https://gematik.de/fhir/erp/NamingSystem/GEM_ERP_NS_PrescriptionId"), // new NamingSystem
  ACCESS_CODE("https://gematik.de/fhir/NamingSystem/AccessCode"),
  SECRET("https://gematik.de/fhir/NamingSystem/Secret"),
  TELEMATIK_ID("https://gematik.de/fhir/NamingSystem/TelematikID"),
  // NOTE will be resolved/merged with TELEMATIK_ID later!
  TELEMATIK_ID_SID("https://gematik.de/fhir/sid/telematik-id"),
  ;

  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
