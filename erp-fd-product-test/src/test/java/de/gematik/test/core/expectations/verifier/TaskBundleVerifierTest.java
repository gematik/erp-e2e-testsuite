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

package de.gematik.test.core.expectations.verifier;

import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.authoredOnDateIsEqual;
import static de.gematik.test.core.expectations.verifier.TaskBundleVerifier.verifyAuthoredOnDateWithPredicate;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.LocalDate;
import java.time.Month;
import lombok.val;
import org.hl7.fhir.r4.model.Binary;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskBundleVerifierTest extends ParsingTest {

  ErxTaskBundle bundle =
      getDecodedFromPath(
          ErxTaskBundle.class,
          "fhir/valid/erp/1.3.0/taskbundlesearchset/5ea6b34e-d05d-42e1-815d-5b44f2eb49fe.json");

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
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK_12.getCanonicalUrl());
    task.getContained().add(new Binary());
    erxTaskBundle2.addEntry().setResource(task);

    val step = TaskBundleVerifier.doesContainsErxTasksWithoutQES(ErpAfos.A_25209);
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle1));
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle2));
  }

  @Test
  void containsExclusivelyTasksWithGKVInsuranceType() {
    val erxTaskBundle1 = new ErxTaskBundle();
    val task = new ErxTask();
    task.getMeta().addProfile(ErpWorkflowStructDef.TASK_12.getCanonicalUrl());
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

    val step = TaskBundleVerifier.doesNotContainsExpiredErxTasks(ErpAfos.A_23452);
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

    val step = TaskBundleVerifier.doesNotContainsExpiredErxTasks(ErpAfos.A_23452);
    assertThrows(AssertionError.class, () -> step.apply(erxTaskBundle));
  }

  @Test
  void shouldValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val step = authoredOnDateIsEqual(LocalDate.of(2024, Month.JULY, 25));
    assertDoesNotThrow(() -> step.apply(bundle));
  }

  @Test
  void shouldThrowsWhileValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val step = authoredOnDateIsEqual(LocalDate.of(2024, Month.JULY, 24));
    assertThrows(AssertionError.class, () -> step.apply(bundle));
  }

  @Test
  void shouldThrowWileValidateAuthoredOnDateCorrectInSearchSetBundles() {
    val testDate = LocalDate.of(2024, Month.JULY, 25);
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
}
