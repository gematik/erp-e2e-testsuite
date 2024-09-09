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

package de.gematik.test.erezept.fhirdump;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.cucumber.java.Scenario;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import lombok.val;
import net.thucydides.model.domain.Story;
import net.thucydides.model.domain.TestOutcome;
import net.thucydides.model.domain.TestResult;
import net.thucydides.model.steps.ExecutedStepDescription;
import org.junit.jupiter.api.Test;

class FhirDumperTest {

  @Test
  void shouldHaveNoCurrentScenarioWithoutStart() {
    val basePath = Path.of("target", "fhir_dumps", "no_scenario_test");
    val d = new FhirDumper(basePath);
    assertTrue(d.getCurrentScenario().isEmpty());
  }

  @Test
  void shouldHaveCurrentScenarioAfterStart() {
    val basePath = Path.of("target", "fhir_dumps", "with_scenario_test");
    val d = new FhirDumper(basePath);
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn("hello-world-scenario");
    when(scenario.getSourceTagNames()).thenReturn(Collections.emptyList());
    d.startScenario(scenario);
    assertTrue(d.getCurrentScenario().isPresent());
  }

  @Test
  void shouldWriteDumpFile() {
    val basePath = Path.of("target", "fhir_dumps", "positive_test");
    val d = new FhirDumper(basePath);
    val stepListener = new FhirDumpStepListener(d);

    val scenarioName = "hello_world_scenario";
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn(scenarioName);
    when(scenario.getSourceTagNames()).thenReturn(Collections.emptyList());

    d.startScenario(scenario);
    val stepDescription = ExecutedStepDescription.withTitle("Sample Step description");
    stepListener.stepStarted(stepDescription);

    d.writeDump("TEST", "hello_world.txt", "some content");

    val to = mock(TestOutcome.class);
    val story = mock(Story.class);
    val result = mock(TestResult.class);
    when(to.getDescription()).thenReturn("Test description");
    when(to.getName()).thenReturn(scenarioName);
    when(to.getUserStory()).thenReturn(story);
    when(to.getResult()).thenReturn(result);
    when(story.getPath()).thenReturn("/a/b/c");
    when(result.getLabel()).thenReturn("Passed");

    stepListener.testFinished(to);

    val summaryFile = basePath.resolve("fhir_dump.json").toFile();
    val indexFile = basePath.resolve(Path.of("Allgemein", scenarioName, "index.json")).toFile();
    assertFalse(summaryFile.exists());
    assertFalse(indexFile.exists());
    d.writeDumpSummary();
    assertTrue(summaryFile.exists());
    assertTrue(indexFile.exists());
  }

  @Test
  void shouldWriteScenarioFileWithMainActor() {
    val basePath = Path.of("target", "fhir_dumps", "positive_test");
    val d = new FhirDumper(basePath);
    val stepListener = new FhirDumpStepListener(d);

    val scenarioName = "hello_world_scenario";
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn(scenarioName);
    when(scenario.getSourceTagNames())
        .thenReturn(
            List.of(
                "@Hauptdarsteller:Versicherter",
                "@AFO-ID:A_19019",
                "@AF-ID:A_18506",
                "@AF-ID:A_18822"));

    d.startScenario(scenario);
    val stepDescription = ExecutedStepDescription.withTitle("Sample Step description");
    stepListener.stepStarted(stepDescription);

    d.writeDump("TEST", "hello_world.txt", "some content");

    val to = mock(TestOutcome.class);
    val story = mock(Story.class);
    val result = mock(TestResult.class);
    when(to.getDescription()).thenReturn("Test description");
    when(to.getName()).thenReturn(scenarioName);
    when(to.getUserStory()).thenReturn(story);
    when(to.getResult()).thenReturn(result);
    when(story.getPath()).thenReturn(format("Versicherter/{0}", scenarioName));
    when(result.getLabel()).thenReturn("Passed");

    stepListener.testFinished(to);

    val summaryFile = basePath.resolve("fhir_dump.json").toFile();
    val indexFile = basePath.resolve(Path.of("Versicherter", scenarioName, "index.json")).toFile();
    assertFalse(summaryFile.exists());
    assertFalse(indexFile.exists());
    d.writeDumpSummary();
    assertTrue(summaryFile.exists());
    assertTrue(indexFile.exists());
  }

  @Test
  void shouldNotWriteScenarioIndexFileWithoutSteps() {
    val basePath = Path.of("target", "fhir_dumps", "test_no_steps");
    val d = new FhirDumper(basePath);

    val scenarioName = "hello-world-scenario";
    val scenario = mock(Scenario.class);
    when(scenario.getId()).thenReturn("123");
    when(scenario.getName()).thenReturn(scenarioName);
    when(scenario.getSourceTagNames()).thenReturn(Collections.emptyList());

    d.startScenario(scenario);
    d.getCurrentScenario()
        .ifPresent(
            s -> {
              s.setDescription("Hello World is a sample scenario");
            });
    val scenarioPath = basePath.resolve("hello-world-scenario");

    d.writeDump("TEST", "hello_world.txt", "some content");
    assertFalse(scenarioPath.toFile().exists()); // not written!!

    val to = new TestOutcome("test");
    d.finishScenario(to);

    val summaryFile = basePath.resolve("fhir_dump.json").toFile();
    assertFalse(summaryFile.exists());
    d.writeDumpSummary();
    assertTrue(summaryFile.exists());
    assertFalse(scenarioPath.resolve("index.json").toFile().exists());
  }

  @Test
  void shouldRepeatedlyWriteToSameDirectory() {
    assertDoesNotThrow(this::shouldWriteDumpFile);
    assertDoesNotThrow(this::shouldWriteDumpFile);
  }
}
