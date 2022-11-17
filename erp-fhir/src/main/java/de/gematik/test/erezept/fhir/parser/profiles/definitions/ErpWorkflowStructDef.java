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

package de.gematik.test.erezept.fhir.parser.profiles.definitions;

import de.gematik.test.erezept.fhir.parser.profiles.IStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpWorkflowStructDef implements IStructureDefinition<ErpWorkflowVersion> {
  SUPPLY_OPTIONS_TYPE("https://gematik.de/fhir/StructureDefinition/SupplyOptionsType"),
  AVAILABILITY_STATUS("https://gematik.de/fhir/StructureDefinition/AvailabilityStateExtension"),
  INSURANCE_PROVIDER("https://gematik.de/fhir/StructureDefinition/InsuranceProvider"),
  SUBSTITION_ALLOWED("https://gematik.de/fhir/StructureDefinition/SubstitutionAllowedType"),
  PRESCRIPTION_TYPE("https://gematik.de/fhir/StructureDefinition/PrescriptionType"),
  BINARY("https://gematik.de/fhir/StructureDefinition/ErxBinary"),
  TASK("https://gematik.de/fhir/StructureDefinition/ErxTask"),
  MEDICATION_DISPENSE("https://gematik.de/fhir/StructureDefinition/ErxMedicationDispense"),
  RECEIPT("https://gematik.de/fhir/StructureDefinition/ErxReceipt"),

  CONSENT("https://gematik.de/fhir/StructureDefinition/ErxConsent"),
  MARKING_FLAG("https://gematik.de/fhir/StructureDefinition/MarkingFlag"),
  CHARGE_ITEM("https://gematik.de/fhir/StructureDefinition/ErxChargeItem"),
  COM_INFO_REQ("https://gematik.de/fhir/StructureDefinition/ErxCommunicationInfoReq"),
  COM_DISP_REQ("https://gematik.de/fhir/StructureDefinition/ErxCommunicationDispReq"),
  COM_REPLY("https://gematik.de/fhir/StructureDefinition/ErxCommunicationReply"),
  COM_REPRESENTATIVE("https://gematik.de/fhir/StructureDefinition/ErxCommunicationRepresentative"),

  AUDIT_EVENT("https://gematik.de/fhir/StructureDefinition/ErxAuditEvent"),
  ;

  private final String canonicalUrl;
}
