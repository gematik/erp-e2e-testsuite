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
 */

package de.gematik.test.erezept.integration.fuzzing;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeBetween;
import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIsBetween;

import ca.uhn.fhir.parser.LenientErrorHandler;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.fhir.fuzzing.FuzzingEngine;
import de.gematik.bbriccs.fhir.fuzzing.impl.FuzzingEngineImpl;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.core.expectations.requirements.FhirRequirements;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.abilities.UseHapiFuzzer;
import de.gematik.test.erezept.actions.AcceptPrescription;
import de.gematik.test.erezept.actions.ActivatePrescription;
import de.gematik.test.erezept.actions.ClosePrescription;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.TaskCreate;
import de.gematik.test.erezept.actions.TheTask;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actions.communication.GetMessage;
import de.gematik.test.erezept.actions.communication.SendMessages;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.usecases.CommunicationGetByIdCommand;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxCommunicationBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.extensions.erp.SupplyOptionsType;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.values.json.CommunicationReplyMessage;
import de.gematik.test.erezept.fhir.valuesets.AvailabilityStatus;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.erezept.toggle.FuzzingIterationsToggle;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.AfterEach;
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
@DisplayName("Fuzzing-Szenarien für den E-Rezept Workflow (Bricks!)")
@Tag("Fuzzing")
public class FuzzingIT extends ErpTest {

  private static final Integer iterations = featureConf.getToggle(new FuzzingIterationsToggle());

  private final List<ErxTask> tasks = new LinkedList<>();

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @Actor(name = "Am Flughafen")
  private PharmacyActor pharmacy;

  public static Stream<Arguments> fuzzingStrength() {
    return IntStream.rangeClosed(1, iterations)
        //        .mapToObj(x -> Arguments.of((double) 1 - Math.exp(-Math.pow(x, 2) / iterations)));
        .mapToObj(x -> Arguments.of(Math.sin((double) x / iterations)));
  }

  private void equipWithFuzzer(ErpActor actor, FuzzingEngine fuzzingEngine) {
    actor.can(UseHapiFuzzer.withFuzzingEngine(fuzzingEngine));

    // this is required to be able to encode invalid FHIR resources
    val fhir = actor.abilityTo(UseTheErpClient.class).getFhir();
    val errorHandler = new LenientErrorHandler();
    errorHandler.disableAllErrors();
    fhir.getCtx().setParserErrorHandler(errorHandler);
  }

  @AfterEach
  void cleanup() {
    // post condition: cleanup created prescription to avoid side effects from
    // possible invalid resources
    val doctorClient = doctor.abilityTo(UseTheErpClient.class);
    tasks.forEach(
        task -> doctorClient.request(new TaskAbortCommand(task.getTaskId(), task.getAccessCode())));
  }

