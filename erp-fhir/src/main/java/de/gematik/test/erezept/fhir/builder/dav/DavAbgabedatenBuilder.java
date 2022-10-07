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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.extensions.dav.InvoiceId;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.dav.DavDispensedMedication;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class DavAbgabedatenBuilder extends AbstractResourceBuilder<DavAbgabedatenBuilder> {

  private static final String BASE_URL = "urn:uuid:";

  private final PrescriptionId prescriptionId;
  private final DavCompositionBuilder compositionBuilder;

  private PharmacyOrganization pharmacy;
  private DavDispensedMedication medication;
  private DavInvoice invoice;

  private DavAbgabedatenBuilder(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    this.compositionBuilder = DavCompositionBuilder.builder();
  }

  public static DavAbgabedatenBuilder builder(KbvErpBundle bundle) {
    return builder(bundle.getPrescriptionId());
  }

  public static DavAbgabedatenBuilder builder(PrescriptionId prescriptionId) {
    return new DavAbgabedatenBuilder(prescriptionId);
  }

  public DavAbgabedatenBuilder pharmacy(PharmacyOrganization pharmacy) {
    this.pharmacy = pharmacy;
    return self();
  }

  public DavAbgabedatenBuilder medication(DavDispensedMedication medication) {
    this.medication = medication;
    return self();
  }

  public DavAbgabedatenBuilder invoice(DavInvoice invoice) {
    this.invoice = invoice;
    return self();
  }

  public DavAbgabedatenBundle build() {
    val dav = new DavAbgabedatenBundle();

    val profile = ErpStructureDefinition.DAV_ABGABEDATENSATZ.asCanonicalType();
    val meta = new Meta().setLastUpdated(new Date()).setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    dav.setType(Bundle.BundleType.DOCUMENT);
    dav.setTimestamp(new Date());
    dav.setId(this.getResourceId()).setMeta(meta);

    dav.setIdentifier(prescriptionId.asIdentifier());

    val pharmacyEntry = this.createBundleEntryFrom(this.pharmacy, compositionBuilder::pharmacy);
    dav.addEntry(pharmacyEntry);

    val invoiceEntry = this.createBundleEntryFrom(this.invoice);
    dav.addEntry(invoiceEntry);

    val medicationEntry = this.createMedicationEntry(this.medication);
    dav.addEntry(medicationEntry);

    dav.getEntry().add(0, buildCompositionEntry());

    return dav;
  }

  /**
   * The Medication contains a reference to the invoice which is not completely known beforehand.
   * The prefix of the full URL is set here and thus needs to be "fixed". The same issue applies to
   * the performer of the Medication
   *
   * @param medication the medication which shall be used to create the BundleEntryComponent
   * @return the BundleEntryComponent
   */
  private Bundle.BundleEntryComponent createMedicationEntry(DavDispensedMedication medication) {
    val mext = medication.getExtensionByUrl(InvoiceId.STRUCTURE_DEFINITION.getCanonicalUrl());
    val plainInvoiceId = mext.getValue().castToReference(mext.getValue()).getReference();
    mext.setValue(new Reference(createFullUrl(plainInvoiceId)));

    val plainPerformerId = medication.getPerformerFirstRep().getActor().getReference();
    medication.getPerformerFirstRep().getActor().setReference(createFullUrl(plainPerformerId));

    return this.createBundleEntryFrom(this.medication, compositionBuilder::medication);
  }

  private Bundle.BundleEntryComponent createBundleEntryFrom(
      Resource resource, Consumer<String> consumer) {
    val id = resource.getId();
    val fullUrl = createFullUrl(id);
    consumer.accept(fullUrl);
    return createBundleEntryFrom(resource);
  }

  private Bundle.BundleEntryComponent createBundleEntryFrom(Resource resource) {
    val id = resource.getId();
    val fullUrl = createFullUrl(id);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  private String createFullUrl(String id) {
    return BASE_URL + id;
  }

  private Bundle.BundleEntryComponent buildCompositionEntry() {
    val composition = compositionBuilder.status(Composition.CompositionStatus.FINAL).build();
    val compositionUrl = createFullUrl(composition.getId());
    return new Bundle.BundleEntryComponent().setResource(composition).setFullUrl(compositionUrl);
  }
}
