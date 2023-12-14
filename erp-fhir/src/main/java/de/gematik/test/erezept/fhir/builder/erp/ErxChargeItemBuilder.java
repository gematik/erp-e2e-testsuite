/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.PatientenrechnungStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.Hl7CodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.references.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.references.erp.ErxReceiptReference;
import de.gematik.test.erezept.fhir.references.kbv.KbvBundleReference;
import de.gematik.test.erezept.fhir.resources.dav.DavAbgabedatenBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.resources.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.*;
import java.util.function.Function;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

public class ErxChargeItemBuilder extends AbstractResourceBuilder<ErxChargeItemBuilder> {

  private final PrescriptionId prescriptionId;
  private PatientenrechnungVersion version;
  private Reference receiptReference;
  private AccessCode accessCode;
  private ChargeItem.ChargeItemStatus status = ChargeItem.ChargeItemStatus.BILLABLE;
  private KVNR kvnr;
  private String kvnrAssignerName;
  private TelematikID telematikId;
  private AbgabedatensatzReference davReference;
  private byte[] signedDavBundle;
  private Reference kbvReference;
  private MarkingFlag markingFlag;

  private DateTimeType enteredDate;

  private ErxChargeItemBuilder(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;

    this.version =
        ErpWorkflowVersion.getDefaultVersion().isEqual("1.1.1")
            ? null
            : PatientenrechnungVersion.V1_0_0;
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

  public ErxChargeItemBuilder subject(KVNR kvnr, String kvnrAssignerName) {
    this.kvnr = kvnr;
    this.kvnrAssignerName = kvnrAssignerName;
    return self();
  }

  public ErxChargeItemBuilder receipt(ErxReceipt receipt) {
    val ref = new ErxReceiptReference(receipt);
    return receiptReference(ref);
  }

  public ErxChargeItemBuilder receiptReference(ErxReceiptReference reference) {
    this.receiptReference = reference.asReference();
    return this;
  }

  public ErxChargeItemBuilder enterer(String telematikId) {
    return enterer(TelematikID.from(telematikId));
  }

  public ErxChargeItemBuilder enterer(TelematikID telematikId) {
    this.telematikId = telematikId;
    return self();
  }

  public ErxChargeItemBuilder entered(Date date) {
    return entered(date, TemporalPrecisionEnum.SECOND);
  }

  public ErxChargeItemBuilder entered(Date date, TemporalPrecisionEnum precision) {
    this.enteredDate = new DateTimeType(date, precision);
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
   * @param id referencing a KBV-ERP-Bundle
   * @return this Builder
   */
  public ErxChargeItemBuilder verordnung(String id) {
    return verordnung(new KbvBundleReference(id));
  }

  public ErxChargeItemBuilder verordnung(KbvBundleReference reference) {
    this.kbvReference = reference.asReference();
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
    chargeItem.setId(this.prescriptionId.getValue()).setMeta(meta);

    if (enteredDate == null) this.entered(new Date());
    chargeItem.setEnteredDateElement(enteredDate);

    chargeItem.setStatus(status);
    chargeItem.setCode(Hl7CodeSystem.DATA_ABSENT.asCodeableConcept("not-applicable"));

    val containedDavBundle =
        new Binary().setContentType("application/pkcs7-mime").setContent(this.signedDavBundle);
    containedDavBundle.setId(davReference.getReference(false));

    if (version != null) {
      val identifiers = new ArrayList<Identifier>(2);
      identifiers.add(prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121));
      Optional.ofNullable(accessCode)
          .ifPresent(
              ac -> identifiers.add(ac.asIdentifier(ErpWorkflowNamingSystem.ACCESS_CODE_121)));

      chargeItem.setIdentifier(identifiers);
      containedDavBundle
          .getMeta()
          .addProfile(
              ErpWorkflowStructDef.BINARY_12.getVersionedUrl(ErpWorkflowVersion.V1_2_0, true));

      val kvnrIdentifier = kvnr.asIdentifier(DeBasisNamingSystem.KVID_PKV);
      kvnrIdentifier.getAssigner().setDisplay(kvnrAssignerName);
      chargeItem.setSubject(new Reference().setIdentifier(kvnrIdentifier));

      Optional.ofNullable(telematikId)
          .ifPresent(
              tid ->
                  chargeItem.setEnterer(tid.asReference(ErpWorkflowNamingSystem.TELEMATIK_ID_SID)));
      Optional.ofNullable(markingFlag)
          .ifPresent(mf -> chargeItem.setExtension(List.of(mf.asExtension(version))));

      this.fixSupportingInformationReferences();
    } else {
      chargeItem.setIdentifier(List.of(prescriptionId.asIdentifier()));

      containedDavBundle
          .getMeta()
          .addProfile(ErpWorkflowStructDef.BINARY.getVersionedUrl(ErpWorkflowVersion.V1_1_1));

      val kvnrIdentifier = kvnr.asIdentifier();
      kvnrIdentifier.getAssigner().setDisplay(kvnrAssignerName);
      chargeItem.setSubject(new Reference().setIdentifier(kvnrIdentifier));
      Optional.ofNullable(telematikId).ifPresent(tid -> chargeItem.setEnterer(tid.asReference()));
      Optional.ofNullable(markingFlag)
          .ifPresent(mf -> chargeItem.setExtension(List.of(mf.asExtension())));
    }

    chargeItem.addContained((Resource) containedDavBundle);
    chargeItem.addSupportingInformation(receiptReference);
    chargeItem.addSupportingInformation(kbvReference);
    chargeItem.addSupportingInformation(davReference);

    return chargeItem;
  }

  /**
   * These fixes on references are required because supporting information on newer profiles look
   * differently
   */
  private void fixSupportingInformationReferences() {
    if (kbvReference != null) {
      kbvReference.setDisplay(kbvReference.getType()).setType(null);
      if (!kbvReference.getReference().startsWith("Bundle/")) {
        kbvReference.setReference(format("Bundle/{0}", kbvReference.getReference()));
      }
    }

    if (davReference != null) {
      davReference.setDisplay(davReference.getType()).setType(null);
    }
  }

  public static ErxChargeItemBuilder faker(PrescriptionId prescriptionId) {
    val b = forPrescription(prescriptionId);
    b.subject(de.gematik.test.erezept.fhir.values.KVNR.random(), GemFaker.insuranceName())
        .enterer(GemFaker.fakerTelematikId())
        .markingFlag(true, false, false)
        .accessCode(AccessCode.random())
        .verordnung(UUID.randomUUID().toString())
        .abgabedatensatz(b.getResourceId(), "faked binary content".getBytes());
    return b;
  }

  public static ErxChargeItemBuilder forPrescription(PrescriptionId prescriptionId) {
    return new ErxChargeItemBuilder(prescriptionId);
  }

}
