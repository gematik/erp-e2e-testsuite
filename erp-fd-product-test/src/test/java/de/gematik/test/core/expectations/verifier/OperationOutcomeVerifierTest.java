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

import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHasDetailsText;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.operationOutcomeHintsDeviatingAuthoredOnDate;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationOutcomeVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldDetectMissingDetailsText() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    val step = operationOutcomeHasDetailsText("Test", ErpAfos.A_22487);

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }

  @Test
  void shouldDetectWrongDetailsText() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    oo.getIssueFirstRep().getDetails().setText("some weird details text");
    val step = operationOutcomeHasDetailsText("Test", ErpAfos.A_22487);

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }

  @Test
  void shouldPassOnCorrectDetailsText() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    oo.getIssueFirstRep().getDetails().setText("some weird details text");
    val step = operationOutcomeHasDetailsText("some weird details text", ErpAfos.A_22487);

    step.apply(oo);
  }

  @Test
  void shouldPassOnCorrectDetailsTextInSecondIssue() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    oo.addIssue().getDetails().setText("some weird details text");
    oo.addIssue().getDetails().setText("random other text");

    val step = operationOutcomeHasDetailsText("random other text", ErpAfos.A_22487);

    step.apply(oo);
  }

  @Test
  void shouldPassOnDeviatingAuthoredOn() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    oo.addIssue()
        .getDetails()
        .setText(
            "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich sein");

    val step = operationOutcomeHintsDeviatingAuthoredOnDate();

    step.apply(oo);
  }

  @Test
  void shouldFailOnWrongDeviatingAuthoredOn() {
    val oo = FhirTestResourceUtil.createOperationOutcome();
    // NOTE the exclamation mark at the end
    oo.addIssue()
        .getDetails()
        .setText(
            "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich sein!");

    val step = operationOutcomeHintsDeviatingAuthoredOnDate();

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }
}
