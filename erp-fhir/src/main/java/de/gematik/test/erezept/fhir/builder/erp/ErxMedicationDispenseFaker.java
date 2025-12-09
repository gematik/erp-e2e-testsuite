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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFutureExpirationDate;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;

import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;

public class ErxMedicationDispenseFaker
    extends ErxMedicationDispenseBaseFaker<
        ErxMedicationDispense,
        ErpWorkflowVersion,
        ErxMedicationDispenseFaker,
        ErxMedicationDispenseBuilder> {

  private ErxMedicationDispenseFaker() {
    super();
    this.withBatch(fakerLotNumber(), fakerFutureExpirationDate());
    withMedication(GemErpMedicationFaker.forPznMedication().fake());
  }

  public static ErxMedicationDispenseFaker builder() {
    return new ErxMedicationDispenseFaker();
  }

  public ErxMedicationDispenseFaker withMedication(KbvErpMedication medication) {
    builderConsumers.put("medication", b -> b.medication(medication));
    return this;
  }

  public ErxMedicationDispenseFaker withPreparedDate(Date whenPrepared) {
    builderConsumers.put("preparedDate", b -> b.whenPrepared(whenPrepared));
    return this;
  }

  public ErxMedicationDispenseFaker withBatch(Medication.MedicationBatchComponent batch) {
    builderConsumers.put("batch", b -> b.batch(batch));
    return this;
  }

  public ErxMedicationDispenseFaker withBatch(String lotNumber, Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return this.withBatch(newBatch);
  }

  public ErxMedicationDispenseBuilder toBuilder() {
    val builder = ErxMedicationDispenseBuilder.forKvnr(kvnr);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
