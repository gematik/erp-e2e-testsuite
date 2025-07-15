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

package de.gematik.test.erezept.fhir.builder.dav;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpBasisStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvDispensedMedication;
import de.gematik.test.erezept.fhir.r4.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.Resource;

public class DavPkvAbgabedatenBuilder
    extends ResourceBuilder<DavPkvAbgabedatenBundle, DavPkvAbgabedatenBuilder> {

  private static final String BASE_URL = "urn:uuid:";
  private final PrescriptionId prescriptionId;
  private final DavCompositionBuilder compositionBuilder;
  private AbdaErpPkvVersion abdaErpPkvVersion = AbdaErpPkvVersion.getDefaultVersion();
  private PharmacyOrganization pharmacy;
  private DavPkvDispensedMedication medication;
  private DavInvoice invoice;

  private DavPkvAbgabedatenBuilder(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    this.compositionBuilder = DavCompositionBuilder.builder();
  }

  public static DavPkvAbgabedatenBuilder builder(KbvErpBundle bundle) {
    return builder(bundle.getPrescriptionId());
  }

  public static DavPkvAbgabedatenBuilder builder(PrescriptionId prescriptionId) {
    return new DavPkvAbgabedatenBuilder(prescriptionId);
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public DavPkvAbgabedatenBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    this.compositionBuilder.version(version);
    return this;
  }

  public DavPkvAbgabedatenBuilder pharmacy(PharmacyOrganization pharmacy) {
    this.pharmacy = pharmacy;
    return self();
  }

  public DavPkvAbgabedatenBuilder medication(DavPkvDispensedMedication medication) {
    this.medication = medication;
    return self();
  }

  public DavPkvAbgabedatenBuilder invoice(DavInvoice invoice) {
    this.invoice = invoice;
    return self();
  }

  @Override
  public DavPkvAbgabedatenBundle build() {
    val dav =
        this.createResource(
            DavPkvAbgabedatenBundle::new,
            AbdaErpPkvStructDef.PKV_ABGABEDATENSATZ,
            abdaErpPkvVersion);

    dav.getMeta().setLastUpdated(new Date());
    dav.setType(Bundle.BundleType.DOCUMENT);
    dav.setTimestamp(new Date());

    dav.setIdentifier(prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID));

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
  private Bundle.BundleEntryComponent createMedicationEntry(DavPkvDispensedMedication medication) {
    val mext =
        medication.getExtension().stream()
            .filter(AbdaErpBasisStructDef.ABRECHNUNGSZEILEN::matches)
            .findFirst()
            .orElseThrow(
                () ->
                    new MissingFieldException(
                        medication.getClass(), AbdaErpBasisStructDef.ABRECHNUNGSZEILEN));
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
