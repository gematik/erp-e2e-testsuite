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

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static de.gematik.test.core.expectations.verifier.OperationOutcomeVerifier.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OperationOutcomeVerifierTest {

  private static final String DIAGNOSTICS_IKNR =
      "Bundle.entry[4].resource{Coverage}.payor[0].identifier.value: error: -for-LaengeIK: Die"
          + " IK-Nummer muss 9-stellig sein. (from profile:"
          + " https://fhir.kbv.de/StructureDefinition/KBV_PR_FOR_Coverage|1.1.0);";

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(OperationOutcomeVerifier.class));
  }

  @Test
  void shouldDetectMissingDetailsText() {
    val oo = createOperationOutcome();
    val step = operationOutcomeHasDetailsText("Test", ErpAfos.A_22487);

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }

  @Test
  void shouldDetectWrongDetailsText() {
    val oo = createOperationOutcome();
    oo.getIssueFirstRep().getDetails().setText("some weird details text");
    val step = operationOutcomeHasDetailsText("Test", ErpAfos.A_22487);

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }

  @Test
  void shouldPassOnCorrectDetailsText() {
    val oo = createOperationOutcome();
    oo.getIssueFirstRep().getDetails().setText("some weird details text");
    val step = operationOutcomeHasDetailsText("some weird details text", ErpAfos.A_22487);

    step.apply(oo);
  }

  @Test
  void shouldPassOnCorrectDetailsTextInSecondIssue() {
    val oo = createOperationOutcome();
    oo.addIssue().getDetails().setText("some weird details text");
    oo.addIssue().getDetails().setText("random other text");

    val step = operationOutcomeHasDetailsText("random other text", ErpAfos.A_22487);

    step.apply(oo);
  }

  @Test
  void shouldPassOnDeviatingAuthoredOn() {
    val oo = createOperationOutcome();
    oo.addIssue()
        .getDetails()
        .setText(
            "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich"
                + " sein");

    val step = operationOutcomeHintsDeviatingAuthoredOnDate();

    step.apply(oo);
  }

  @Test
  void shouldFailOnWrongDeviatingAuthoredOn() {
    val oo = createOperationOutcome();
    // NOTE the exclamation mark at the end
    oo.addIssue()
        .getDetails()
        .setText(
            "Ausstellungsdatum und Signaturzeitpunkt weichen voneinander ab, müssen aber taggleich"
                + " sein!");

    val step = operationOutcomeHintsDeviatingAuthoredOnDate();

    assertThrows(AssertionError.class, () -> step.apply(oo));
  }

  @Test
  void shouldVerifyDiagnosticsContains() {
    val oOResource = createOperationOutcome();
    oOResource.getIssueFirstRep().setDiagnostics(DIAGNOSTICS_IKNR);
    val step =
        operationOutcomeContainsInDiagnostics("Die IK-Nummer muss 9-stellig sein", ErpAfos.A_23888);
    step.apply(oOResource);
  }

  @Test
  void shouldDetectInvalidDiagnostics() {
    val oOResource = createOperationOutcome();
    oOResource.getIssueFirstRep().setDiagnostics("random invalid diagnostics text");
    val step =
        operationOutcomeContainsInDiagnostics("Die IK-Nummer muss 9-stellig sein", ErpAfos.A_23888);
    assertThrows(AssertionError.class, () -> step.apply(oOResource));
  }

  @Test
  void shouldVerifyWithMissingDiagnostics() {
    val oOResource = createOperationOutcome();
    oOResource.getIssueFirstRep().setDiagnostics(null);
    val step =
        operationOutcomeContainsInDiagnostics("Die IK-Nummer muss 9-stellig sein", ErpAfos.A_23888);
    assertThrows(AssertionError.class, () -> step.apply(oOResource));
  }

  @Test
  void shouldVerifyDetailedTextContains() {
    val oOResource = createOperationOutcome();
    oOResource.addIssue().getDetails().setText("FHIR-Validation error");
    val step = operationOutcomeContainsInDetailText("FHIR-Validation error", ErpAfos.A_23888);
    step.apply(oOResource);
  }

  @Test
  void shouldVerify_oOHasInDetailsTextOneOf() {
    val oOResource = createOperationOutcome();
    oOResource.addIssue().getDetails().setText("FHIR-Validation error");
    val step = hasAnyOfDetailsText(ErpAfos.A_23888, "FHIR-Validation error", "nonsenseError");
    step.apply(oOResource);
  }

  @Test
  void shouldThrowWhileVerify_oOHasInDetailsTextOneOf() {
    val oOResource = createOperationOutcome();
    oOResource.addIssue().getDetails().setText("nonsense text");
    val step = hasAnyOfDetailsText(ErpAfos.A_23888, "FHIR-Validation error");
    assertThrows(AssertionError.class, () -> step.apply(oOResource));
  }

  @Test
  void shouldThrowWhileVerifyWithoutDetailsText_oOHasInDetailsTextOneOf() {
    val oOResource = createOperationOutcome();
    oOResource.getIssueFirstRep().getDetails().setText(null);
    val step = hasAnyOfDetailsText(ErpAfos.A_23888, "FHIR-Validation error");
    assertThrows(AssertionError.class, () -> step.apply(oOResource));
  }
}
