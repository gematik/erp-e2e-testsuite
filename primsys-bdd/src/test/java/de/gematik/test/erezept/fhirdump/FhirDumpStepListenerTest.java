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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import io.cucumber.java.Scenario;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import lombok.val;
import net.thucydides.core.model.DataTable;
import net.thucydides.core.model.Story;
import net.thucydides.core.model.TestOutcome;
import net.thucydides.core.model.TestResult;
import net.thucydides.core.model.features.ApplicationFeature;
import net.thucydides.core.steps.ExecutedStepDescription;
import net.thucydides.core.steps.StepFailure;
import org.junit.jupiter.api.Test;

class FhirDumpStepListenerTest {

  @Test
  @SuppressWarnings({"java:S5961"})
  void shouldNotFailOnUnimplementedMethods() {
    // mainly for reaching code coverage, no real value from these tests anyway
    val listener = new FhirDumpStepListener(FhirDumper.getInstance());
    assertDoesNotThrow(() -> listener.testSuiteStarted(mock(Story.class)));
    assertDoesNotThrow(() -> listener.testSuiteStarted(listener.getClass()));
    assertDoesNotThrow(listener::testSuiteFinished);
    assertDoesNotThrow(() -> listener.testStarted("hello"));
    assertDoesNotThrow(() -> listener.testStarted("hello", "world"));
    assertDoesNotThrow(() -> listener.testStarted("hello", "world", ZonedDateTime.now()));
    assertDoesNotThrow(listener::testRetried);
    assertDoesNotThrow(() -> listener.skippedStepStarted(mock(ExecutedStepDescription.class)));
    assertDoesNotThrow(() -> listener.stepFailed(mock(StepFailure.class)));
    assertDoesNotThrow(() -> listener.stepFailed(mock(StepFailure.class), List.of()));
    assertDoesNotThrow(() -> listener.lastStepFailed(mock(StepFailure.class)));
    assertDoesNotThrow(listener::stepIgnored);
    assertDoesNotThrow(() -> listener.stepPending());
    assertDoesNotThrow(() -> listener.stepPending("hello"));
    assertDoesNotThrow(() -> listener.stepFinished());
    assertDoesNotThrow(() -> listener.stepFinished(List.of()));
    assertDoesNotThrow(listener::testIgnored);
    assertDoesNotThrow(listener::testSkipped);
    assertDoesNotThrow(listener::testPending);
    assertDoesNotThrow(listener::testIsManual);
    assertDoesNotThrow(listener::notifyScreenChange);
    assertDoesNotThrow(() -> listener.useExamplesFrom(DataTable.withHeaders(List.of()).build()));
    assertDoesNotThrow(() -> listener.addNewExamplesFrom(DataTable.withHeaders(List.of()).build()));
    assertDoesNotThrow(() -> listener.exampleStarted(Map.of()));
    assertDoesNotThrow(listener::exampleFinished);
    assertDoesNotThrow(listener::testRunFinished);
    assertDoesNotThrow(() -> listener.assumptionViolated("hello"));
    assertDoesNotThrow(() -> listener.takeScreenshots(List.of()));
    assertDoesNotThrow(() -> listener.takeScreenshots(TestResult.SUCCESS, List.of()));
  }

  @Test
  void shouldForwardZonedTestFinished() {
    val d = FhirDumper.getInstance();

    val scenarioName = "hello_world_scenario";
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn(scenarioName);
    when(scenario.getSourceTagNames()).thenReturn(Collections.emptyList());
    d.startScenario(scenario);

    val listener = spy(new FhirDumpStepListener(d));

    val appFeature = new ApplicationFeature("id", "name");
    val userStory =
        new Story("id", "name", "storyclassname", "display_name", "a/b/c", appFeature);
    val testOutcome = new TestOutcome("testname");
    testOutcome.setDescription("description");
    testOutcome.setUserStory(userStory);
    val zdt = ZonedDateTime.now();

    assertDoesNotThrow(() -> listener.testFinished(testOutcome, false, zdt));
    verify(listener, times(1)).testFinished(testOutcome);
  }

  @Test
  void shouldForwardTestFailed() {
    val d = FhirDumper.getInstance();

    val scenarioName = "hello_world_scenario";
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn(scenarioName);
    when(scenario.getSourceTagNames()).thenReturn(Collections.emptyList());
    d.startScenario(scenario);

    val listener = spy(new FhirDumpStepListener(d));

    val appFeature = new ApplicationFeature("id", "name");
    val userStory =
        new Story("id", "name", "storyclassname", "display_name", "a/b/c", appFeature);
    val testOutcome = new TestOutcome("testname");
    testOutcome.setDescription("description");
    testOutcome.setUserStory(userStory);

    assertDoesNotThrow(() -> listener.testFailed(testOutcome, new NullPointerException()));
    verify(listener, times(1)).testFinished(testOutcome);
  }
}
