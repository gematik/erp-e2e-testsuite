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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;

public class ErxChargeItemFaker {
  private final Map<String, Consumer<ErxChargeItemBuilder>> builderConsumers = new HashMap<>();
  private PrescriptionId prescriptionId = fakerPrescriptionId();
  private static final String KEY_ABGABEDATENSATZ =
      "abgabedatensatz"; // key used for builderConsumers map

  private ErxChargeItemFaker() {
    builderConsumers.put("accessCode", b -> b.accessCode(AccessCode.random()));
    builderConsumers.put("subject", b -> b.subject(KVNR.random(), insuranceName()));
    builderConsumers.put("enterer", b -> b.enterer(fakerTelematikId()));
    builderConsumers.put("verordnung", b -> b.verordnung(UUID.randomUUID().toString()));
    builderConsumers.put("markingFlag", b -> b.markingFlag(true, false, false));
  }

  public static ErxChargeItemFaker builder() {
    return new ErxChargeItemFaker();
  }

  public ErxChargeItemFaker withPrescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return this;
  }

  public ErxChargeItemFaker withVersion(PatientenrechnungVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public ErxChargeItemFaker withAccessCode(AccessCode accessCode) {
    builderConsumers.computeIfPresent(
        "accessCode", (key, defaultValue) -> b -> b.accessCode(accessCode));
    return this;
  }

  public ErxChargeItemFaker withAccessCode(String accessCode) {
    return this.withAccessCode(new AccessCode(accessCode));
  }

  public ErxChargeItemFaker withStatus(ChargeItem.ChargeItemStatus status) {
    builderConsumers.put("status", b -> b.status(status));
    return this;
  }

  public ErxChargeItemFaker withStatus(final String status) {
    return this.withStatus(ChargeItem.ChargeItemStatus.fromCode(status.toLowerCase()));
  }

  public ErxChargeItemFaker withSubject(KVNR kvnr, String kvnrAssignerName) {
    builderConsumers.computeIfPresent(
        "subject", (key, defaultValue) -> b -> b.subject(kvnr, kvnrAssignerName));
    return this;
  }

  public ErxChargeItemFaker withReceiptReference(ErxReceiptReference reference) {
    builderConsumers.put("receiptReference", b -> b.receiptReference(reference));
    return this;
  }

  public ErxChargeItemFaker withReceipt(ErxReceipt receipt) {
    val ref = new ErxReceiptReference(receipt);
    return this.withReceiptReference(ref);
  }

  public ErxChargeItemFaker withEnterer(TelematikID telematikId) {
    builderConsumers.computeIfPresent(
        "enterer", (key, defaultValue) -> b -> b.enterer(telematikId));
    return this;
  }

  public ErxChargeItemFaker withEnterer(String telematikId) {
    return this.withEnterer(TelematikID.from(telematikId));
  }

  public ErxChargeItemFaker withEnteredDate(Date date, TemporalPrecisionEnum precision) {
    builderConsumers.put("enteredDate", b -> b.entered(date, precision));
    return this;
  }

  public ErxChargeItemFaker withEnteredDate(Date date) {
    return this.withEnteredDate(date, TemporalPrecisionEnum.SECOND);
  }

  public ErxChargeItemFaker withMarkingFlag(MarkingFlag markingFlag) {
    builderConsumers.computeIfPresent(
        "markingFlag", (key, defaultValue) -> b -> b.markingFlag(markingFlag));
    return this;
  }

  public ErxChargeItemFaker withMarkingFlag(
      boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return this.withMarkingFlag(MarkingFlag.with(insuranceProvider, subsidy, taxOffice));
  }

  public ErxChargeItemFaker withVerordnung(String id) {
    return this.withVerordnung(new KbvBundleReference(id));
  }

  public ErxChargeItemFaker withVerordnung(KbvBundleReference reference) {
    builderConsumers.computeIfPresent(
        "verordnung", (key, defaultValue) -> b -> b.verordnung(reference));
    return this;
  }

  public ErxChargeItemFaker withVerordnung(KbvErpBundle bundle) {
    return this.withVerordnung(bundle.getReference());
  }

  public ErxChargeItemFaker withAbgabedatensatz(
      DavAbgabedatenBundle bundle, Function<DavAbgabedatenBundle, byte[]> signer) {
    return withAbgabedatensatz(bundle.getReference(), signer.apply(bundle));
  }

  public ErxChargeItemFaker withAbgabedatensatz(String id, byte[] signed) {
    return withAbgabedatensatz(new AbgabedatensatzReference(id), signed);
  }

  public ErxChargeItemFaker withAbgabedatensatz(AbgabedatensatzReference reference, byte[] signed) {
    builderConsumers.put(KEY_ABGABEDATENSATZ, b -> b.abgabedatensatz(reference, signed));
    return this;
  }

  public ErxChargeItem fake() {
    return this.toBuilder().build();
  }

  public ErxChargeItemBuilder toBuilder() {
    val builder = ErxChargeItemBuilder.forPrescription(prescriptionId);
    builderConsumers.computeIfAbsent(
        KEY_ABGABEDATENSATZ,
        key -> b -> b.abgabedatensatz(builder.getResourceId(), "faked binary content".getBytes()));
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
