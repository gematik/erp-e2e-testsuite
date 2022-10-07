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

package de.gematik.test.erezept.fhir.resources.dav;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.parser.profiles.StructureDefinitionFixedUrls;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.values.IKNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle", profile = StructureDefinitionFixedUrls.DAV_PKV_PR_ERP_ABGABEDATEN)
@SuppressWarnings({"java:S110"})
public class DavAbgabedatenBundle extends Bundle {

  public String getLogicalId() {
    return this.id.getValue();
  }

  public AbgabedatensatzReference getReference() {
    return new AbgabedatensatzReference(this.getLogicalId());
  }

  public PrescriptionId getPrescriptionId() {
    if (!this.getIdentifier().getSystem().equals(PrescriptionId.NAMING_SYSTEM.getCanonicalUrl())) {
      throw new MissingFieldException(DavAbgabedatenBundle.class, PrescriptionId.NAMING_SYSTEM);
    }
    return new PrescriptionId(this.getIdentifier().getValue());
  }

  public PrescriptionFlowType getFlowType() {
    return PrescriptionFlowType.fromPrescriptionId(this.getPrescriptionId());
  }

  public PharmacyOrganization getPharmacy() {
    // first, find all organizations within the KBV Bundle
    val organizations = getOrganizations();

    // now filter for Organizations which have a IKNR
    return organizations.stream()
        .filter(
            org ->
                org.getIdentifier().stream()
                    .anyMatch(
                        identifier ->
                            identifier.getSystem().equals(IKNR.getSystem().getCanonicalUrl())))
        .map(PharmacyOrganization::fromOrganization)
        .findAny()
        .orElseThrow(() -> new MissingFieldException(PharmacyOrganization.class, IKNR.getSystem()));
  }

  public DavDispensedMedication getDispensedMedication() {
    return this.entry.stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.MedicationDispense))
        .map(DavDispensedMedication::fromMedicationDispense)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    DavDispensedMedication.class, ErpStructureDefinition.DAV_ABGABEINFORMATIONEN));
  }

  public DavInvoice getInvoice() {
    return this.entry.stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Invoice))
        .map(DavInvoice::fromInvoice)
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    DavDispensedMedication.class, ErpStructureDefinition.DAV_INVOICE));
  }

  private List<Organization> getOrganizations() {
    return this.entry.stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Organization))
        .map(Organization.class::cast)
        .collect(Collectors.toList());
  }
}
