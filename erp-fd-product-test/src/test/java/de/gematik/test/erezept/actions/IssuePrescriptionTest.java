/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.testutils.ErpResponseUtil;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.fhirfuzz.FhirFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import de.gematik.test.utils.ActorMockUtil;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task.TaskStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class IssuePrescriptionTest extends ErpFhirParsingTest {

  private static PatientActor patientActor;

  @BeforeAll
  static void setup() {
    StopwatchProvider.init();
    CoverageReporter.getInstance().startTestcase("don't care");

    val patientMock = ActorMockUtil.createRandomPatient();
    patientActor = patientMock.getLeft();
  }

  static Stream<Arguments> shouldCreateValidPrescriptionFromRandomKbvBundleBuilder() {
    return Stream.of(
        arguments(
            "Pharmacy Only with random KbvBundle-Builder",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patientActor)
                            .withResponsibleDoctor(doctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withRandomKbvBundle())),
        arguments(
            "Pharmacy Only with random KbvBundle-Builder and a custom signing function",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patientActor)
                            .withResponsibleDoctor(doctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withRandomKbvBundle()
                            .setCustomSigningFunction(
                                encodedKbv -> new byte[] {0x01, 0x02, 0x03}))),
        arguments(
            "Direct Assignment with given random KbvBundle-Builder",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patientActor)
                            .withResponsibleDoctor(doctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                            .withKbvBundleFrom(
                                KbvErpBundleFaker.builder()
                                    .withKvnr(patientActor.getKvnr())
                                    .withAuthorDate(new Date())
                                    .toBuilder()))));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource
  void shouldCreateValidPrescriptionFromRandomKbvBundleBuilder(
      String label, Function<DoctorActor, ErpInteraction<ErxTask>> function) {

    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    log.trace("Execute {}", label);
    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);

    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    // produces valid Kbv Bundle from random Builder
    assertDoesNotThrow(() -> function.apply(doctorActor));
  }

  @Test
  void shouldCreateInvalidPrescriptionWithExtensions() {
    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);
    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    val extensionUrl = "https://test.erp.gematik.de";
    val extension = new Extension(extensionUrl).setValue(new StringType("Testvalue"));

    doctorActor.performs(
        IssuePrescription.forPatient(patientActor)
            .withCoverageExtension(extension)
            .withPatientExtension(extension)
            .withCompositionExtension(extension)
            .withMedicationExtension(extension)
            .withMedicationRequestExtension(extension)
            .withRandomKbvBundle());
  }

  @Test
  void shouldManipulateKbvBundle() {
    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);
    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    doctorActor.performs(
        IssuePrescription.forPatient(patientActor)
            .withResourceManipulator(
                b -> {
                  val ext =
                      b.getCoverage()
                          .getExtensionByUrl(
                              DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl());
                  ext.getValue().castToCoding(ext.getValue()).setCode("42");
                })
            .withRandomKbvBundle());
  }

  @Test
  void shouldCreateValidPrescriptionFromRandomKbvBundleBuilderAndFuzz() {
    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);
    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val sigObserver = new ByteArrayOutputStream();
    val isPr =
        IssuePrescription.forPatient(patientActor)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withSmartFuzzer(new FhirFuzzImpl(new FuzzerContext(FuzzConfig.getRandom())))
            .withRandomKbvBundle()
            .setSignatureObserver(sigObserver);
    assertDoesNotThrow(() -> doctorActor.performs(isPr));
    assertTrue((Base64.getEncoder().encodeToString(sigObserver.toByteArray())).length() > 20);
  }

  @Test
  void shouldCreateValidPrescriptionFromRandomKbvBundleAsBG() {
    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    val patient2 = new PatientActor("Hanna Bäcker");
    patient2.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient2.getName()));
    patient2.changeCoverageInsuranceType(InsuranceTypeDe.BG);
    patient2.setPayorType(PayorType.UK);

    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);
    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val sigObserver = new ByteArrayOutputStream();
    val isPr =
        IssuePrescription.forPatient(patient2)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withSmartFuzzer(new FhirFuzzImpl(new FuzzerContext(FuzzConfig.getRandom())))
            .withResponsibleDoctor(doctorActor)
            .withRandomKbvBundle()
            .setSignatureObserver(sigObserver);
    assertDoesNotThrow(() -> doctorActor.performs(isPr));
    assertTrue((Base64.getEncoder().encodeToString(sigObserver.toByteArray())).length() > 20);
  }

  @Test
  void shouldCreateValidPrescriptionFromRandomKbvBundleATPrescription() {
    val doctorMock = ActorMockUtil.createRandomDoctor();
    val doctorActor = doctorMock.getLeft();
    val doctorClient = doctorMock.getRight();

    val patient2 = new PatientActor("Hanna Bäcker");
    patient2.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient2.getName()));
    patient2.changeCoverageInsuranceType(InsuranceTypeDe.BG);
    patient2.setPayorType(PayorType.UK);

    val draftTask = new ErxTask();
    draftTask.setId("Task/" + TaskId.random().getValue());
    draftTask.addIdentifier(PrescriptionId.random().asIdentifier());
    draftTask.addIdentifier(AccessCode.random().asIdentifier());
    draftTask.setStatus(TaskStatus.DRAFT);
    val createResponse = ErpResponseUtil.createErpResponse(draftTask, ErxTask.class, 201);

    when(doctorClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(doctorClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val sigObserver = new ByteArrayOutputStream();
    val isPr =
        IssuePrescription.forPatient(patient2)
            .asTPrescription(
                KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
                    .withMedication(KbvErpMedicationPZNFaker.asTPrescription())
                    .withKvnr(patient2.getKvnr())
                    .toBuilder())
            .setSignatureObserver(sigObserver);
    assertDoesNotThrow(() -> doctorActor.performs(isPr));
    assertTrue((Base64.getEncoder().encodeToString(sigObserver.toByteArray())).length() > 20);
  }
}
