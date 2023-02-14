/*
 * Copyright (c) 2023 gematik GmbH
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

import de.gematik.test.erezept.fhir.builder.*;
import de.gematik.test.erezept.fhir.extensions.erp.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.parser.profiles.systems.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.references.dav.*;
import de.gematik.test.erezept.fhir.references.erp.*;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.dav.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public class ErxChargeItemBuilder extends AbstractResourceBuilder<ErxChargeItemBuilder> {

  private PatientenrechnungVersion version;
  private final PrescriptionId prescriptionId;
  private ErxReceiptReference receiptReference;
  private AccessCode accessCode;
  private ChargeItem.ChargeItemStatus status = ChargeItem.ChargeItemStatus.BILLABLE;
  private KVID kvid;
  private String kvidAssignerName;
  private TelematikID telematikId;
  private AbgabedatensatzReference davReference;
  private byte[] signedDavBundle;
  private KbvBundleReference kbvReference;
  private MarkingFlag markingFlag;

  private ErxChargeItemBuilder(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
  }

  public static ErxChargeItemBuilder faker(PrescriptionId prescriptionId) {
    val b = forPrescription(prescriptionId);
    b.subject(GemFaker.fakerKvid(), GemFaker.insuranceName())
        .enterer(GemFaker.fakerTelematikId())
        .markingFlag(true, false, false)
        .verordnung(b.getResourceId())
        .abgabedatensatz(b.getResourceId(), "faked binary content".getBytes());
    return b;
  }

  public static ErxChargeItemBuilder forPrescription(PrescriptionId prescriptionId) {
    return new ErxChargeItemBuilder(prescriptionId);
  }

  public ErxChargeItemBuilder version(PatientenrechnungVersion version) {
    this.version = version;
    return self();
  }

  public ErxChargeItemBuilder accessCode(String accessCode) {
    return accessCode(new AccessCode(accessCode));
  }

  public ErxChargeItemBuilder accessCode(AccessCode accessCode) {
    this.accessCode = accessCode;
    return self();
  }

  public ErxChargeItemBuilder status(@NonNull final String status) {
    return status(ChargeItem.ChargeItemStatus.fromCode(status.toLowerCase()));
  }

  public ErxChargeItemBuilder status(ChargeItem.ChargeItemStatus status) {
    this.status = status;
    return self();
  }

  public ErxChargeItemBuilder subject(String kvid, String kvidAssignerName) {
    return subject(KVID.from(kvid), kvidAssignerName);
  }

  public ErxChargeItemBuilder receipt(ErxReceipt receipt) {
    val ref = new ErxReceiptReference(receipt);
    return receiptReference(ref);
  }

  public ErxChargeItemBuilder receiptReference(ErxReceiptReference reference) {
    this.receiptReference = reference;
    return this;
  }

  public ErxChargeItemBuilder subject(KVID kvid, String kvidAssignerName) {
    this.kvid = kvid;
    this.kvidAssignerName = kvidAssignerName;
    return self();
  }

  public ErxChargeItemBuilder enterer(String telematikId) {
    return enterer(TelematikID.from(telematikId));
  }

  public ErxChargeItemBuilder enterer(TelematikID telematikId) {
    this.telematikId = telematikId;
    return self();
  }

  public ErxChargeItemBuilder markingFlag(
      boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return markingFlag(MarkingFlag.with(insuranceProvider, subsidy, taxOffice));
  }

  public ErxChargeItemBuilder markingFlag(MarkingFlag markingFlag) {
    this.markingFlag = markingFlag;
    return self();
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

  public ErxChargeItemBuilder abgabedatensatz(
      DavAbgabedatenBundle bundle, Function<DavAbgabedatenBundle, byte[]> signer) {
    return abgabedatensatz(bundle.getReference(), signer.apply(bundle));
  }

  public ErxChargeItemBuilder abgabedatensatz(String id, byte[] signed) {
    return abgabedatensatz(new AbgabedatensatzReference(id), signed);
  }

  public ErxChargeItemBuilder abgabedatensatz(AbgabedatensatzReference reference, byte[] signed) {
    this.davReference = reference;
    this.davReference.makeContained();
    this.signedDavBundle = signed;
    return self();
  }

  public ErxChargeItem build() {
    val chargeItem = new ErxChargeItem();

    CanonicalType profile;
    if (version != null) {
      profile = PatientenrechnungStructDef.CHARGE_ITEM.asCanonicalType(version, true);
    } else {
      profile = ErpWorkflowStructDef.CHARGE_ITEM.asCanonicalType();
    }
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    chargeItem.setMeta(meta);
    chargeItem.setEnteredDate(new Date());

    chargeItem.setStatus(status);
    chargeItem.setCode(BuilderUtil.dataAbsent());

    val containedDavBundle =
        new Binary().setContentType("application/pkcs7-mime").setContent(this.signedDavBundle);
    containedDavBundle.setId(davReference.getReference(false));

    if (version != null) {
      val identifiers =
          List.of(
              prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121),
              accessCode.asIdentifier(ErpWorkflowNamingSystem.ACCESS_CODE_121));
      chargeItem.setIdentifier(identifiers);

      containedDavBundle
          .getMeta()
          .addProfile(
              ErpWorkflowStructDef.BINARY_12.getVersionedUrl(ErpWorkflowVersion.V1_2_0, true));

      val kvidIdentifier = kvid.asIdentifier(DeBasisNamingSystem.KVID_PKV);
      kvidIdentifier.getAssigner().setDisplay(kvidAssignerName);
      chargeItem.setSubject(new Reference().setIdentifier(kvidIdentifier));
      chargeItem.setEnterer(telematikId.asReference(ErpWorkflowNamingSystem.TELEMATIK_ID_SID));
      chargeItem.setExtension(List.of(markingFlag.asExtension(version)));
    } else {
      chargeItem.setIdentifier(List.of(prescriptionId.asIdentifier()));

      containedDavBundle
          .getMeta()
          .addProfile(ErpWorkflowStructDef.BINARY.getVersionedUrl(ErpWorkflowVersion.V1_1_1));

      val kvidIdentifier = kvid.asIdentifier();
      kvidIdentifier.getAssigner().setDisplay(kvidAssignerName);
      chargeItem.setSubject(new Reference().setIdentifier(kvidIdentifier));
      chargeItem.setEnterer(telematikId.asReference());
      chargeItem.setExtension(List.of(markingFlag.asExtension()));
    }

    chargeItem.addContained((Resource) containedDavBundle);
    chargeItem.addSupportingInformation(receiptReference);
    chargeItem.addSupportingInformation(davReference);
    chargeItem.addSupportingInformation(kbvReference);

    return chargeItem;
  }
}
