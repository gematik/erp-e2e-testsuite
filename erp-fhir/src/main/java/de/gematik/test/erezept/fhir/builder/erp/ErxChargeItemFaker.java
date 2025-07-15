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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerTelematikId;
import static de.gematik.test.erezept.fhir.builder.GemFaker.insuranceName;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.TelematikID;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import de.gematik.test.erezept.fhir.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.r4.dav.AbgabedatensatzReference;
import de.gematik.test.erezept.fhir.r4.dav.DavPkvAbgabedatenBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItem;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Function;
import lombok.val;
import org.hl7.fhir.r4.model.ChargeItem;
import org.hl7.fhir.r4.model.Reference;

public class ErxChargeItemFaker {

  private final Map<String, Consumer<ErxChargeItemBuilder>> builderConsumers = new HashMap<>();
  private PrescriptionId prescriptionId = PrescriptionId.random();

  private ErxChargeItemFaker() {
    this.withAccessCode(AccessCode.random())
        .withSubject(KVNR.randomPkv(), insuranceName())
        .withEnterer(fakerTelematikId())
        .withVerordnung(UUID.randomUUID().toString())
        .withAbgabedatensatz(
            UUID.randomUUID().toString(), GemFaker.getFaker().chuckNorris().fact().getBytes())
        .withMarkingFlag(fakerBool(), fakerBool(), fakerBool());
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
    builderConsumers.put("accessCode", b -> b.accessCode(accessCode));
    return this;
  }

  public ErxChargeItemFaker withAccessCode(String accessCode) {
    return this.withAccessCode(AccessCode.from(accessCode));
  }

  public ErxChargeItemFaker withStatus(ChargeItem.ChargeItemStatus status) {
    builderConsumers.put("status", b -> b.status(status));
    return this;
  }

  public ErxChargeItemFaker withStatus(final String status) {
    return this.withStatus(ChargeItem.ChargeItemStatus.fromCode(status.toLowerCase()));
  }

  public ErxChargeItemFaker withSubject(KVNR kvnr, String kvnrAssignerName) {
    builderConsumers.put("subject", b -> b.subject(kvnr, kvnrAssignerName));
    return this;
  }

  public ErxChargeItemFaker withReceipt(ErxReceipt receipt) {
    builderConsumers.put("receiptReference", b -> b.receipt(receipt));
    return this;
  }

  public ErxChargeItemFaker withEnterer(TelematikID telematikId) {
    builderConsumers.put("enterer", b -> b.enterer(telematikId));
    return this;
  }

  public ErxChargeItemFaker withEnterer(String telematikId) {
    return this.withEnterer(TelematikID.from(telematikId));
  }

  public ErxChargeItemFaker withEnteredDate(Date date) {
    builderConsumers.put("enteredDate", b -> b.entered(date));
    return this;
  }

  public ErxChargeItemFaker withMarkingFlag(MarkingFlag markingFlag) {
    builderConsumers.put("markingFlag", b -> b.markingFlag(markingFlag));
    return this;
  }

  public ErxChargeItemFaker withMarkingFlag(
      boolean insuranceProvider, boolean subsidy, boolean taxOffice) {
    return this.withMarkingFlag(MarkingFlag.with(insuranceProvider, subsidy, taxOffice));
  }

  public ErxChargeItemFaker withVerordnung(String id) {
    builderConsumers.put("verordnung", b -> b.verordnung(id));
    return this;
  }

  public ErxChargeItemFaker withVerordnung(Reference reference) {
    builderConsumers.put("verordnung", b -> b.verordnung(reference));
    return this;
  }

  public ErxChargeItemFaker withVerordnung(KbvErpBundle bundle) {
    return this.withVerordnung(bundle.asReference());
  }

  public ErxChargeItemFaker withAbgabedatensatz(
      DavPkvAbgabedatenBundle bundle, Function<DavPkvAbgabedatenBundle, byte[]> signer) {
    return withAbgabedatensatz(bundle.getReference(), signer.apply(bundle));
  }

  public ErxChargeItemFaker withAbgabedatensatz(String id, byte[] signed) {
    return withAbgabedatensatz(new AbgabedatensatzReference(id), signed);
  }

  public ErxChargeItemFaker withAbgabedatensatz(AbgabedatensatzReference reference, byte[] signed) {
    builderConsumers.put("abgabedatensatz", b -> b.abgabedatensatz(reference, signed));
    return this;
  }

  public ErxChargeItem fake() {
    return this.toBuilder().build();
  }

  public ErxChargeItemBuilder toBuilder() {
    val builder = ErxChargeItemBuilder.forPrescription(prescriptionId);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
