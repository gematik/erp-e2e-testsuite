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

package de.gematik.test.erezept.fhirdump;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.*;
import java.util.*;
import lombok.*;
import net.thucydides.core.model.*;
import net.thucydides.core.steps.*;
import org.junit.jupiter.api.*;

class FhirDumpStepListenerTest {

  @Test
  void shouldNotFailOnUnimplementedMethods() {
    // mainly for reaching code coverage, no real value from these tests anyway
    val listener = new FhirDumpStepListener(FhirDumper.getInstance());
    assertDoesNotThrow(() -> listener.testSuiteStarted(mock(Story.class)));
    assertDoesNotThrow(() -> listener.testSuiteStarted(listener.getClass()));
    assertDoesNotThrow(listener::testSuiteFinished);
    assertDoesNotThrow(() -> listener.testStarted("hello"));
    assertDoesNotThrow(() -> listener.testStarted("hello", "world"));
    assertDoesNotThrow(() -> listener.testStarted("hello", "world", ZonedDateTime.now()));
    assertDoesNotThrow(
        () -> listener.testFinished(mock(TestOutcome.class), false, ZonedDateTime.now()));
    assertDoesNotThrow(listener::testRetried);
    assertDoesNotThrow(() -> listener.skippedStepStarted(mock(ExecutedStepDescription.class)));
    assertDoesNotThrow(() -> listener.stepFailed(mock(StepFailure.class)));
    assertDoesNotThrow(() -> listener.lastStepFailed(mock(StepFailure.class)));
    assertDoesNotThrow(listener::stepIgnored);
    assertDoesNotThrow(() -> listener.stepPending());
    assertDoesNotThrow(() -> listener.stepPending("hello"));
    assertDoesNotThrow(() -> listener.stepFinished());
    assertDoesNotThrow(() -> listener.stepFinished(List.of()));
    assertDoesNotThrow(() -> listener.testFailed(mock(TestOutcome.class), new Exception("")));
    assertDoesNotThrow(listener::testIgnored);
    assertDoesNotThrow(listener::testSkipped);
    assertDoesNotThrow(listener::testPending);
    assertDoesNotThrow(listener::testIsManual);
    assertDoesNotThrow(listener::notifyScreenChange);
    assertDoesNotThrow(() -> listener.useExamplesFrom(DataTable.withHeaders(List.of()).build()));
    assertDoesNotThrow(() -> listener.addNewExamplesFrom(DataTable.withHeaders(List.of()).build()));
    assertDoesNotThrow(() -> listener.exampleStarted(Map.of()));
    assertDoesNotThrow(listener::exampleFinished);
    assertDoesNotThrow(() -> listener.assumptionViolated("hello"));
    assertDoesNotThrow(() -> listener.takeScreenshots(List.of()));
  }
}
