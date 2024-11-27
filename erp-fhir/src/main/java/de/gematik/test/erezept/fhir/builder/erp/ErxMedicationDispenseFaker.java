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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerBool;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerDrugName;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFutureExpirationDate;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerLotNumber;

import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispense;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;

public class ErxMedicationDispenseFaker
    extends ErxMedicationDispenseBaseFaker<
        ErxMedicationDispense, ErxMedicationDispenseFaker, ErxMedicationDispenseBuilder> {

  private static final String KEY_MEDICATION = "medication"; // key used for builderConsumers map

  private ErxMedicationDispenseFaker() {
    super();
    builderConsumers.put("batch", b -> b.batch(fakerLotNumber(), fakerFutureExpirationDate()));

    if (fakerBool()
        && ErpWorkflowVersion.getDefaultVersion().compareTo(ErpWorkflowVersion.V1_4_0) >= 0) {
      withMedication(GemErpMedicationFaker.builder().fake());
    } else {
      withMedication(KbvErpMedicationPZNFaker.builder().fake());
    }
  }

  public static ErxMedicationDispenseFaker builder() {
    return new ErxMedicationDispenseFaker();
  }

  public ErxMedicationDispenseFaker withPzn(String pzn) {
    val newMedication =
        KbvErpMedicationPZNFaker.builder().withPznMedication(pzn, fakerDrugName()).fake();
    builderConsumers.put(KEY_MEDICATION, b -> b.medication(newMedication));
    return this;
  }

  public ErxMedicationDispenseFaker withMedication(KbvErpMedication medication) {
    builderConsumers.put(KEY_MEDICATION, b -> b.medication(medication));
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
