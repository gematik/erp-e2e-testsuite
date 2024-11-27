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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPrescriptionId;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerTelematikId;

import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBase;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import org.hl7.fhir.r4.model.MedicationDispense;

public abstract class ErxMedicationDispenseBaseFaker<
    M extends ErxMedicationDispenseBase,
    F extends ErxMedicationDispenseBaseFaker<M, F, B>,
    B extends ErxMedicationDispenseBaseBuilder<M, B>> {

  protected KVNR kvnr = KVNR.random();
  protected final Map<String, Consumer<B>> builderConsumers = new HashMap<>();

  protected ErxMedicationDispenseBaseFaker() {
    withPerformer(fakerTelematikId());
    withPrescriptionId(fakerPrescriptionId());
  }

  @SuppressWarnings("unchecked")
  protected final F self() {
    return (F) this;
  }

  public F withKvnr(KVNR kvnr) {
    this.kvnr = kvnr;
    return self();
  }

  public F withVersion(ErpWorkflowVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return self();
  }

  public F withPerformer(String perfomerId) {
    builderConsumers.put("performer", b -> b.performerId(perfomerId));
    return self();
  }

  public F withPrescriptionId(PrescriptionId prescriptionId) {
    builderConsumers.put("prescriptionId", b -> b.prescriptionId(prescriptionId));
    return self();
  }

  public F withPrescriptionId(String prescriptionId) {
    return this.withPrescriptionId(new PrescriptionId(prescriptionId));
  }

  public F withMedication(GemErpMedication medication) {
    builderConsumers.put("medication", b -> b.medication(medication));
    return self();
  }

  public F withStatus(MedicationDispense.MedicationDispenseStatus status) {
    builderConsumers.put("status", b -> b.status(status));
    return self();
  }

  public F withStatus(String status) {
    return this.withStatus(MedicationDispense.MedicationDispenseStatus.fromCode(status));
  }

  public F withHandedOverDate(Date whenHandedOver) {
    builderConsumers.put("handedOverDate", b -> b.whenHandedOver(whenHandedOver));
    return self();
  }

  public abstract B toBuilder();

  public final M fake() {
    return this.toBuilder().build();
  }
}
