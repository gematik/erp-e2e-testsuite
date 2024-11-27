/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KbvItaForStructDef implements IStructureDefinition<KbvItaForVersion> {
  PRACTITIONER("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Practitioner"),
  PRACTITIONER_ROLE("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_PractitionerRole"),
  ORGANIZATION("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Organization"),
  COVERAGE("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage"),
  PATIENT("https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Patient"),
  BASIS("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Legal_basis"),
  TARIFF("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_PKV_Tariff"),
  STATUS_CO_PAYMENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_StatusCoPayment"),
  ALTERNATIVE_IK("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Alternative_IK"),
  ACCIDENT("https://fhir.kbv.de/StructureDefinition/KBV_EX_FOR_Accident"),
  ;

  private final String canonicalUrl;
}
