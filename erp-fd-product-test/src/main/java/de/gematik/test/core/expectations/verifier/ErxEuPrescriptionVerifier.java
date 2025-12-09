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

package de.gematik.test.core.expectations.verifier;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Task;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ErxEuPrescriptionVerifier {

  public static VerificationStep<EuPrescriptionBundle> bundleHasPrescriptionCount(
      int expectedCount) {
    return new VerificationStep.StepBuilder<EuPrescriptionBundle>(
            ErpAfos.A_27587, "Should contain " + expectedCount + " prescription(s)")
        .predicate(bundle -> bundle.getPrescriptionIds().size() == expectedCount)
        .accept();
  }

  public static <T extends EuPrescriptionBundle> VerificationStep<T> tasksHaveStatus(
      Task.TaskStatus expectedStatus) {
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_27589, "All tasks have status " + expectedStatus)
        .predicate(
            bundle ->
                bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(ErxTask.class::isInstance)
                    .map(r -> (ErxTask) r)
                    .allMatch(task -> task.getStatus() == expectedStatus))
        .accept();
  }

  public static <T extends EuPrescriptionBundle> VerificationStep<T> tasksHaveOwnerSet() {
    return new VerificationStep.StepBuilder<T>(ErpAfos.A_27589, "All tasks have owner set")
        .predicate(
            bundle ->
                bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(ErxTask.class::isInstance)
                    .map(r -> (ErxTask) r)
                    .allMatch(task -> task.hasOwner() && task.getOwner() != null))
        .accept();
  }

  public static <T extends EuPrescriptionBundle>
      VerificationStep<T> tasksHaveNewSecretIdentifier() {
    return new VerificationStep.StepBuilder<T>(
            ErpAfos.A_27589, "All tasks have a Secret identifier")
        .predicate(
            bundle ->
                bundle.getEntry().stream()
                    .map(Bundle.BundleEntryComponent::getResource)
                    .filter(ErxTask.class::isInstance)
                    .map(r -> (ErxTask) r)
                    .allMatch(task -> task.getSecret().isPresent()))
        .accept();
  }

  public static VerificationStep<EuPrescriptionBundle> bundleNotContainsPrescription(
      PrescriptionId prescriptionId) {
    return new VerificationStep.StepBuilder<EuPrescriptionBundle>(
            ErpAfos.A_27587, "Bundle should NOT contain prescription " + prescriptionId)
        .predicate(bundle -> bundle.getPrescriptionIds().stream().noneMatch(prescriptionId::equals))
        .accept();
  }

  public static VerificationStep<EuPrescriptionBundle> bundleContainsPrescription(
      PrescriptionId prescriptionId, int expectedCount) {
    return new VerificationStep.StepBuilder<EuPrescriptionBundle>(
            ErpAfos.A_27587, "Bundle should contain prescription " + prescriptionId)
        .predicate(
            bundle ->
                bundle.getPrescriptionIds().contains(prescriptionId)
                    && bundle.getPrescriptionIds().stream()
                            .filter(pID -> pID.equals(prescriptionId))
                            .toList()
                            .size()
                        == expectedCount)
        .accept();
  }

  public static VerificationStep<EuPrescriptionBundle> bundleContainsOnlyKvnr(
      KVNR kvnr, RequirementsSet requirementsSet) {
    return new VerificationStep.StepBuilder<EuPrescriptionBundle>(
            requirementsSet,
            "Bundle should contain only prescriptions for KVNR: " + kvnr.getValue())
        .predicate(
            bundle ->
                bundle.getKbvErpBundles().stream()
                    .allMatch(b -> b.getPatient().getKvnr().equals(kvnr)))
        .accept();
  }
}
