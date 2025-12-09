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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.eu.EuPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.Test;

class ErxEuPrescriptionVerifierTest extends ErpFhirParsingTest {

  private EuPrescriptionBundle createBundleWithTasks(ErxTask... tasks) {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();
    List<Bundle.BundleEntryComponent> entries =
        Arrays.stream(tasks).map(t -> new Bundle.BundleEntryComponent().setResource(t)).toList();
    bundle.setEntry(entries);
    return bundle;
  }

  private ErxTask createTask(Task.TaskStatus status, boolean withOwner, boolean withSecret) {
    ErxTask task = new ErxTask();
    task.setStatus(status);
    if (withOwner) {
      task.setOwner(new Reference("Organization/owner"));
    }
    if (withSecret) {
      task.addIdentifier(new Identifier().setSystem("secret").setValue("s3cr3t"));
    }
    return task;
  }

  @Test
  void shouldFailOnWrongPrescriptionCount() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(createTask(Task.TaskStatus.READY, true, true));

    val step = ErxEuPrescriptionVerifier.bundleHasPrescriptionCount(2);

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldVerifyAllTasksHaveExpectedStatus() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(
            createTask(Task.TaskStatus.READY, true, true),
            createTask(Task.TaskStatus.READY, true, true));

    val step = ErxEuPrescriptionVerifier.tasksHaveStatus(Task.TaskStatus.READY);

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailIfOneTaskHasDifferentStatus() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(
            createTask(Task.TaskStatus.READY, true, true),
            createTask(Task.TaskStatus.COMPLETED, true, true));

    val step = ErxEuPrescriptionVerifier.tasksHaveStatus(Task.TaskStatus.READY);

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldVerifyAllTasksHaveOwner() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(
            createTask(Task.TaskStatus.READY, true, true),
            createTask(Task.TaskStatus.READY, true, true));

    val step = ErxEuPrescriptionVerifier.tasksHaveOwnerSet();

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailIfAnyTaskMissingOwner() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(createTask(Task.TaskStatus.READY, false, true));

    val step = ErxEuPrescriptionVerifier.tasksHaveOwnerSet();

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailIfTaskMissingSecret() {
    EuPrescriptionBundle bundle =
        createBundleWithTasks(createTask(Task.TaskStatus.READY, false, true));

    val step = ErxEuPrescriptionVerifier.tasksHaveNewSecretIdentifier();

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldPassWithEmptyBundle() {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();
    bundle.setEntry(Collections.emptyList());

    val stepStatus = ErxEuPrescriptionVerifier.tasksHaveStatus(Task.TaskStatus.READY);
    val stepOwner = ErxEuPrescriptionVerifier.tasksHaveOwnerSet();
    val stepSecret = ErxEuPrescriptionVerifier.tasksHaveNewSecretIdentifier();

    assertTrue(stepStatus.getPredicate().test(bundle));
    assertTrue(stepOwner.getPredicate().test(bundle));
    assertTrue(stepSecret.getPredicate().test(bundle));
  }

  @Test
  void shouldPassIfBundleContainsPrescription() {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();
    KbvErpBundle kbv = new KbvErpBundle();
    val pid = PrescriptionId.random();
    kbv.setPrescriptionId(pid);
    bundle.setEntry(List.of(new Bundle.BundleEntryComponent().setResource(kbv)));

    val step = ErxEuPrescriptionVerifier.bundleContainsPrescription(pid, 1);

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailIfBundleDoesNotContainPrescription() {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();
    KbvErpBundle kbv = new KbvErpBundle();
    val pid = PrescriptionId.random();
    kbv.setPrescriptionId(pid);
    bundle.setEntry(List.of(new Bundle.BundleEntryComponent().setResource(kbv)));

    val step = ErxEuPrescriptionVerifier.bundleNotContainsPrescription(pid);

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldReturnTrueIfExpectedPrescriptionIsPresent() {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();

    KbvErpBundle kbv1 = new KbvErpBundle();
    kbv1.setPrescriptionId(PrescriptionId.random());

    KbvErpBundle kbv2 = new KbvErpBundle();
    val expectedPid = PrescriptionId.random();
    kbv2.setPrescriptionId(expectedPid);

    bundle.setEntry(
        List.of(
            new Bundle.BundleEntryComponent().setResource(kbv1),
            new Bundle.BundleEntryComponent().setResource(kbv2)));

    val step = ErxEuPrescriptionVerifier.bundleContainsPrescription(expectedPid, 1);

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldReturnFalseIfUnexpectedPrescriptionIsPresent() {
    EuPrescriptionBundle bundle = new EuPrescriptionBundle();
    KbvErpBundle kbv1 = new KbvErpBundle();
    kbv1.setPrescriptionId(PrescriptionId.random());

    val forbiddenPid = PrescriptionId.random();
    KbvErpBundle kbv2 = new KbvErpBundle();
    kbv2.setPrescriptionId(forbiddenPid);

    bundle.setEntry(
        List.of(
            new Bundle.BundleEntryComponent().setResource(kbv1),
            new Bundle.BundleEntryComponent().setResource(kbv2)));

    val step = ErxEuPrescriptionVerifier.bundleNotContainsPrescription(forbiddenPid);

    assertFalse(step.getPredicate().test(bundle));
  }

  @Test
  void shouldReturnFalseIfInvalidKvnrIsDetected() {
    val koi = KVNR.random(); // KVNR of interest
    val bundle = new EuPrescriptionBundle();
    val kbv1 =
        KbvErpBundleFaker.builder()
            .withPatient(
                KbvPatientFaker.builder().withKvnrAndInsuranceType(koi, InsuranceTypeDe.GKV).fake())
            .fake();
    val kbv2 = KbvErpBundleFaker.builder().fake();

    bundle.addEntry().setResource(kbv1);
    bundle.addEntry().setResource(kbv2);

    val step = ErxEuPrescriptionVerifier.bundleContainsOnlyKvnr(koi, ErpAfos.A_27063);

    assertFalse(step.getPredicate().test(bundle));
  }
}
