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

package de.gematik.test.erezept.fhir.resources.dav;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Organization;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class DavAbgabedatenBundle extends Bundle implements ErpFhirResource {

  public String getLogicalId() {
    return IdentifierUtil.getUnqualifiedId(this.id);
  }

  public AbgabedatensatzReference getReference() {
    return new AbgabedatensatzReference(this.getLogicalId());
  }

  public PrescriptionId getPrescriptionId() {
    if (!this.getIdentifier().getSystem().equals(PrescriptionId.NAMING_SYSTEM.getCanonicalUrl())
        && !this.getIdentifier()
            .getSystem()
            .equals(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121.getCanonicalUrl())) {
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
                            identifier
                                    .getSystem()
                                    .equals(DeBasisNamingSystem.IKNR.getCanonicalUrl())
                                || identifier
                                    .getSystem()
                                    .equals(DeBasisNamingSystem.IKNR_SID.getCanonicalUrl())))
        .map(PharmacyOrganization::fromOrganization)
        .findAny()
        .orElseThrow(
            () -> new MissingFieldException(PharmacyOrganization.class, DeBasisNamingSystem.IKNR));
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
                    DavDispensedMedication.class, AbdaErpPkvStructDef.PKV_ABGABEINFORMATIONEN));
  }

  public DavInvoice getInvoice() {
    val davInvoice =
        this.entry.stream()
            .filter(
                resource -> resource.getResource().getResourceType().equals(ResourceType.Invoice))
            .findFirst()
            .orElseThrow(
                () ->
                    new MissingFieldException(
                        this.getClass(), AbdaErpPkvStructDef.PKV_ABRECHNUNGSZEILEN));

    DavInvoice ret;
    if (!davInvoice.getResource().getClass().equals(DavInvoice.class)) {
      ret = DavInvoice.fromInvoice(davInvoice.getResource());
      davInvoice.setResource(ret);
    } else {
      ret = (DavInvoice) davInvoice.getResource();
    }

    return ret;
  }

  private List<Organization> getOrganizations() {
    return this.entry.stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Organization))
        .map(Organization.class::cast)
        .toList();
  }

  public static DavAbgabedatenBundle fromBundle(Bundle adaptee) {
    val davBundle = new DavAbgabedatenBundle();
    adaptee.copyValues(davBundle);
    return davBundle;
  }

  public static DavAbgabedatenBundle fromBundle(Resource adaptee) {
    return fromBundle((Bundle) adaptee);
  }

  @Override
  public String getDescription() {
    val workflow = this.getFlowType();
    val type = format("{0} Abgabedatensatz", workflow.getDisplay());
    val prescriptionId = this.getPrescriptionId().getValue();
    val pzn = this.getInvoice().getPzn();
    val totalPrice = this.getInvoice().getTotalPrice();
    val insurantPrice = this.getInvoice().getTotalCoPayment();
    val currency = this.getInvoice().getCurrency();
    val pharmacyName = this.getPharmacy().getName();
    val vat = this.getInvoice().getVAT();
    return format(
        "{0} für das E-Rezept {1} mit der PZN {2} im Gesamtwert von {3} {6} (Selbstbeteiligung: {4}"
            + " {6}) inkl. MwSt {7}% durch die {5}",
        type, prescriptionId, pzn, totalPrice, insurantPrice, pharmacyName, currency, vat);
  }
}
