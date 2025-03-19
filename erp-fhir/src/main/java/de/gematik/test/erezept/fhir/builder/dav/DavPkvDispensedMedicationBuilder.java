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
 */

package de.gematik.test.erezept.fhir.builder.dav;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.HL7CodeSystem;
import de.gematik.test.erezept.fhir.extensions.dav.InvoiceId;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.r4.dav.DavInvoice;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvDispensedMedication;
import de.gematik.test.erezept.fhir.r4.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.dav.AbrechnungsTyp;
import de.gematik.test.erezept.fhir.valuesets.dav.MedicationDispenseType;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationDispense;

public class DavPkvDispensedMedicationBuilder
    extends ResourceBuilder<DavPkvDispensedMedication, DavPkvDispensedMedicationBuilder> {

  private AbdaErpPkvVersion abdaErpPkvVersion = AbdaErpPkvVersion.getDefaultVersion();

  private final MedicationDispenseType dispenseType;
  private final AbrechnungsTyp type;
  private Date whenHandedOver;
  private MedicationDispense.MedicationDispenseStatus status;

  // required fields without default values!
  private PrescriptionId authorizingPrescription;
  private String performerId;
  private InvoiceId invoiceId;

  private DavPkvDispensedMedicationBuilder(
      MedicationDispenseType dispenseType, AbrechnungsTyp type) {
    this.dispenseType = dispenseType;
    this.type = type;
    this.whenHandedOver = new Date(); // default value
    this.status = MedicationDispense.MedicationDispenseStatus.COMPLETED;
  }

  public static DavPkvDispensedMedicationBuilder builder() {
    return builder(MedicationDispenseType.ABGABE, AbrechnungsTyp.STANDARD);
  }

  public static DavPkvDispensedMedicationBuilder builder(
      MedicationDispenseType dispenseType, AbrechnungsTyp type) {
    return new DavPkvDispensedMedicationBuilder(dispenseType, type);
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public DavPkvDispensedMedicationBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    return this;
  }

  public DavPkvDispensedMedicationBuilder status(String status) {
    return status(MedicationDispense.MedicationDispenseStatus.fromCode(status));
  }

  public DavPkvDispensedMedicationBuilder status(
      MedicationDispense.MedicationDispenseStatus status) {
    this.status = status;
    return self();
  }

  public DavPkvDispensedMedicationBuilder whenHandedOver(Date whenHandedOver) {
    this.whenHandedOver = whenHandedOver;
    return self();
  }

  public DavPkvDispensedMedicationBuilder prescription(PrescriptionId prescriptionId) {
    this.authorizingPrescription = prescriptionId;
    return self();
  }

  public DavPkvDispensedMedicationBuilder pharmacy(PharmacyOrganization pharmacy) {
    return pharmacy(pharmacy.getId());
  }

  public DavPkvDispensedMedicationBuilder pharmacy(String pharmacyId) {
    this.performerId = pharmacyId;
    return self();
  }

  public DavPkvDispensedMedicationBuilder invoice(DavInvoice invoice) {
    return invoice(invoice.getInvoiceId());
  }

  public DavPkvDispensedMedicationBuilder invoice(String invoiceId) {
    return invoice(InvoiceId.fromId(invoiceId));
  }

  public DavPkvDispensedMedicationBuilder invoice(InvoiceId invoiceId) {
    this.invoiceId = invoiceId;
    return self();
  }

  @Override
  public DavPkvDispensedMedication build() {
    val dm =
        this.createResource(
            DavPkvDispensedMedication::new,
            AbdaErpPkvStructDef.PKV_ABGABEINFORMATIONEN,
            abdaErpPkvVersion);

    dm.addExtension(
        AbdaErpPkvStructDef.PKV_ABRECHNUNGSTYP.getCanonicalUrl(), type.asCodeableConcept());

    dm.setType(dispenseType.asCodeableConcept());
    dm.setWhenHandedOverElement(new DateTimeType(whenHandedOver, TemporalPrecisionEnum.DAY));
    dm.setStatus(status);
    dm.setAuthorizingPrescription(
        List.of(authorizingPrescription.asReference(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121)));

    val mdp = new MedicationDispense.MedicationDispensePerformerComponent();
    mdp.getActor().setReference(performerId);
    dm.setPerformer(List.of(mdp));
    dm.setMedication(HL7CodeSystem.DATA_ABSENT.asCodeableConcept("not-applicable"));

    dm.addExtension(invoiceId);

    return dm;
  }
}
