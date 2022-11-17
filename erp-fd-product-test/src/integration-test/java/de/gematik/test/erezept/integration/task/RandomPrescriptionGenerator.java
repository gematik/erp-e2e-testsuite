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

package de.gematik.test.erezept.integration.task;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.ThrowingConsumer;
import org.junit.runner.RunWith;

import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.*;
import static de.gematik.test.core.expectations.verifier.PrescriptionBundleVerifier.bundleHasValidAccessCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.*;

@Slf4j
@RunWith(SerenityRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Generate random Prescriptions")
@Tag("Smoketest")
@Disabled("Just a PoC for Dynamic Tests") // still need to figure out how this can be useful
class RandomPrescriptionGenerator extends ErpTest {

  @Actor(name = "Bernd Claudius")
  private DoctorActor bernd;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Actor(name = "Am Flughafen")
  private PharmacyActor flughafen;

  @TestcaseId("ERP_DYNAMIC_PRESCRIPTIONS_01")
  @TestFactory
  @DisplayName("Kompletter Lebenszyklus eines zufälligen E-Rezeptes")
  Stream<DynamicTest> generateDynamicRandomPrescriptions() {

    val iterations = Integer.parseInt(System.getProperty("SMOKE_ITERATIONS", "10"));

    val inputGenerator = IntStream.range(0, iterations).iterator();
    Function<Integer, String> displayNameGenerator = (input) -> "Iteration: " + input;
    ThrowingConsumer<Integer> testExecutor =
        (input) -> {
          // 1. Step: Task/$create creates a new task
          val creation =
              bernd.performs(TaskCreate.withFlowType(PrescriptionFlowType.FLOW_TYPE_160));
          bernd.attemptsTo(
              Verify.that(creation)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIsBetween(200, 210))
                  .isCorrect());

          // 2. Step: Task/$activate activates the task for the patient
          val draftTask = creation.getExpectedResponse();
          val prescriptionId = draftTask.getPrescriptionId();

          val activation =
              bernd.performs(
                  ActivatePrescription.forGiven(creation)
                      .withKbvBundle(
                          KbvErpBundleBuilder.faker(sina.getKvnr(), prescriptionId)
                              .patient(sina.getPatientData())
                              .insurance(sina.getInsuranceCoverage())
                              .build()));

          bernd.attemptsTo(
              Verify.that(activation)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIsBetween(200, 210))
                  .has(hasValidPrescriptionId())
                  .isCorrect());

          // 3. Step: patient fetches the task from FD via it's ID
          val getTaskInteraction = sina.asksFor(TheTask.fromBackend(draftTask));
          sina.attemptsTo(
              Verify.that(getTaskInteraction)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIsBetween(200, 210))
                  .and(bundleHasValidAccessCode())
                  .isCorrect());

          // 4. Step: Task/$accept accepts the task and provides a secret to the pharmacy
          val acceptInteraction =
              flughafen.performs(AcceptPrescription.forTheTaskFrom(getTaskInteraction));
          flughafen.attemptsTo(
              Verify.that(acceptInteraction)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIsBetween(200, 210))
                  .isCorrect());

          // 5. Step: Task/$close dispenses the prescription and closes the task
          val closeInteraction =
              flughafen.performs(DispensePrescription.acceptedWith(acceptInteraction));
          flughafen.attemptsTo(
              Verify.that(closeInteraction)
                  .withExpectedType()
                  .hasResponseWith(returnCodeIsBetween(200, 210))
                  .isCorrect());
        };

    return DynamicTest.stream(inputGenerator, displayNameGenerator, testExecutor);
  }
}
