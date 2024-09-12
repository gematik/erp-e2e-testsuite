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

package de.gematik.test.erezept.performance.task;

import static java.text.MessageFormat.format;

import de.gematik.test.core.ArgumentComposer;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.DurationOperationRequirement;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actions.TaskAbort;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.KonnektorResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Performable;
import net.serenitybdd.screenplay.Question;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Messung E-Rezept Aktivierung")
class TaskActivatePerformanceUseCase extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

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
        .arguments(
            VersicherungsArtDeBasis.PKV,
            PrescriptionAssignmentKind.DIRECT_ASSIGNMENT,
            DurationOperationRequirement.ACTIVATE_FLOWTYPE_209)
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

    sina.changePatientInsuranceType(insuranceType);

    val creation = doctor.performs(TaskCreate.forPatient(sina).ofAssignmentKind(assignmentKind));
    doctor.attemptsTo(
        Verify.that(creation.getResponse().getDuration())
            .doesNotExceed(quantileFor(DurationOperationRequirement.CREATE)));

    val draftTask = creation.getExpectedResponse();
    val prescriptionId = draftTask.getPrescriptionId();

    val kbvBundleFaker = KbvErpBundleFaker.builder().withKvnr(sina.getKvnr());
    kbvBundleFaker
        .withPrescriptionId(prescriptionId)
        .withPractitioner(doctor.getPractitioner())
        .withCustodian(doctor.getMedicalOrganization())
        .withPatient(sina.getPatientData())
        .withInsurance(sina.getInsuranceCoverage(), sina.getPatientData());
    sina.getAssignerOrganization().ifPresent(kbvBundleFaker::withAssignerOrganization);
    val kbvBundle = kbvBundleFaker.fake();

    // when built via withRandomKbvBundle, the practitioner reference does not match and needs to be
    // fixed
    kbvBundle
        .getMedicationRequestOptional()
        .ifPresent(
            mr ->
                mr.getRequester()
                    .setReference(format("Practitioner/{0}", doctor.getPractitioner().getId())));

    val signedKbvBundle = doctor.performs(SignKbvBundleAction.forGiven(kbvBundle));
    doctor.attemptsTo(
        Verify.that(signedKbvBundle.getDuration())
            .doesNotExceed(quantileFor(DurationOperationRequirement.SIGN_DOCUMENT)));

    val activation =
        doctor.performs(ActivateAction.forGiven(draftTask, signedKbvBundle.getPayload()));
    doctor.attemptsTo(
        Verify.that(activation.getResponse().getDuration())
            .doesNotExceed(quantileFor(expectedActivateDuration)));

    sina.performs(TaskAbort.asPatient(activation.getExpectedResponse()));
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

    private final TaskId taskId;
    private final AccessCode accessCode;
    private final byte[] signedKbv;

    public static ActivateAction forGiven(ErxTask task, byte[] signedKbv) {
      return new ActivateAction(task.getTaskId(), task.getAccessCode(), signedKbv);
    }

    @Override
    @Step("{0} aktiviert Task #taskId mit #accessCode")
    public ErpInteraction<ErxTask> answeredBy(net.serenitybdd.screenplay.Actor actor) {
      val cmd = new TaskActivateCommand(taskId, accessCode, signedKbv);
      return this.performCommandAs(cmd, actor);
    }
  }
}
