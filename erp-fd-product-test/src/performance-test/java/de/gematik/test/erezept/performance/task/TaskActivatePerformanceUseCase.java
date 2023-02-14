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


package de.gematik.test.erezept.performance.task;


import static java.text.MessageFormat.*;

import de.gematik.test.core.*;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.*;
import de.gematik.test.core.expectations.requirements.*;
import de.gematik.test.core.expectations.verifier.*;
import de.gematik.test.erezept.*;
import de.gematik.test.erezept.actions.*;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import de.gematik.test.konnektor.*;
import java.time.*;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import lombok.*;
import lombok.extern.slf4j.*;
import net.serenitybdd.junit.runners.*;
import net.serenitybdd.junit5.*;
import net.serenitybdd.screenplay.*;
import net.thucydides.core.annotations.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;
import org.junit.runner.*;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Messung E-Rezept Aktivierung")
class TaskActivatePerformanceUseCase extends ErpTest {

  @Actor(name = "Bernd Claudius")
  private DoctorActor bernd;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  static Stream<Arguments> prescriptionTypesProvider() {

    return ArgumentComposer.composeWith()
        .arguments(
            VersicherungsArtDeBasis.GKV, // given insurance kind
            PrescriptionAssignmentKind.PHARMACY_ONLY, // given assignment kind
            DurationOperationRequirement.ACTIVATE_FLOWTYPE_160) // expected flow type
        .arguments(
            VersicherungsArtDeBasis.GKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            DurationOperationRequirement.ACTIVATE_FLOWTYPE_169)
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.PHARMACY_ONLY,
            DurationOperationRequirement.ACTIVATE_FLOWTYPE_200)
        .create();
  }

  @TestcaseId("ERP_PERFORMANCE_TASK_ACTIVATE_01")
  @ParameterizedTest(name = "[{index}] -> Verordnender Arzt stellt ein {0} E-Rezept für {1} aus")
  @DisplayName("E-Rezept als Verordnender Arzt an eine/n Versicherte/n ausstellen")
  @MethodSource("prescriptionTypesProvider")
  @Tag("ERP_PERFORMANCE_TASK_ACTIVATE_01")
  void activatePrescription(
      VersicherungsArtDeBasis insuranceType,
      PrescriptionAssignmentKind assignmentKind,
      DurationOperationRequirement expectedActivateDuration) {

    sina.changeInsuranceType(insuranceType);

    val creation = bernd.performs(
        TaskCreate.forPatient(sina).ofAssignmentKind(assignmentKind));
    bernd.attemptsTo(
        Verify.that(creation.getResponse().getDuration())
          .doesNotExceed(quantileFor(DurationOperationRequirement.CREATE)));

    val draftTask = creation.getExpectedResponse();
    val prescriptionId = draftTask.getPrescriptionId();

    val kbvBundleBuilder = KbvErpBundleBuilder.faker(sina.getKvnr());
    kbvBundleBuilder
        .prescriptionId(prescriptionId)
        .practitioner(bernd.getPractitioner())
        .custodian(bernd.getMedicalOrganization())
        .patient(sina.getPatientData())
        .insurance(sina.getInsuranceCoverage());
    sina.getAssignerOrganization().ifPresent(kbvBundleBuilder::assigner);
    val kbvBundle = kbvBundleBuilder.build();

    val signedKbvBundle = bernd.performs(SignKbvBundleAction.forGiven(kbvBundle));
    bernd.attemptsTo(
        Verify.that(signedKbvBundle.getDuration())
            .doesNotExceed(quantileFor(DurationOperationRequirement.SIGN_DOCUMENT)));

    val activation =
        bernd.performs(ActivateAction.forGiven(draftTask, signedKbvBundle.getPayload()));
    bernd.attemptsTo(
        Verify.that(activation.getResponse().getDuration())
            .doesNotExceed(quantileFor(expectedActivateDuration)));

    sina.performs(
        TaskAbort.asPatient(activation.getExpectedResponse()));
  }

  private VerificationStep<Duration> quantileFor(DurationOperationRequirement expected) {

    Predicate<Duration> predicate = d -> d.toMillis() <= expected.getQuantile();
    val step =
        new VerificationStep.StepBuilder<Duration>(
            expected.getRequirement(),
            format(
                "Request muss innerhalb von {0} millisekunden beantwortet werden",
                expected.getQuantile()));
    return step.predicate(predicate).accept();
  }

  @RequiredArgsConstructor
  static class Verify implements Performable {

    private final List<VerificationStep<Duration>> steps = new ArrayList<>();

    private final Duration actual;

    public static Verify that(Duration actual) {
      return new Verify(actual);
    }

    @Override
    @Step("{0} verifiziert #step")
    public <T extends net.serenitybdd.screenplay.Actor> void performAs(T t) {
      int stepIdx = 1;
      for (val step : this.steps) {
        log.info(
            format("+- [{0}] {1}: {2}", stepIdx++, step.getRequirement(), step.getExpectation()));
        step.apply(actual);
      }
    }

    public Verify doesNotExceed(VerificationStep<Duration> step) {
      steps.add(step);
      return this;
    }
  }

  @RequiredArgsConstructor
  static class SignKbvBundleAction implements Question<KonnektorResponse<byte[]>> {
    private final KbvErpBundle kbvErpBundle;
    public static SignKbvBundleAction forGiven(KbvErpBundle kbvErpBundle) {
      return new SignKbvBundleAction(kbvErpBundle);
    }
    @Override
    @Step("{0} erstellt QES Signatur von #kbvBundle")
    public KonnektorResponse<byte[]> answeredBy(net.serenitybdd.screenplay.Actor actor) {
      val erpClient = SafeAbility.getAbility(actor, UseTheErpClient.class);
      val konnektor = SafeAbility.getAbility(actor, UseTheKonnektor.class);

      var encodedKbv = erpClient.encode(kbvErpBundle, erpClient.getSendMime().toFhirEncoding());
      return konnektor.signDocumentWithHba(encodedKbv);
    }
  }

  @RequiredArgsConstructor
  static class ActivateAction extends ErpAction<ErxTask> {

    private final String taskId;
    private final AccessCode accessCode;
    private final byte[] signedKbv;

    public static ActivateAction forGiven(ErxTask task, byte[] signedKbv) {
      return new ActivateAction(task.getUnqualifiedId(), task.getAccessCode(), signedKbv);
    }

    @Override
    @Step("{0} aktiviert Task #taskId mit #accessCode")
    public ErpInteraction<ErxTask> answeredBy(net.serenitybdd.screenplay.Actor actor) {
      val cmd = new TaskActivateCommand(taskId, accessCode, signedKbv);
      return this.performCommandAs(cmd, actor);
    }
  }
}
