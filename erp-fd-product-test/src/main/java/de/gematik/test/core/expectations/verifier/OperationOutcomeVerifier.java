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

package de.gematik.test.core.expectations.verifier;

import static java.text.MessageFormat.format;

import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import java.util.Arrays;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;

public class OperationOutcomeVerifier {

  private static final String EXPECTATION_TEMPLATE =
      "Die Warnung ''{0}'' muss im OperationOutcome enthalten sein";

  private OperationOutcomeVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<OperationOutcome> operationOutcomeHintsDeviatingAuthoredOnDate() {
    val detailsText =
        "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich"
            + " sein";
    return operationOutcomeHasDetailsText(detailsText, ErpAfos.A_22487);
  }

  public static VerificationStep<OperationOutcome> operationOutcomeHasDetailsText(
      String text, RequirementsSet req) {
    Predicate<OperationOutcome> predicate =
        oo -> oo.getIssue().stream().anyMatch(issue -> text.equals(issue.getDetails().getText()));

    val step =
        new VerificationStep.StepBuilder<OperationOutcome>(
            req.getRequirement(), format(EXPECTATION_TEMPLATE, text));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<OperationOutcome> operationOutcomeContainsInDetailText(
      String text, RequirementsSet req) {
    Predicate<OperationOutcome> predicate =
        oo -> oo.getIssue().stream().anyMatch(issue -> issue.getDetails().getText().contains(text));

    val step =
        new VerificationStep.StepBuilder<OperationOutcome>(
            req.getRequirement(), format(EXPECTATION_TEMPLATE, text));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<OperationOutcome> operationOutcomeContainsInDiagnostics(
      String text, RequirementsSet req) {
    Predicate<OperationOutcome> predicate =
        oo ->
            oo.getIssue().stream()
                .anyMatch(issue -> issue.hasDiagnostics() && issue.getDiagnostics().contains(text));

    val step =
        new VerificationStep.StepBuilder<OperationOutcome>(
            req.getRequirement(), format(EXPECTATION_TEMPLATE, text));
    return step.predicate(predicate).accept();
  }

  public static VerificationStep<OperationOutcome> hasAnyOfDetailsText(
      RequirementsSet req, String... texts) {
    Predicate<OperationOutcome> predicate =
        oo ->
            oo.getIssue().stream()
                .anyMatch(
                    issue -> {
                      if (issue.hasDetails()) {
                        return Arrays.stream(texts)
                            .anyMatch(text -> issue.getDetails().getText().contains(text));
                      }
                      return false;
                    });
    val step =
        new VerificationStep.StepBuilder<OperationOutcome>(
            req.getRequirement(), format(EXPECTATION_TEMPLATE, texts));
    return step.predicate(predicate).accept();
  }
}
