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

package de.gematik.test.erezept.fhir.builder.erp;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.BuilderUtil;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.references.erp.ErxReceiptReference;
import de.gematik.test.erezept.fhir.references.kbv.KbvBundleReference;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVID;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.Date;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Meta;

public class ErxChargeItemBuilder extends AbstractResourceBuilder<ErxChargeItemBuilder> {

  private final Identifier identifier;
  private final ErxReceiptReference receiptReference;

  private ChargeItem.ChargeItemStatus status = ChargeItem.ChargeItemStatus.BILLABLE;
  private KVID kvid;
  private TelematikID telematikId;
  private AbgabedatensatzReference davReference;
  private KbvBundleReference kbvReference;
  private boolean insuranceProvider = false;
  private boolean subsidy = false;
  private boolean taxOffice = false;

  private ErxChargeItemBuilder(PrescriptionId prescriptionId) {
    this.identifier = prescriptionId.asIdentifier();
    this.receiptReference = new ErxReceiptReference(prescriptionId);
  }

  public static ErxChargeItemBuilder faker(PrescriptionId prescriptionId) {
    val b = forPrescription(prescriptionId);
    b.subject(GemFaker.fakerKvid())
        .enterer(GemFaker.fakerTelematikId())
        .markingFlag(true, false, false)
        .verordnung(IdType.newRandomUuid().getId())
        .abgabedatensatz(IdType.newRandomUuid().getId());
    return b;
  }

  public static ErxChargeItemBuilder forPrescription(PrescriptionId prescriptionId) {
    return new ErxChargeItemBuilder(prescriptionId);
  }

  public ErxChargeItemBuilder status(@NonNull final String status) {
    return status(ChargeItem.ChargeItemStatus.fromCode(status.toLowerCase()));
  }

  public ErxChargeItemBuilder status(ChargeItem.ChargeItemStatus status) {
    this.status = status;
    return self();
  }

  public ErxChargeItemBuilder subject(String kvid) {
    return subject(KVID.from(kvid));
  }

  public ErxChargeItemBuilder subject(KVID kvid) {
    this.kvid = kvid;
    return self();
  }

  public ErxChargeItemBuilder enterer(String telematikId) {
    return enterer(TelematikID.from(telematikId));
  }

  public ErxChargeItemBuilder enterer(TelematikID telematikId) {
    this.telematikId = telematikId;
    return self();
  }

  public ErxChargeItemBuilder insuranceProvider(boolean insuranceProvider) {
    this.insuranceProvider = insuranceProvider;
    return self();
  }

  public ErxChargeItemBuilder subsidy(boolean subsidy) {
    this.subsidy = subsidy;
    return self();
  }

  public ErxChargeItemBuilder taxOffice(boolean taxOffice) {
    this.taxOffice = taxOffice;
    return self();
  }

  public ErxChargeItemBuilder markingFlag(
      boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return insuranceProvider(insuranceProvider).subsidy(subsidy).taxOffice(taxOffice);
  }

  /**
   * @deprecated use the KbvErpBundle directly if you have one or create a KbvBundleReference from
   *     your ID
   * @param id referencing a KBV-ERP-Bundle
   * @return this Builder
   */
  @Deprecated(forRemoval = true)
  public ErxChargeItemBuilder verordnung(String id) {
    return verordnung(new KbvBundleReference(id));
  }

  public ErxChargeItemBuilder verordnung(KbvBundleReference reference) {
    this.kbvReference = reference;
    return self();
  }

  public ErxChargeItemBuilder verordnung(KbvErpBundle bundle) {
    return verordnung(bundle.getReference());
  }

  /**
   * @deprecated use the DavAbgabedatenBundle directly if you have one or create a
   *     AbgabedatensatzReference from your ID
   * @param id referencing a DAV-Abgabedatensatz
   * @return this Builder
   */
  @Deprecated(forRemoval = true)
  public ErxChargeItemBuilder abgabedatensatz(String id) {
    return abgabedatensatz(new AbgabedatensatzReference(id));
  }

  public ErxChargeItemBuilder abgabedatensatz(AbgabedatensatzReference reference) {
    this.davReference = reference;
    return self();
  }

  public ErxChargeItemBuilder abgabedatensatz(DavAbgabedatenBundle bundle) {
    return abgabedatensatz(bundle.getReference());
  }

  public ErxChargeItem build() {
    val chargeItem = new ErxChargeItem();

    val profile = ErpStructureDefinition.GEM_CHARGE_ITEM.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    chargeItem.setMeta(meta);

    chargeItem.setIdentifier(List.of(identifier));
    chargeItem.setStatus(status);
    chargeItem.setSubject(kvid.asReference());
    chargeItem.setEnterer(telematikId.asReference());
    chargeItem.setCode(BuilderUtil.dataAbsent());
    chargeItem.setExtension(
        List.of(BuilderUtil.markingFlag(insuranceProvider, subsidy, taxOffice)));
    chargeItem.setEnteredDate(new Date());

    chargeItem.addSupportingInformation(receiptReference);
    chargeItem.addSupportingInformation(davReference);
    chargeItem.addSupportingInformation(kbvReference);

    return chargeItem;
  }
}
