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

import static de.gematik.test.core.expectations.verifier.TaskVerifier.*;
import static de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef.EXT_REDEEMABLE_BY_PROPERTIES;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.date.DateCalculator;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import lombok.val;
import org.hl7.fhir.r4.model.*;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TaskVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  private ErxTask newTask() {
    return new ErxTask();
  }

  private ErxTask withRedeemableByProperties(boolean value) {
    val task = newTask();
    task.addExtension(
        new Extension(EXT_REDEEMABLE_BY_PROPERTIES.getCanonicalUrl(), new BooleanType(value)));
    return task;
  }

  private ErxTask withRedeemableByPatientAuthorization(boolean value) {
    val task = newTask();
    task.addExtension(
        new Extension(
            GemErpEuStructDef.EXT_REDEEMABLE_BY_PATIENT_AUTHORIZATION.getCanonicalUrl(),
            new BooleanType(value)));
    return task;
  }

  @Test
  void shouldNotInstantiateTaskVerifier() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(TaskVerifier.class));
  }

  @Test
  void shouldThrowOnInvalidPrescriptionFlowType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_169.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val step = hasWorkflowType(PrescriptionFlowType.FLOW_TYPE_160);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldPassOnCorrectPrescriptionFlowType() {
    val task = new ErxTask();
    val coding = PrescriptionFlowType.FLOW_TYPE_160.asCoding(true);
    task.addExtension(ErpWorkflowStructDef.PRESCRIPTION_TYPE.getCanonicalUrl(), coding);

    val step = hasWorkflowType(PrescriptionFlowType.FLOW_TYPE_160);
    step.apply(task);
  }

  @Test
  void shouldFailOnMissingPrescriptionId() {
    val task = new ErxTask();

    val step = hasValidPrescriptionId();
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldPassOnCorrectReadyTaskStatus() {
    val task = new ErxTask();
    task.setStatus(TaskStatus.READY);

    val step = isInReadyStatus();
    step.apply(task);
  }

  @Test
  void shouldPassOnCorrectDraftTaskStatus() {
    val task = new ErxTask();
    task.setStatus(TaskStatus.DRAFT);

    val step = isInDraftStatus();
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidStatus() {
    val task = new ErxTask();
    task.setStatus(TaskStatus.COMPLETED);

    val step = isInDraftStatus();
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectMvoExpiryDateWithoutEnd() {
    val dc = new DateCalculator();
    val validExpiryDate = dc.getDateAfterCalendarDays(new Date(), 365);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(validExpiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoExpiryDate(null);
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidMvoExpiryDateWithoutEnd() {
    val dc = new DateCalculator();
    val invalidExpiryDate = dc.getDateAfterCalendarDays(new Date(), 364);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(invalidExpiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoExpiryDate(null);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectMvoExpiryDateWithEnd() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterCalendarDays(new Date(), 10);
    val mvoEndDate = dc.getDateAfterCalendarDays(new Date(), 10);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoExpiryDate(mvoEndDate);
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidMvoExpiryDateWithEnd() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterCalendarDays(new Date(), 9);
    val mvoEndDate = dc.getDateAfterCalendarDays(new Date(), 10);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE_12.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoExpiryDate(mvoEndDate);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectMvoAcceptDateWithoutEnd() {
    val dc = new DateCalculator();
    val validExpiryDate = dc.getDateAfterCalendarDays(new Date(), 365);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(validExpiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoAcceptDate(null);
    step.apply(task);
  }

  @Test
  void shouldCheckCorrectMvoAcceptDateWithEnd() {
    val dc = new DateCalculator();
    val validExpiryDate = dc.getDateAfterCalendarDays(new Date(), 365);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(validExpiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoAcceptDate(validExpiryDate);
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidMvoAcceptDateWithoutEnd() {
    val dc = new DateCalculator();
    val invalidExpiryDate = dc.getDateAfterCalendarDays(new Date(), 10);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(invalidExpiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectMvoAcceptDate(null);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectExpiryDate() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterMonths(new Date(), 3);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectExpiryDate();
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidExpiryDate() {
    val dc = new DateCalculator();
    val expiryDateTmp = dc.getDateAfterMonths(new Date(), 3);
    val expiryDate = dc.getDateAfterCalendarDays(expiryDateTmp, 1);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.EXPIRY_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectExpiryDate();
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectAcceptDateGkv() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterCalendarDays(new Date(), 28);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectAcceptDate(PrescriptionFlowType.FLOW_TYPE_160);
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidAcceptDateGkv() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterMonths(new Date(), 3);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectAcceptDate(PrescriptionFlowType.FLOW_TYPE_160);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldCheckCorrectAcceptDatePkv() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterMonths(new Date(), 3);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectAcceptDate(PrescriptionFlowType.FLOW_TYPE_200);
    step.apply(task);
  }

  @Test
  void shouldFailOnInvalidAcceptDatePkv() {
    val dc = new DateCalculator();
    val expiryDate = dc.getDateAfterCalendarDays(new Date(), 28);
    val dateType = new DateType(new SimpleDateFormat("yyyy-MM-dd").format(expiryDate));
    val task = new ErxTask();

    task.addExtension(ErpWorkflowStructDef.ACCEPT_DATE.getCanonicalUrl(), dateType);

    val step = hasCorrectAcceptDate(PrescriptionFlowType.FLOW_TYPE_200);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldDetectTaskStatusCorrect() {
    val task = new ErxTask();
    task.setStatus(Task.TaskStatus.INPROGRESS);
    val testStatus = Task.TaskStatus.INPROGRESS;

    val step = taskIsInStatus(testStatus, ErpAfos.A_24034);
    assertDoesNotThrow(() -> step.apply(task));
  }

  @Test
  void shouldFailsWhileDetectTaskStatus() {
    val task = new ErxTask();
    task.setStatus(Task.TaskStatus.COMPLETED);
    val testStatus = Task.TaskStatus.INPROGRESS;

    val step = taskIsInStatus(testStatus, ErpAfos.A_24034);
    assertThrows(AssertionError.class, () -> step.apply(task));
  }

  @Test
  void shouldVerifyRedeemableByProperties() {
    val step = TaskVerifier.hasRedeemableByProperties(true);
    assertTrue(step.getPredicate().test(withRedeemableByProperties(true)));
  }

  @Test
  void shouldFailRedeemableByPropertiesWhenExtensionMissing() {
    val step = TaskVerifier.hasRedeemableByProperties(true);
    assertFalse(step.getPredicate().test(newTask()));
  }

  @Test
  void shouldVerifyRedeemableByPatientAuthorization() {
    val step = TaskVerifier.hasRedeemableByPatientAuthorization(false);
    assertTrue(step.getPredicate().test(withRedeemableByPatientAuthorization(false)));
  }

  @Test
  void shouldFailRedeemableByPatientAuthorizationWhenValueDifferent() {
    val step = TaskVerifier.hasRedeemableByPatientAuthorization(true);

    assertFalse(step.getPredicate().test(withRedeemableByPatientAuthorization(false)));
  }

  @Test
  void shouldFailRedeemableByPatientAuthorizationWhenExtensionMissing() {
    val step = TaskVerifier.hasRedeemableByPatientAuthorization(true);

    assertFalse(step.getPredicate().test(newTask()));
  }

  @Test
  void shouldVerifySecretPresent() {
    val task = new ErxTask();
    task.getMeta().setProfile(List.of(ErpWorkflowStructDef.TASK.asCanonicalType()));
    task.addIdentifier(ErpWorkflowNamingSystem.SECRET.asIdentifier("0123456789"));

    val bundle = new ErxAcceptBundle();
    bundle.setEntry(List.of(new Bundle.BundleEntryComponent().setResource(task)));

    val step = TaskVerifier.hasSecret();

    assertTrue(step.getPredicate().test(bundle));
  }

  @Test
  void shouldFailWhenSecretMissing() {
    val task = new ErxTask();
    task.getMeta().setProfile(List.of(ErpWorkflowStructDef.TASK.asCanonicalType()));

    val bundle = new ErxAcceptBundle();
    bundle.setEntry(List.of(new Bundle.BundleEntryComponent().setResource(task)));

    val step = TaskVerifier.hasSecret();

    assertFalse(step.getPredicate().test(bundle));
  }
}
