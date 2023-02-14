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

package de.gematik.test.erezept.integration.task;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.fuzzing.core.NamedEnvelope;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.ActorStage;
import de.gematik.test.erezept.actors.ActorType;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

import java.util.function.Function;
import java.util.stream.Stream;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsIn;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.*;
import static java.text.MessageFormat.format;
import static org.junit.jupiter.params.provider.EnumSource.Mode.EXCLUDE;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Task für ein E-Rezept erstellen")
class TaskCreateUsecase extends ErpTest {

  @TestcaseId("ERP_TASK_CREATE_01")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt erstellt Task mit WorkFlow {0}")
  @DisplayName("Erstelle einen E-Rezept Task als Verordnender Arzt")
  @EnumSource(
      value = PrescriptionFlowType.class,
      mode = EXCLUDE,
      names = {"FLOW_TYPE_209"})
  void createTask(PrescriptionFlowType flowType) {
    val doctor = this.getDoctorNamed("Bernd Claudius");
    val creation = doctor.performs(TaskCreate.withFlowType(flowType));
    doctor.attemptsTo(
        Verify.that(creation)
            .withExpectedType(ErpAfos.A_19018)
            .hasResponseWith(returnCode(201))
            .and(hasWorkflowType(flowType))
            .and(isInDraftStatus())
            .and(hasValidPrescriptionId())
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_CREATE_02")
  @ParameterizedTest(name = "[{index}] -> {0} erstellt Task mit WorkFlow {1}")
  @DisplayName("Nur ein Verordnender Arzt darf E-Rezepte aktivieren")
  @MethodSource("taskCreateProvider")
  void createTaskAsImproperActor(
      NamedEnvelope<Function<ActorStage, ErpActor>> actorGetter, PrescriptionFlowType flowType) {
    val actor = actorGetter.getParameter().apply(this.stage);
    val creation = actor.performs(TaskCreate.withFlowType(flowType));
    actor.attemptsTo(
        Verify.that(creation)
            .withOperationOutcome(ErpAfos.A_19018)
            .responseWith(returnCodeIsIn(403, 410))
            .isCorrect());
  }

  static Stream<Arguments> taskCreateProvider() {
    return ArgumentComposer.composeWith()
        .arguments(
            NamedEnvelope.of(
                format("{0} Sina Hüllmann", ActorType.PATIENT),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPatientNamed("Sina Hüllmann")),
            PrescriptionFlowType.FLOW_TYPE_160)
        .arguments(
            NamedEnvelope.of(
                format("{0} Sina Hüllmann", ActorType.PATIENT),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPatientNamed("Sina Hüllmann")),
            PrescriptionFlowType.FLOW_TYPE_169)
        .arguments(
            NamedEnvelope.of(
                format("{0} Sina Hüllmann", ActorType.PATIENT),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPatientNamed("Sina Hüllmann")),
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            NamedEnvelope.of(
                format("{0} Sina Hüllmann", ActorType.PATIENT),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPatientNamed("Sina Hüllmann")),
            PrescriptionFlowType.FLOW_TYPE_209)
        .arguments(
            NamedEnvelope.of(
                format("{0} Am Flughafen", ActorType.PHARMACY),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPharmacyNamed("Am Flughafen")),
            PrescriptionFlowType.FLOW_TYPE_160)
        .arguments(
            NamedEnvelope.of(
                format("{0} Am Flughafen", ActorType.PHARMACY),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPharmacyNamed("Am Flughafen")),
            PrescriptionFlowType.FLOW_TYPE_169)
        .arguments(
            NamedEnvelope.of(
                format("{0} Am Flughafen", ActorType.PHARMACY),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPharmacyNamed("Am Flughafen")),
            PrescriptionFlowType.FLOW_TYPE_200)
        .arguments(
            NamedEnvelope.of(
                format("{0} Am Flughafen", ActorType.PHARMACY),
                (Function<ActorStage, ErpActor>) (stage) -> stage.getPharmacyNamed("Am Flughafen")),
            PrescriptionFlowType.FLOW_TYPE_209)
        .create();
  }
}
