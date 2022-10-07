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

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import java.util.List;
import java.util.Map;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.InvalidArgumentException;

class ErpResponseVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void returnCodeIsCorrectTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCode(200, ErpAfos.A_19514_02);
    step.apply(response);
  }

  @Test
  void returnCodeIsWrongTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIs(201);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsNotCorrectTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIsNot(404);
    step.apply(response);
  }

  @Test
  void returnCodeIsNotWrongTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIsNot(200);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsInCorrectTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIsIn(100, 200, 300);
    step.apply(response);
  }

  @Test
  void returnCodeIsInWrongTest() {
    val response = new ErpResponse(201, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIsIn(List.of(100, 200, 300));
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void returnCodeIsBetweenCorrectTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = returnCodeIsBetween(200, 210);
    step.apply(response);
  }

  @Test
  void returnCodeIsBetweenWrongTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
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
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = payloadIsOfType(KbvErpBundle.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsOfTypeWrongTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = payloadIsOfType(OperationOutcome.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }

  @Test
  void payloadIsNotOfTypeCorrectTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = payloadIsNotOfType(OperationOutcome.class, ErpAfos.A_19022);
    step.apply(response);
  }

  @Test
  void payloadIsNotOfTypeWrongTest() {
    val response = new ErpResponse(200, Map.of(), KbvErpBundleBuilder.faker("X12345673").build());
    val step = payloadIsNotOfType(KbvErpBundle.class, ErpAfos.A_19022);
    assertThrows(AssertionError.class, () -> step.apply(response));
  }
}
