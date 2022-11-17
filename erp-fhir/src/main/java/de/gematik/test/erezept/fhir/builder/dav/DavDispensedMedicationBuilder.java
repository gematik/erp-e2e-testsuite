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

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.BuilderUtil;
import de.gematik.test.erezept.fhir.extensions.dav.InvoiceId;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.AbdaErpPkvStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.AbdaErpPkvVersion;
import de.gematik.test.erezept.fhir.resources.dav.DavDispensedMedication;
import de.gematik.test.erezept.fhir.resources.dav.DavInvoice;
import de.gematik.test.erezept.fhir.resources.dav.PharmacyOrganization;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.dav.AbrechnungsTyp;
import de.gematik.test.erezept.fhir.valuesets.dav.MedicationDispenseType;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.DateTimeType;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Meta;

public class DavDispensedMedicationBuilder
    extends AbstractResourceBuilder<DavDispensedMedicationBuilder> {

  private AbdaErpPkvVersion abdaErpPkvVersion = AbdaErpPkvVersion.getDefaultVersion();

  private final MedicationDispenseType dispenseType;
  private final AbrechnungsTyp type;
  private Date whenHandedOver;
  private MedicationDispense.MedicationDispenseStatus status;

  // required fields without default values!
  private PrescriptionId authorizingPrescription;
  private String performerId;
  private InvoiceId invoiceId;

  private DavDispensedMedicationBuilder(MedicationDispenseType dispenseType, AbrechnungsTyp type) {
    this.dispenseType = dispenseType;
    this.type = type;
    this.whenHandedOver = new Date(); // default value
    this.status = MedicationDispense.MedicationDispenseStatus.COMPLETED;
  }

  public static DavDispensedMedicationBuilder builder() {
    return builder(MedicationDispenseType.ABGABE, AbrechnungsTyp.STANDARD);
  }

  public static DavDispensedMedicationBuilder builder(
      MedicationDispenseType dispenseType, AbrechnungsTyp type) {
    return new DavDispensedMedicationBuilder(dispenseType, type);
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public DavDispensedMedicationBuilder version(AbdaErpPkvVersion version) {
    this.abdaErpPkvVersion = version;
    return this;
  }

  public DavDispensedMedicationBuilder status(@NonNull String status) {
    return status(MedicationDispense.MedicationDispenseStatus.fromCode(status));
  }

  public DavDispensedMedicationBuilder status(MedicationDispense.MedicationDispenseStatus status) {
    this.status = status;
    return self();
  }

  public DavDispensedMedicationBuilder whenHandedOver(@NonNull Date whenHandedOver) {
    this.whenHandedOver = whenHandedOver;
    return self();
  }

  public DavDispensedMedicationBuilder prescription(PrescriptionId prescriptionId) {
    this.authorizingPrescription = prescriptionId;
    return self();
  }

  public DavDispensedMedicationBuilder pharmacy(PharmacyOrganization pharmacy) {
    return pharmacy(pharmacy.getId());
  }

  public DavDispensedMedicationBuilder pharmacy(@NonNull String pharmacyId) {
    this.performerId = pharmacyId;
    return self();
  }

  public DavDispensedMedicationBuilder invoice(@NonNull DavInvoice invoice) {
    return invoice(invoice.getInvoiceId());
  }

  public DavDispensedMedicationBuilder invoice(@NonNull String invoiceId) {
    return invoice(InvoiceId.fromId(invoiceId));
  }

  public DavDispensedMedicationBuilder invoice(@NonNull InvoiceId invoiceId) {
    this.invoiceId = invoiceId;
    return self();
  }

  public DavDispensedMedication build() {
    val dm = new DavDispensedMedication();

    val profile =
        AbdaErpPkvStructDef.PKV_ABGABEINFORMATIONEN.asCanonicalType(abdaErpPkvVersion, true);
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    dm.setId(this.getResourceId());
    dm.setMeta(meta);

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
    dm.setMedication(BuilderUtil.dataAbsent());

    dm.addExtension(invoiceId);

    return dm;
  }
}
