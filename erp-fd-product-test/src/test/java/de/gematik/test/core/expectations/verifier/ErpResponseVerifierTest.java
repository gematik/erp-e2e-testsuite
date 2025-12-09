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
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.InvalidArgumentException;

class ErpResponseVerifierTest extends ErpFhirBuildingTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(ErpResponseVerifier.class));
  }

  @Test
  void returnCodeIsCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCode(200, ErpAfos.A_19514);
    step.apply(response);
  }

  @Test
  void returnCodeIsWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIs(201);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsNotCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIsNot(404);
    step.apply(response);
  }

  @Test
  void returnCodeIsNotWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIsNot(200);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsInCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIsIn(100, 200, 300);
    step.apply(response);
  }

  @Test
  void headerShouldContain() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(123)
            .withHeaders(Map.of("Warning", "123 und Zahlen und so weiter"))
            .andValidationResult(createEmptyValidationResult());
    val step = headerContentContains("Warning", "123 und Zahlen und so weiter", ErpAfos.A_23891);
    step.apply(response);
  }

  @Test
  void headerShouldNotContain() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(123)
            .withHeaders(Map.of("Warning", "123 und Zahlen und so weiter"))
            .andValidationResult(createEmptyValidationResult());
    val step = headerContentContains("Warning", "nee nix passendes drin !", ErpAfos.A_23891);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsInWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(201)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIsIn(List.of(100, 200, 300));
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsBetweenCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = returnCodeIsBetween(200, 210);
    step.apply(response);
  }

  @Test
  void returnCodeIsBetweenWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
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
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsOfTypeWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = payloadIsOfType(OperationOutcome.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void payloadIsNotOfTypeCorrectTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsNotOfTypeWrongTest() {
    val response =
        ErpResponse.forPayload(KbvErpBundleFaker.builder().fake(), KbvErpBundle.class)
            .withStatusCode(200)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    val step = payloadIsNotOfType(KbvErpBundle.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }
}
