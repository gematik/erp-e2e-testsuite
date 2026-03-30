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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerAmount;
import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerTelematikId;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;

@RequiredArgsConstructor
public class ErxMedicationDispenseBundleFaker {
  private final ErpWorkflowVersion erpWorkflowVersion;

  private int amount = fakerAmount();
  private KVNR kvnr = KVNR.random();
  private String performerId = fakerTelematikId();
  private PrescriptionId prescriptionId = PrescriptionId.random();

  public static ErxMedicationDispenseBundleFaker build() {
    return build(ErpWorkflowVersion.getDefaultVersion());
  }

  public static ErxMedicationDispenseBundleFaker build(ErpWorkflowVersion erpWorkflowVersion) {
    return new ErxMedicationDispenseBundleFaker(erpWorkflowVersion);
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
    val builder = ErxMedicationDispenseBundleBuilder.empty().version(erpWorkflowVersion);
    IntStream.range(0, amount)
        .forEach(
            idx ->
                builder.add(
                    ErxMedicationDispenseFaker.builder(erpWorkflowVersion)
                        .withKvnr(kvnr)
                        .withPerformer(performerId)
                        .withPrescriptionId(prescriptionId)
                        .fake()));
    return builder;
  }
}
