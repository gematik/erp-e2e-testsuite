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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.erp.ErxTaskBundle;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

public class TaskBundleVerifier {

  private TaskBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxTaskBundle> doesNotContainQES(ErpAfos afo) {
    Predicate<Resource> isErxTask =
        resource ->
            resource instanceof Task && ErpWorkflowStructDef.TASK.matches(resource.getMeta());
    Predicate<ErxTaskBundle> onlyErxTaskInEntries =
        bundle ->
            bundle.getEntry().stream()
                .map(Bundle.BundleEntryComponent::getResource)
                .allMatch(isErxTask);
    Predicate<ErxTaskBundle> noContainedResources =
        bundle ->
            bundle.getTasks().stream()
                .flatMap(it -> it.getContained().stream())
                .findAny()
                .isEmpty();

    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            afo.getRequirement(),
            format(
                "Das  ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf keine"
                    + " QES (Binary) zu den E-Rezepten enthalten."));
    return step.predicate(onlyErxTaskInEntries.and(noContainedResources)).accept();
  }

  public static VerificationStep<ErxTaskBundle> authoredOnDateIsEqual(LocalDate date) {
    return verifyAuthoredOnDateWithPredicate(
        ld -> ld.isEqual(date),
        "Die enthaltenen Tasks müssen das AuthoredOn Datum " + date.toString() + " enthalten");
  }

  /**
   * @param localDatePredicate // example: ld -> ld.isBefore(LocalDate.now())
   * @param description // the description of expected behavior as String
   * @return VerificationStep
   */
  public static VerificationStep<ErxTaskBundle> verifyAuthoredOnDateWithPredicate(
      Predicate<LocalDate> localDatePredicate, String description) {
    Predicate<ErxTaskBundle> predicate =
        bundle ->
            bundle.getTasks().stream()
                .map(task -> DateConverter.getInstance().dateToLocalDate(task.getAuthoredOn()))
                .allMatch(localDatePredicate);
    return new VerificationStep.StepBuilder<ErxTaskBundle>(ErpAfos.A_25515, description)
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<ErxTaskBundle> containsExclusivelyTasksWithGKVInsuranceType() {
    Predicate<ErxTaskBundle> verify =
        bundle -> bundle.getTasks().stream().allMatch(task -> task.getFlowType().isGkvType());
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            ErpAfos.A_23452.getRequirement(),
            format(
                "Das ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf nur"
                    + " E-Rezepte mit Workflow 160 enthalten."));
    return step.predicate(verify).accept();
  }

  public static VerificationStep<ErxTaskBundle> doesNotContainExpiredTasks(ErpAfos afo) {
    val compareDate = Date.from(LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant());
    Predicate<ErxTaskBundle> verify =
        bundle ->
            bundle.getTasks().stream().noneMatch(it -> it.getExpiryDate().before(compareDate));
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            afo.getRequirement(),
            format(
                "Das ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf keine"
                    + " abgelaufenen E-Rezepte enthalten."));
    return step.predicate(verify).accept();
  }

  public static VerificationStep<ErxTaskBundle> hasNoTasks() {
    Predicate<ErxTaskBundle> verify = bundle -> bundle.getTasks().isEmpty();
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            ErpAfos.A_23452.getRequirement(),
            format(
                "Das ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf nur"
                    + " E-Rezepte des Patienten mit der KVNR aus dem Prüfungsnachweis enthalten."));
    return step.predicate(verify).accept();
  }

  public static VerificationStep<ErxTaskBundle> containsOnlyTasksWith(
      Task.TaskStatus status, ErpAfos req) {
    Predicate<ErxTaskBundle> verify =
        bundle -> bundle.getTasks().stream().allMatch(it -> it.getStatus().equals(status));
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            req.getRequirement(),
            format(
                "Im Bundle dürfen nur Task mit Status {0} enthalten sein. (Task.status = \"{0}\")",
                status.getDisplay()));
    return step.predicate(verify).accept();
  }

  public static VerificationStep<ErxTaskBundle> containsOnlyTasksFor(KVNR kvnr, ErpAfos req) {
    Predicate<ErxTaskBundle> verify =
        bundle ->
            bundle.getTasks().stream()
                .allMatch(
                    it ->
                        it.getForKvnr()
                            .map(egkKvnr -> egkKvnr.getValue().equals(kvnr.getValue()))
                            .orElse(false));
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            req.getRequirement(),
            format("Im Bundle dürfen nur Task für die KVNR {0} enthalten sein", kvnr.getValue()));
    return step.predicate(verify).accept();
  }

  public static VerificationStep<ErxTaskBundle> containsOnlyTasksWith(
      PrescriptionFlowType prescriptionFlowType, ErpAfos req) {
    Predicate<ErxTaskBundle> verify =
        bundle ->
            bundle.getTasks().stream()
                .allMatch(it -> it.getFlowType().equals(prescriptionFlowType));
    val step =
        new VerificationStep.StepBuilder<ErxTaskBundle>(
            req.getRequirement(),
            format(
                "Im Bundle dürfen nur Task mit Workflowtype {0} enthalten sein",
                prescriptionFlowType.getCode()));
    return step.predicate(verify).accept();
  }
}
