/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import de.gematik.test.erezept.fhir.date.DateCalculator;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Date;
import java.util.function.Predicate;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

public class TaskVerifier {

  private TaskVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxTask> hasWorkflowType(PrescriptionFlowType flowType) {
    return hasWorkflowType(flowType, ErpAfos.A_19112);
  }

  public static VerificationStep<ErxTask> hasWorkflowType(
      PrescriptionFlowType flowType, RequirementsSet req) {
    Predicate<ErxTask> predicate = task -> task.getFlowType().equals(flowType);
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            req.getRequirement(),
            format("Der WorkflowType muss {0} entsprechen", flowType.getCode()));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> isInDraftStatus() {
    return isInStatus(Task.TaskStatus.DRAFT, ErpAfos.A_19114.getRequirement());
  }

  public static VerificationStep<ErxTask> isInReadyStatus() {
    return isInStatus(Task.TaskStatus.READY, ErpAfos.A_19128.getRequirement());
  }

  public static VerificationStep<ErxTask> isInStatus(Task.TaskStatus status, Requirement req) {
    Predicate<ErxTask> predicate = task -> status.equals(task.getStatus());
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            req, format("Der Status eines neuen Tasks muss sich im Status {0} befinden", status));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasValidPrescriptionId() {
    Predicate<ErxTask> predicate =
        task -> {
          try {
            return task.getPrescriptionId().check();
          } catch (MissingFieldException mfe) {
            return false;
          }
        };
    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19019.getRequirement(),
            "Die Rezept-ID muss valide gemäß der Bildungsregel [gemSpec_DM_eRp#19217] sein");
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasCorrectMvoExpiryDate(Date mvoEndDate) {
    return hasCorrectMvoExpiryDate(new Date(), mvoEndDate);
  }

  public static VerificationStep<ErxTask> hasCorrectMvoExpiryDate(
      Date signatureDate, @Nullable Date mvoEndDate) {
    val dc = DateConverter.getInstance();
    val calculator = new DateCalculator();
    val expectedDate =
        dc.truncate(
            (mvoEndDate != null)
                ? mvoEndDate
                : calculator.getDateAfterCalendarDays(signatureDate, 365));

    Predicate<ErxTask> predicate =
        task -> {
          val expiryDate = task.getExpiryDate();
          return calculator.equalDates(expectedDate, expiryDate);
        };

    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19445.getRequirement(),
            format("Das MVO E-Rezept muss als ExpiryDate den Wert {0} haben", expectedDate));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasCorrectMvoAcceptDate(Date mvoEndDate) {
    return hasCorrectMvoAcceptDate(new Date(), mvoEndDate);
  }

  public static VerificationStep<ErxTask> hasCorrectMvoAcceptDate(
      Date signatureDate, @Nullable Date mvoEndDate) {
    val dc = DateConverter.getInstance();
    val calculator = new DateCalculator();
    val expectedDate =
        dc.truncate(
            (mvoEndDate != null)
                ? mvoEndDate
                : calculator.getDateAfterCalendarDays(signatureDate, 365));

    Predicate<ErxTask> predicate =
        task -> {
          val acceptDate = task.getAcceptDate();
          return calculator.equalDates(expectedDate, acceptDate);
        };

    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19445.getRequirement(),
            format("Das MVO E-Rezept muss als AcceptDate den Wert {0} haben", expectedDate));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasCorrectExpiryDate() {
    return hasCorrectExpiryDate(new Date());
  }

  public static VerificationStep<ErxTask> hasCorrectExpiryDate(Date signatureDate) {
    val calculator = new DateCalculator();
    val expectedDate = calculator.getDateAfterMonths(signatureDate, 3);

    Predicate<ErxTask> predicate =
        task -> {
          val expiryDate = task.getExpiryDate();
          return calculator.equalDates(expectedDate, expiryDate);
        };

    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19445.getRequirement(),
            format("Das E-Rezept muss als ExpiryDate den Wert {0} haben", expectedDate));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<ErxTask> hasCorrectAcceptDate(PrescriptionFlowType flowType) {
    return hasCorrectAcceptDate(new Date(), flowType);
  }

  public static VerificationStep<ErxTask> hasCorrectAcceptDate(
      Date signatureDate, PrescriptionFlowType flowType) {
    val dc = DateConverter.getInstance();
    val calculator = new DateCalculator();
    val expectedDate =
        dc.truncate(
            flowType.isPkvType()
                ? calculator.getDateAfterMonths(signatureDate, 3)
                : calculator.getDateAfterCalendarDays(signatureDate, 28));

    Predicate<ErxTask> predicate =
        task -> {
          val acceptDate = task.getAcceptDate();
          return calculator.equalDates(expectedDate, acceptDate);
        };

    val step =
        new VerificationStep.StepBuilder<ErxTask>(
            ErpAfos.A_19445.getRequirement(),
            format("Das E-Rezept muss als AcceptDate den Wert {0} haben", expectedDate));
    return step.predicate(predicate).accept();
  }
}
