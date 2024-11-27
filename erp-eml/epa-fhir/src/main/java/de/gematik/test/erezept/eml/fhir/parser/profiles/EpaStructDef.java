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

package de.gematik.test.erezept.eml.fhir.parser.profiles;

import de.gematik.bbriccs.fhir.coding.ProfileStructureDefinition;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum EpaStructDef implements ProfileStructureDefinition<EpaVersion> {
  RX_PRESCRIPTION_ID(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/rx-prescription-process-identifier-extension"),
  EPA_MEDICATION("https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication"),
  EPA_MEDICATION_REQUEST(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-request"),
  EPA_MEDICATION_DISPENSE(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-medication-dispense"),
  EPA_OP_PROVIDE_DISPENSATION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-provide-dispensation-erp-input-parameters"),
  EPA_OP_CANCEL_DISPENSATION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-cancel-dispensation-erp-input-parameters"),
  EPA_OP_PROVIDE_PRESCRIPTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-provide-prescription-erp-input-parameters"),
  EPA_OP_CANCEL_PRESCRIPTION(
      "https://gematik.de/fhir/epa-medication/StructureDefinition/epa-op-cancel-prescription-erp-input-parameters"),
  ;

  private final String canonicalUrl;
}
