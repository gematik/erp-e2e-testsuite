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

package de.gematik.test.erezept.fhir.builder.eu;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerFutureExpirationDate;

import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuMedication;
import de.gematik.test.erezept.fhir.r4.eu.EuMedicationDispense;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.PractitionerRole;

public class EuMedicationDispenseFaker
    extends ErxMedicationDispenseBaseFaker<
        EuMedicationDispense, EuVersion, EuMedicationDispenseFaker, EuMedicationDispenseBuilder> {

  private EuMedicationDispenseFaker() {
    super();
    this.withBatch(fakerLotNumber(), fakerFutureExpirationDate());
    withMedication(EuMedicationBuilder.builder().build());
  }

  public static EuMedicationDispenseFaker builder() {
    return new EuMedicationDispenseFaker();
  }

  public EuMedicationDispenseFaker withMedication(EuMedication medication) {
    builderConsumers.put("medication", b -> b.medication(medication));
    return this;
  }

  public EuMedicationDispenseFaker withPreparedDate(Date whenPrepared) {
    builderConsumers.put("preparedDate", b -> b.whenPrepared(whenPrepared));
    return this;
  }

  public EuMedicationDispenseFaker withBatch(Medication.MedicationBatchComponent batch) {
    builderConsumers.put("batch", b -> b.batch(batch));
    return this;
  }

  public EuMedicationDispenseFaker withPerformer(PractitionerRole practitionerRoleForRef) {
    builderConsumers.put("performer", b -> b.performer(practitionerRoleForRef));
    return this;
  }

  public EuMedicationDispenseFaker withBatch(String lotNumber, Date expirationDate) {
    val newBatch = new Medication.MedicationBatchComponent();
    newBatch.setLotNumber(lotNumber);
    newBatch.setExpirationDate(expirationDate);
    return this.withBatch(newBatch);
  }

  public EuMedicationDispenseBuilder toBuilder() {
    val builder = EuMedicationDispenseBuilder.forKvnr(kvnr);
    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
