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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.expectations.requirements.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.testutil.*;
import java.util.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.openqa.selenium.*;

class ErpResponseVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.throwsInvocationTargetException(ErpResponseVerifier.class));
  }

  @Test
  void returnCodeIsCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCode(200, ErpAfos.A_19514_02);
    step.apply(response);
  }

  @Test
  void returnCodeIsWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIs(201);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsNotCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIsNot(404);
    step.apply(response);
  }

  @Test
  void returnCodeIsNotWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIsNot(200);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsInCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIsIn(100, 200, 300);
    step.apply(response);
  }

  @Test
  void returnCodeIsInWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIsIn(List.of(100, 200, 300));
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsBetweenCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeIsBetween(200, 210);
    step.apply(response);
  }

  @Test
  void returnCodeIsBetweenWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = returnCodeBetween(201, 202);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void shouldThrowIfNoReturnCodesGiven() {
    List<Integer> emptyList = List.of();
    assertThrows(InvalidArgumentException.class, () -> returnCodeIsIn(emptyList));
  }

  @Test
  void payloadIsOfTypeCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsOfTypeWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = payloadIsOfType(OperationOutcome.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void payloadIsNotOfTypeCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsNotOfTypeWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleBuilder.faker().build(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    val step = payloadIsNotOfType(KbvErpBundle.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }
}