  @TestcaseId("ERP_FUZZING_01")
  @DisplayName("Randomisiertes SmartFuzzing für den E-Rezept Workflow")
  @ParameterizedTest(name = "[{index}] -> SmartFuzzing mit ''{0}''")
  @MethodSource("fuzzingStrength")
  void runCompleteWorkflowWithBricksFuzzer(double fuzzingStrength) {
    val insuranceType = GemFaker.randomElement(InsuranceTypeDe.PKV, InsuranceTypeDe.GKV);
    val assignmentKind =
        GemFaker.randomElement(
            PrescriptionAssignmentKind.PHARMACY_ONLY, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);
    patient.changePatientInsuranceType(insuranceType);

    val fuzzer = FuzzingEngineImpl.builder(fuzzingStrength).withDefaultFuzzers().build();
    List.of(doctor, patient, pharmacy).forEach(actor -> equipWithFuzzer(actor, fuzzer));

    val creation = doctor.asksFor(TaskCreate.forPatient(patient).ofAssignmentKind(assignmentKind));
    doctor.attemptsTo(
        Verify.that(creation)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    val taskId =
        creation
            .getResponse()
            .getResourceOptional()
            .map(
                erxTask -> {
                  tasks.add(erxTask);
                  return erxTask.getTaskId();
                })
            .orElseGet(() -> TaskId.from(PrescriptionId.random()));

    val accessCode =
        creation
            .getResponse()
            .getResourceOptional()
            .map(ErxTask::getAccessCode)
            .orElseGet(AccessCode::random);

    val activation =
        doctor.asksFor(
            ActivatePrescription.withId(taskId)
                .andAccessCode(accessCode)
                .withKbvBundle(KbvErpBundleFaker.builder().withKvnr(patient.getKvnr()).fake()));
    doctor.attemptsTo(
        Verify.that(activation)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    val getTaskBundle = patient.asksFor(TheTask.withId(taskId));
    patient.attemptsTo(
        Verify.that(getTaskBundle)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    val acceptAPrescription =
        getTaskBundle
            .getResponse()
            .getResourceOptional()
            .map(
                taskBundle ->
                    AcceptPrescription.with(
                        taskId,
                        taskBundle.getTask().getOptionalAccessCode().orElseGet(AccessCode::random)))
            .orElseGet(() -> AcceptPrescription.with(taskId, AccessCode.random()));
    val acceptResponse = pharmacy.performs(acceptAPrescription);
    pharmacy.attemptsTo(
        Verify.that(acceptResponse)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    if (acceptResponse.isOfExpectedType()) {
      // TODO: find a way to randomly fake missing data if previous step failed
      val closeInteraction = pharmacy.performs(ClosePrescription.acceptedWith(acceptResponse));
      pharmacy.attemptsTo(
          Verify.that(closeInteraction)
              .withIndefiniteType()
              .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
              .isCorrect());
    }
  }

  @TestcaseId("ERP_FUZZING_02")
  @DisplayName("Randomisiertes SmartFuzzing Communications zwischen Versichertem und Apotheke")
  @ParameterizedTest(name = "[{index}] -> SmartFuzzing mit ''{0}''")
  @MethodSource("fuzzingStrength")
  void runCommunicationsWithBricksFuzzer(double fuzzingStrength) {
    val insuranceType = GemFaker.randomElement(InsuranceTypeDe.PKV, InsuranceTypeDe.GKV);
    val assignmentKind =
        GemFaker.randomElement(
            PrescriptionAssignmentKind.PHARMACY_ONLY, PrescriptionAssignmentKind.DIRECT_ASSIGNMENT);
    patient.changePatientInsuranceType(insuranceType);

    // precondition: create a prescription without fuzzing
    val activation =
        doctor.performs(
            IssuePrescription.forPatient(patient)
                .ofAssignmentKind(assignmentKind)
                .withRandomKbvBundle());
    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpAfos.A_19022)
            .hasResponseWith(returnCode(200))
            .isCorrect());
    val task = activation.getExpectedResponse();
    tasks.add(task);

    val getTaskBundle = patient.asksFor(TheTask.fromBackend(task));
    patient.attemptsTo(Verify.that(getTaskBundle).withExpectedType(ErpAfos.A_19022).isCorrect());

    val medication =
        getTaskBundle
            .getExpectedResponse()
            .getKbvBundle()
            .map(KbvErpBundle::getMedication)
            .orElseGet(() -> KbvErpMedicationPZNFaker.builder().fake());

    // now activate fuzzing
    val fuzzer = FuzzingEngineImpl.builder(fuzzingStrength).withDefaultFuzzers().build();
    List.of(patient, pharmacy).forEach(actor -> equipWithFuzzer(actor, fuzzer));

    // fuzzing communication messages
    val infoReqCom =
        ErxCommunicationBuilder.forInfoRequest("Hallo, das ist meine &lt;Request&gt; <Nachricht>!")
            .basedOn(task.getTaskId())
            .status("unknown")
            .medication(medication)
            .insurance(IKNR.asArgeIknr("104212059"))
            .receiver(pharmacy.getTelematikId().getValue())
            .substitution(false)
            .flowType(
                PrescriptionFlowType.fromPrescriptionId(PrescriptionId.from(task.getTaskId())))
            .build();

    val infoReqResponse = patient.performs(SendMessages.withCommunication(infoReqCom));
    patient.attemptsTo(
        Verify.that(infoReqResponse)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    // force the FD to transcode the message to XML if it was accepted
    infoReqResponse
        .getResponse()
        .getResourceOptional()
        .ifPresent(
            communication -> {
              val getInfoRequest =
                  pharmacy.performs(
                      GetMessage.byId(new CommunicationGetByIdCommand(communication.getIdPart())));

              // assume when the FD allowed sending the message, it should be capable of the
              // returning it in xml
              pharmacy.attemptsTo(
                  Verify.that(getInfoRequest)
                      .withExpectedType()
                      .hasResponseWith(returnCodeBetween(200, 210))
                      .isCorrect());
            });

    val replyCom =
        ErxCommunicationBuilder.asReply(new CommunicationReplyMessage())
            .basedOn(task.getTaskId())
            .receiver(patient.getKvnr().getValue())
            .sender(pharmacy.getTelematikId().getValue())
            .availabilityStatus(AvailabilityStatus.AS_30)
            .supplyOptions(SupplyOptionsType.SHIPMENT)
            .build();
    val replyResponse = pharmacy.performs(SendMessages.withCommunication(replyCom));
    pharmacy.attemptsTo(
        Verify.that(replyResponse)
            .withIndefiniteType()
            .hasResponseWith(returnCodeIsBetween(200, 499, FhirRequirements.FHIR_XML_PARSING))
            .isCorrect());

    replyResponse
        .getResponse()
        .getResourceOptional()
        .ifPresent(
            communication -> {
              val getReplyRequest =
                  patient.performs(
                      GetMessage.byId(new CommunicationGetByIdCommand(communication.getIdPart())));

              // assume when the FD allowed sending the message, it should be capable of the
              // returning it in json
              patient.attemptsTo(
                  Verify.that(getReplyRequest)
                      .withExpectedType()
                      .hasResponseWith(returnCodeBetween(200, 210))
                      .isCorrect());
            });
  }
}
