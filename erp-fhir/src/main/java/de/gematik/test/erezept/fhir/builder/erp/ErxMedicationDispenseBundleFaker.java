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
 */

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerAmount;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerPrescriptionId;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerTelematikId;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;

public class ErxMedicationDispenseBundleFaker {
  private int amount = fakerAmount();
  private KVNR kvnr = KVNR.random();
  private String performerId = fakerTelematikId();
  private PrescriptionId prescriptionId = fakerPrescriptionId();

  public static ErxMedicationDispenseBundleFaker build() {
    return new ErxMedicationDispenseBundleFaker();
  }

  public ErxMedicationDispenseBundleFaker withAmount(int amount) {
    this.amount = amount;
    return this;
  }

  public ErxMedicationDispenseBundleFaker withKvnr(KVNR kvnr) {
    this.kvnr = kvnr;
    return this;
  }

  public ErxMedicationDispenseBundleFaker withPerformerId(String performerId) {
    this.performerId = performerId;
    return this;
  }

  public ErxMedicationDispenseBundleFaker withPrescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return this;
  }

  public Bundle fake() {
    return this.toBuilder().build();
  }

  public ErxMedicationDispenseBundleBuilder toBuilder() {
    val builder = ErxMedicationDispenseBundleBuilder.empty();
    IntStream.range(0, amount)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseFaker.builder()
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));
    return builder;
  }
}
