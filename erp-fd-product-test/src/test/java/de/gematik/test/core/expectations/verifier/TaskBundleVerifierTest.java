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

import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.authoredOnDateIsEqual;
import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.verifyAuthoredOnDateWithPredicate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.LocalDate;
import java.time.Month;
import java.util.Arrays;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TaskBundleVerifierTest extends ErpFhirParsingTest {

  ErxTaskBundle bundle =
      getDecodedFromPath(
          ErxTaskBundle.class,
          "fhir/valid/erp/1.4.0/taskBundle/erpFdResponse_manipulatedToOneEntry.json");

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiateUtilityClass() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(TaskBundleVerifier.class));
  }

  @Test
  void doesContainsErxTasksWithoutQES() {
    val erxTaskBundle1 = new ErxTaskBundle();
    erxTaskBundle1.addEntry().setResource(new Binary());

    val erxTaskBundle2 = new ErxTaskBundle();
    val task = new ErxTask();
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK.getCanonicalUrl());
    task.getContained().add(new Binary());
    erxTaskBundle2.addEntry().setResource(task);

    val step = TaskBundleVerifier.doesNotContainQES(ErpAfos.A_25209);
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle1));
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle2));
  }

  @Test
  void containsExclusivelyTasksWithGKVInsuranceType() {
    val erxTaskBundle1 = new ErxTaskBundle();
    val task = new ErxTask();
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK.getCanonicalUrl());
    task.addExtension()
        .setUrl(ErpWorkflowStructDef.PRESCRIPTION_TYPE_12.getCanonicalUrl())
        .setValue(PrescriptionFlowType.FLOW_TYPE_200.asCoding());
    erxTaskBundle1.addEntry().setResource(task);

    val step = TaskBundleVerifier.containsExclusivelyTasksWithGKVInsuranceType();
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle1));
  }

  @Test
  void shouldReturnTrueWhenNoTasksAreExpired() {
    val dc = new de.gematik.test.erezept.fhir.date.DateCalculator();
    val erxTaskBundle = new ErxTaskBundle();
    val erxTask = new ErxTask();
    erxTaskBundle.addEntry().setResource(erxTask);

    val expiryDate = dc.getDateAfterCalendarDays(new java.util.Date(), 1);
    val dateType =
        new org.hl7.fhir.r4.model.DateType(
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    erxTask.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = TaskBundleVerifier.doesNotContainExpiredTasks(ErpAfos.A_23452);
    step.apply(erxTaskBundle);
  }

  @Test
  void shouldReturnFalseWhenTasksAreExpired() {
    val dc = new de.gematik.test.erezept.fhir.date.DateCalculator();
    val erxTaskBundle = new ErxTaskBundle();
    val erxTask = new ErxTask();
    erxTaskBundle.addEntry().setResource(erxTask);

    val expiryDate = dc.getDateAfterCalendarDays(new java.util.Date(), -1);
    val dateType =
        new org.hl7.fhir.r4.model.DateType(
            new java.text.SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    erxTask.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = TaskBundleVerifier.doesNotContainExpiredTasks(ErpAfos.A_23452);
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }

  @Test
  void shouldValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val step = authoredOnDateIsEqual(LocalDate.of(2024, Month.NOVEMBER, 25));
    assertDoesNotThrow(() -> step.apply(bundle));
  }

  @Test
  void shouldThrowsWhileValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val step = authoredOnDateIsEqual(LocalDate.of(2024, Month.JULY, 24));
    assertThrows(AssertionError.class, () -> step.apply(bundle));
  }

  @Test
  void shouldThrowWileValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val testDate = LocalDate.of(2024, Month.NOVEMBER, 25);
    val step =
        verifyAuthoredOnDateWithPredicate(
            ld -> !ld.isEqual(testDate),
            "Die enthaltenen Tasks müssen ein anderes AuthoredOn Datum als "
                + testDate
                + " enthalten");
    assertThrows(AssertionError.class, () -> step.apply(bundle));
  }

  @Test
  void shouldValidateAuthoredOnDateCorrectInSearchSetBundlesAsNotEqual() {
    val testDate = LocalDate.of(2024, Month.JULY, 24);
    val step =
        verifyAuthoredOnDateWithPredicate(
            ld -> !ld.isEqual(testDate),
            "Die enthaltenen Tasks müssen ein anderes AuthoredOn Datum als "
                + testDate
                + " enthalten");
    assertDoesNotThrow(() -> step.apply(bundle));
  }

  @Test
  void shouldReturnTrueWhenNoTasksAreContained() {
    val erxTaskBundle = new ErxTaskBundle();
    val step = TaskBundleVerifier.hasNoTasks();
    assertDoesNotThrow(() -> step.apply(erxTaskBundle));

    erxTaskBundle.addEntry().setResource(new ErxTask());
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }

  @Test
  void shouldReturnFalseWhenNoTasksHasNoMethaProfile() {
    val erxTaskBundle = new ErxTaskBundle();
    erxTaskBundle.addEntry().setResource(new ErxTask());
    val step = TaskBundleVerifier.doesNotContainQES(ErpAfos.A_23452);

    erxTaskBundle.addEntry().setResource(new ErxTask());
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }

  @ParameterizedTest
  @EnumSource(Task.TaskStatus.class)
  void shouldOnlyContainsTaskWithStatus(Task.TaskStatus status) {
    val step = TaskBundleVerifier.containsOnlyTasksWith(status, ErpAfos.A_23452);

    val erxTaskBundle1 = new ErxTaskBundle();
    val erxTask1 = new ErxTask();
    erxTask1.setStatus(status);
    erxTaskBundle1.addEntry().setResource(erxTask1);
    assertDoesNotThrow(() -> step.apply(erxTaskBundle1));

    val erxTaskBundle2 = new ErxTaskBundle();
    Arrays.stream(Task.TaskStatus.values())
        .forEach(
            it -> {
              val erxTask2 = new ErxTask();
              erxTask2.setStatus(it);
              erxTaskBundle2.addEntry().setResource(erxTask2);
            });
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle2));
  }

  @Test
  void shouldOnlyContainsTasksForKvnr() {
    val kvnr = KVNR.random();
    val step = TaskBundleVerifier.containsOnlyTasksFor(kvnr, ErpAfos.A_23452);

    val erxTask = mock(ErxTask.class);
    when(erxTask.getForKvnr()).thenReturn(Optional.of(kvnr));

    val erxTaskBundle = new ErxTaskBundle();
    erxTaskBundle.addEntry().setResource(erxTask);
    assertDoesNotThrow(() -> step.apply(erxTaskBundle));

    erxTaskBundle.addEntry().setResource(new ErxTask());

    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }

  @ParameterizedTest
  @EnumSource(PrescriptionFlowType.class)
  void shouldOnlyContainsTaskWithStatus(PrescriptionFlowType flowType) {
    val step = TaskBundleVerifier.containsOnlyTasksWith(flowType, ErpAfos.A_23452);

    val erxTaskBundle = new ErxTaskBundle();

    Arrays.stream(PrescriptionFlowType.values())
        .forEach(
            it -> {
              val erxTask = mock(ErxTask.class);
              when(erxTask.getFlowType()).thenReturn(it);
              erxTaskBundle.addEntry().setResource(erxTask);
            });
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }
}
