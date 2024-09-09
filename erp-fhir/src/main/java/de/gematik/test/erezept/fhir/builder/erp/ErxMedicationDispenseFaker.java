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

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.MedicationDispense;

public class ErxMedicationDispenseFaker {
  private KVNR kvnr = KVNR.random();
  private final Map<String, Consumer<ErxMedicationDispenseBuilder>> builderConsumers =
      new HashMap<>();
  private static final String KEY_MEDICATION = "medication"; // key used for builderConsumers map

  private ErxMedicationDispenseFaker() {
    builderConsumers.put("performer", b -> b.performerId(fakerTelematikId()));
    builderConsumers.put("prescriptionId", b -> b.prescriptionId(fakerPrescriptionId()));
    builderConsumers.put(
        KEY_MEDICATION, b -> b.medication(KbvErpMedicationPZNFaker.builder().fake()));
    builderConsumers.put("batch", b -> b.batch(fakerLotNumber(), fakerFutureExpirationDate()));
  }

  public static ErxMedicationDispenseFaker builder() {
    return new ErxMedicationDispenseFaker();
  }

  public ErxMedicationDispenseFaker withKvnr(KVNR kvnr) {
    this.kvnr = kvnr;
    return this;
  }

  public ErxMedicationDispenseFaker withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public ErxMedicationDispenseFaker withPzn(String pzn) {
    val newMedication =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake();
    builderConsumers.computeIfPresent(
        KEY_MEDICATION, (key, defaultValue) -> b -> b.medication(newMedication));
    return this;
  }

  public ErxMedicationDispenseFaker withPerfomer(String perfomerId) {
    builderConsumers.computeIfPresent(
        "performer", (key, defaultValue) -> b -> b.performerId(perfomerId));
    return this;
  }

  public ErxMedicationDispenseFaker withPrescriptionId(PrescriptionId prescriptionId) {
    builderConsumers.computeIfPresent(
        "prescriptionId", (key, defaultValue) -> b -> b.prescriptionId(prescriptionId));
    return this;
  }

  public ErxMedicationDispenseFaker withPrescriptionId(String prescriptionId) {
    return this.withPrescriptionId(new PrescriptionId(prescriptionId));
  }

  public ErxMedicationDispenseFaker withStatus(MedicationDispense.MedicationDispenseStatus status) {
    builderConsumers.put("status", b -> b.status(status));
    return this;
  }

  public ErxMedicationDispenseFaker withStatus(String status) {
    return this.withStatus(MedicationDispense.MedicationDispenseStatus.fromCode(status));
  }

  public ErxMedicationDispenseFaker withMedication(KbvErpMedication medication) {
    builderConsumers.computeIfPresent(
        KEY_MEDICATION, (key, defaultValue) -> b -> b.medication(medication));
    return this;
  }

  public ErxMedicationDispenseFaker withHandedOverDate(Date whenHandedOver) {
    builderConsumers.put("handedOverDate", b -> b.whenHandedOver(whenHandedOver));
    return this;
  }

  public ErxMedicationDispenseFaker withPreparedDate(Date whenPrepared) {
    builderConsumers.put("preparedDate", b -> b.whenPrepared(whenPrepared));
    return this;
  }

  public ErxMedicationDispenseFaker withBatch(Medication.MedicationBatchComponent batch) {
    builderConsumers.computeIfPresent("batch", (key, defaultValue) -> b -> b.batch(batch));
    return this;
  }

  public ErxMedicationDispenseFaker withBatch(String lotNumber, Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return this.withBatch(newBatch);
  }

  public ErxMedicationDispense fake() {
    return this.toBuilder().build();
  }

  public ErxMedicationDispenseBuilder toBuilder() {
    val builder = ErxMedicationDispenseBuilder.forKvnr(kvnr);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
