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

package de.gematik.test.erezept.fhirdump;

import java.time.*;
import java.util.*;
import lombok.*;
import net.thucydides.model.domain.DataTable;
import net.thucydides.model.domain.Story;
import net.thucydides.model.domain.TestOutcome;
import net.thucydides.model.domain.TestResult;
import net.thucydides.model.screenshots.ScreenshotAndHtmlSource;
import net.thucydides.model.steps.ExecutedStepDescription;
import net.thucydides.model.steps.StepFailure;
import net.thucydides.model.steps.StepListener;

public class FhirDumpStepListener implements StepListener {

  private final FhirDumper fhirDumper;

  public FhirDumpStepListener(FhirDumper fhirDumper) {
    this.fhirDumper = fhirDumper;
  }

  @Override
  public void testSuiteStarted(Class<?> aClass) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testSuiteStarted(Story story) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testSuiteFinished() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testStarted(String s) {
    // unfortunately we can't use this method here to initialize a started test because the first
    // test is skipped/missed
  }

  @Override
  public void testStarted(String s, String s1) {
    // unfortunately we can't use this method here to initialize a started test because the first
    // test is skipped/missed
  }

  @Override
  public void testStarted(String s, String s1, ZonedDateTime zonedDateTime) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testFinished(TestOutcome testOutcome) {
    fhirDumper
        .getCurrentScenario()
        .ifPresent(
            scenario -> {
              scenario.setName(testOutcome.getName());
              scenario.setDescription(testOutcome.getDescription());
              scenario.setFeatureFile(testOutcome.getUserStory().getPath());
              scenario.setFeature(testOutcome.getUserStory().getDisplayName());
            });

    fhirDumper.finishScenario(testOutcome);
  }

  @Override
  public void testFinished(TestOutcome testOutcome, boolean b, ZonedDateTime zonedDateTime) {
    this.testFinished(testOutcome);
  }

  @Override
  public void testFailed(TestOutcome testOutcome, Throwable throwable) {
    this.testFinished(testOutcome);
  }

  @Override
  public void testRetried() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepStarted(ExecutedStepDescription executedStepDescription) {
    fhirDumper
        .getCurrentScenario()
        .ifPresent(
            scenario -> {
              val step = new StepDump();
              step.setName(executedStepDescription.getName());
              scenario.getSteps().add(step);
            });
  }

  @Override
  public void skippedStepStarted(ExecutedStepDescription executedStepDescription) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepFailed(StepFailure stepFailure) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepFailed(
      StepFailure stepFailure,
      List<ScreenshotAndHtmlSource> list,
      boolean b,
      ZonedDateTime zonedDateTime) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void lastStepFailed(StepFailure stepFailure) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepIgnored() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepPending() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepPending(String s) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepFinished() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepFinished(List<ScreenshotAndHtmlSource> list) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void stepFinished(List<ScreenshotAndHtmlSource> list, ZonedDateTime zonedDateTime) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testIgnored() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testSkipped() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testPending() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testIsManual() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void notifyScreenChange() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void useExamplesFrom(DataTable dataTable) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void addNewExamplesFrom(DataTable dataTable) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void exampleStarted(Map<String, String> map) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void exampleFinished() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void assumptionViolated(String s) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void testRunFinished() {
    // not required for FhirDumpStepListener
  }

  @Override
  public void takeScreenshots(List<ScreenshotAndHtmlSource> list) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void takeScreenshots(TestResult testResult, List<ScreenshotAndHtmlSource> list) {
    // not required for FhirDumpStepListener
  }

  @Override
  public void recordScreenshot(String s, byte[] bytes) {
    // not required for FhirDumpStepListener
  }
}
