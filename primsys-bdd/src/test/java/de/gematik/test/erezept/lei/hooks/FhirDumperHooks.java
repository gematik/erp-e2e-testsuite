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

package de.gematik.test.erezept.lei.hooks;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhirdump.FhirDumpStepListener;
import de.gematik.test.erezept.fhirdump.FhirDumper;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.Scenario;
import lombok.extern.slf4j.Slf4j;
import net.thucydides.core.steps.StepEventBus;

@Slf4j
public class FhirDumperHooks {

  /* unfortunately we cannot register the step listener in @BeforeAll because serenity-bdd somehow overwrites
    this registration.
  */
  private static FhirDumper fhirDumper = FhirDumper.getInstance();
  private static FhirDumpStepListener stepListener =
      new FhirDumpStepListener(FhirDumper.getInstance());

  @AfterAll
  public static void writeFhirDumpSummary() {
    fhirDumper.writeDumpSummary();
  }

  @Before
  public void initFhirDumpScenario(Scenario scenario) {
    /*
    This is required because registering the steplistener in @BeforeAll is too early. Serenity-BDD
    seams to initialize the EventBus after @BeforeAll
    and thus overwriting the registration. Registering the same listener before each scenario
    ensures we have the listener in place when test execution starts.
    Note: instantiation in @BeforeAll is still required because otherwise we would register a new
    listener on each scenario resulting in X listeners after X scenarios
     */
    log.info(format("Initialize FHIR Dumper for Scenario {0}", scenario.getName()));
    StepEventBus.getEventBus()
        .registerListener(stepListener); // won't add multiple times because always the same object
    fhirDumper.startScenario(scenario);
  }
}
