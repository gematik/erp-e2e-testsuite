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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.profiles.definitions;

import de.gematik.bbriccs.fhir.coding.WithStructureDefinition;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpEuStructDef implements WithStructureDefinition<EuVersion> {
  CONSENT_TYPE("https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_Consent"),
  PATCH_TASK_INPUT(
      "https://gematik.de/fhir/erp-eu/StructureDefinition/GEM_ERPEU_PR_PAR_PATCH_Task_Input");

  private final String canonicalUrl;
}
