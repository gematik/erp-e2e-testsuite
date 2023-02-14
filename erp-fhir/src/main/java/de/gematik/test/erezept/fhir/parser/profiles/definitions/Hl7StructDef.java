/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.Hl7Version;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Hl7StructDef implements IStructureDefinition<Hl7Version> {
  HOUSE_NUMBER("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-houseNumber"),
  STREET_NAME("http://hl7.org/fhir/StructureDefinition/iso21090-ADXP-streetName"),
  HUMAN_OWN_NAME("http://hl7.org/fhir/StructureDefinition/humanname-own-name"),
  HUMAN_OWN_PREFIX("http://hl7.org/fhir/StructureDefinition/humanname-own-prefix"),
  NAMENSZUSATT("http://fhir.de/StructureDefinition/humanname-namenszusatz"),
  ISO_21090_EN_QUALIFIER("http://hl7.org/fhir/StructureDefinition/iso21090-EN-qualifier"),
  ;

  private final String canonicalUrl;
}
