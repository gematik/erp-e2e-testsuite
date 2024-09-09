/*
 * Copyright 2024 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.fhir.resources.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import lombok.val;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AcceptBundleVerifierTest {

  @BeforeEach
  void setupReporter() {
    // need to start a testcase manually as we are not using the ErpTestExtension here
    CoverageReporter.getInstance().startTestcase("not needed");
  }

  @Test
  void shouldNotInstantiate() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(AcceptBundleVerifier.class));
  }

  @Test
  void shouldPassForTaskInProgress() {
    val mockBundle = mock(ErxAcceptBundle.class);
    val mockTask = mock(ErxTask.class);
    when(mockTask.getStatus()).thenReturn(Task.TaskStatus.INPROGRESS);
    when(mockBundle.getTask()).thenReturn(mockTask);

    val step = AcceptBundleVerifier.isInProgressStatus();
    step.apply(mockBundle);
  }

  @Test
  void shouldFailForTaskNotInProgress() {
    val mockBundle = mock(ErxAcceptBundle.class);
    val mockTask = mock(ErxTask.class);
    when(mockTask.getStatus()).thenReturn(Task.TaskStatus.ACCEPTED);
    when(mockBundle.getTask()).thenReturn(mockTask);

    val step = AcceptBundleVerifier.isInProgressStatus();
    assertThrows(AssertionError.class, () -> step.apply(mockBundle));
  }

  @ParameterizedTest(name = "AcceptBundle is expected to have consent = {0}")
  @ValueSource(booleans = {true, false})
  void shouldPassWhenHasConsent(boolean shouldHave) {
    val mockBundle = mock(ErxAcceptBundle.class);
    when(mockBundle.hasConsent()).thenReturn(shouldHave);

    val step = AcceptBundleVerifier.consentIsPresent(shouldHave);
    step.apply(mockBundle);
  }

  @ParameterizedTest(name = "AcceptBundle is has consent = {0} but expectation is !{0}")
  @ValueSource(booleans = {true, false})
  void shouldFailWhenHasConsent(boolean shouldHave) {
    val mockBundle = mock(ErxAcceptBundle.class);
    when(mockBundle.hasConsent()).thenReturn(shouldHave);

    val step = AcceptBundleVerifier.consentIsPresent(!shouldHave);
    assertThrows(AssertionError.class, () -> step.apply(mockBundle));
  }
}
