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

package de.gematik.test.core.expectations;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.Requirement;
import de.gematik.test.core.extensions.ErpTestExtension;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.FhirTestResourceUtil;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(ErpTestExtension.class)
// @RunWith(SerenityRunner.class)
// @ExtendWith(SerenityJUnit5Extension.class)
class ErpResponseExpectationTest {

  @Test
  @TestcaseId("ut_expecation_01")
  @DisplayName("Positive Unit Test for an Expectation")
  void shouldPassExpectation() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation =
        ErpResponseExpectation.expectFor(response, KbvErpBundle.class)
            .hasResponseWith(returnCodeIsIn(200))
            .andResponse(payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19230))
            .andResponse(payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19230));

    expectation.ensure();
  }

  @Test
  @TestcaseId("ut_expecation_02")
  @DisplayName("Negative Unit Test for an Expectation")
  void shouldFailExpectation() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation =
        ErpResponseExpectation.expectFor(response, KbvErpBundle.class)
            .responseWith(returnCodeIsIn(200, 201))
            .andResponse(returnCodeIs(201))
            .andResponse(returnCodeIsBetween(200, 201))
            .andResponse(payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19230))
            .andResponse(payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19230));

    assertThrows(AssertionError.class, expectation::ensure);
  }

  @Test
  @TestcaseId("ut_expecation_03")
  @DisplayName("Expectation contains Requirements in toString")
  void shouldHaveRequirements() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation =
        ErpResponseExpectation.expectFor(response, KbvErpBundle.class)
            .hasResponseWith(returnCodeIsIn(200))
            .andResponse(payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19230))
            .andResponse(payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19230));

    // should contain every checked Requirement and filter out duplicates which should result in:
    assertTrue(expectation.toString().contains("(A_19514-03; A_19230)"));
  }

  @Test
  @TestcaseId("ut_expecation_04")
  @DisplayName("Expectation contains no Requirements in toString if no given")
  void shouldNotHaveRequirements() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation = ErpResponseExpectation.expectFor(response, KbvErpBundle.class);

    // no concrete Requirements were given
    assertTrue(expectation.toString().contains("(keine Anforderungen)"));
  }

  @Test
  @TestcaseId("ut_expecation_05")
  @DisplayName("Expectation contains no Requirements in toString if only custom requirements given")
  void shouldNotHaveCustomRequirements() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation =
        ErpResponseExpectation.expectFor(response, KbvErpBundle.class)
            // should filter out custom requirements (as long as we still have them)
            .andResponse(payloadIsNotOfType(OperationOutcome.class, Requirement.custom("custom")));

    // no concrete Requirements were given
    assertTrue(expectation.toString().contains("(keine Anforderungen)"));
  }

  @Test
  @TestcaseId("ut_expecation_06")
  @DisplayName(
      "AssertionError wird bei unerwartetem Payload immer geworfen, auch wenn nicht explizit"
          + " geprüft")
  void shouldThrowOnUnexpectedPayloadWithoutExplicitVerification() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val expectation =
        ErpResponseExpectation.expectFor(response, ErxTask.class).responseWith(returnCodeIs(200));

    assertThrows(AssertionError.class, expectation::ensure);
  }
}
