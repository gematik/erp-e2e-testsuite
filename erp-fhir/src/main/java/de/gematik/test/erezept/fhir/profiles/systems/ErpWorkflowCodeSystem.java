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

package de.gematik.test.erezept.fhir.profiles.systems;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.coding.WithCodeSystem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErpWorkflowCodeSystem implements WithCodeSystem {
  FLOW_TYPE("https://gematik.de/fhir/CodeSystem/Flowtype"),
  FLOW_TYPE_12("https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_FlowType"),
  AVAILABILITY_STATUS("https://gematik.de/fhir/CodeSystem/AvailabilityStatus"),
  AVAILABILITY_STATUS_12("https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_AvailabilityStatus"),
  DOCUMENT_TYPE("https://gematik.de/fhir/CodeSystem/Documenttype"),
  GEM_ERP_CS_DOCUMENT_TYPE("https://gematik.de/fhir/erp/CodeSystem/GEM_ERP_CS_DocumentType"),
  CONSENT_TYPE("https://gematik.de/fhir/CodeSystem/Consenttype"),
  PROFESSION_OID("https://gematik.de/fhir/directory/CodeSystem/OrganizationProfessionOID"),
  ORGANIZATION_PROVIDER_TYPE(
      "https://gematik.de/fhir/directory/CodeSystem/OrganizationProviderType"),
  ORGANIZATION_PROFESSION_OID(
      "https://gematik.de/fhir/directory/CodeSystem/OrganizationProfessionOID"),
  ;
  private final String canonicalUrl;

  @Override
  public String toString() {
    return format("{0}({1})", this.name(), this.canonicalUrl);
  }
}
