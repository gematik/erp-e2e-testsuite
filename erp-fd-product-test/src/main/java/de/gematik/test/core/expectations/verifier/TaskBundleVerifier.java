/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxTaskBundle;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;

public class TaskBundleVerifier {

  private TaskBundleVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<ErxTaskBundle> doesContainsErxTasksWithoutQES() {
    Predicate<Resource> isErxTask =
        resource ->
            resource instanceof Task && ErpWorkflowStructDef.TASK_12.match(resource.getMeta());
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
            ErpAfos.A_23452.getRequirement(),
            format(
                "Das  ErxTaskBundle, abgerufen über Egk in der Apotheke als Apotheker, darf keine"
                    + " QES (Binary) zu den E-Rezepten enthalten."));
    return step.predicate(onlyErxTaskInEntries.and(noContainedResources)).accept();
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
}
