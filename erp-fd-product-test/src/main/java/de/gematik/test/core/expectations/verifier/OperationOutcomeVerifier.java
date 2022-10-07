/*
 * Copyright (c) 2022 gematik GmbH
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
import de.gematik.test.core.expectations.requirements.RequirementsSet;
import java.util.function.Predicate;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;

public class OperationOutcomeVerifier {

  private OperationOutcomeVerifier() {
    throw new AssertionError("do not instantiate!");
  }

  public static VerificationStep<OperationOutcome> operationOutcomeHintsDeviatingAuthoredOnDate() {
    val detailsText =
        "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich sein";
    return operationOutcomeHasDetailsText(detailsText, ErpAfos.A_22487);
  }

  public static VerificationStep<OperationOutcome> operationOutcomeHasDetailsText(
      String text, RequirementsSet req) {
    Predicate<OperationOutcome> predicate =
        oo -> oo.getIssue().stream().anyMatch(issue -> text.equals(issue.getDetails().getText()));

    val step =
        new VerificationStep.StepBuilder<OperationOutcome>(
            req.getRequirement(),
            format("Die Warnung ''{0}'' muss im OperationOutcome enthalten sein", text));
    return step.predicate(predicate).accept();
  }
}
