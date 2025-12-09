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

package de.gematik.test.erezept.actions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.EncodingType;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpFdTestsuiteFactory;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import de.gematik.test.fuzzing.fhirfuzz.FhirFuzzImpl;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzConfig;
import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.stubbing.Answer;

@Slf4j
class IssuePrescriptionTest extends ErpFhirParsingTest {

  private static DoctorActor prescribingDoctor;
  private static DoctorActor responsibleDoctor;
  private static PatientActor patient;
  private static UseTheErpClient useErpClient;

  @BeforeAll
  static void setup() {
    StopwatchProvider.init();
    CoverageReporter.getInstance().startTestcase("don't care");

    val config = ErpFdTestsuiteFactory.create();
    // init doctor
    prescribingDoctor = new DoctorActor("Adelheid Ulmenwald");
    val docConfig = config.getDoctorConfig(prescribingDoctor.getName());
    docConfig.setKonnektor("Soft-Konn");
    useErpClient = mock(UseTheErpClient.class);
    val smcb = config.getSmcbByICCSN(docConfig.getSmcbIccsn());
    val hba = config.getHbaByICCSN(docConfig.getHbaIccsn());

    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig, "UselessTestId");

    val useKonnektor =
        UseTheKonnektor.with(smcb)
            .and(hba)
            .and(CryptoSystem.DEFAULT_CRYPTO_SYSTEM)
            .on(config.instantiateKonnektorClient(docConfig));

    prescribingDoctor.can(useErpClient);
    prescribingDoctor.can(provideBaseData);
    prescribingDoctor.can(useKonnektor);
    prescribingDoctor.changeQualificationType(QualificationType.DOCTOR_IN_TRAINING);

    responsibleDoctor = new DoctorActor("Gündüla Gunther");
    responsibleDoctor.can(
        ProvideDoctorBaseData.fromConfiguration(
            config.getDoctorConfig(responsibleDoctor.getName()), "UselessTestId"));

    // init patient
    patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient.getName()));
  }

  static Stream<Arguments> shouldCreateValidPrescriptionFromRandomKbvBundleBuilder() {
    return Stream.of(
        arguments(
            "Pharmacy Only with random KbvBundle-Builder",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patient)
                            .withResponsibleDoctor(responsibleDoctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withRandomKbvBundle())),
        arguments(
            "Pharmacy Only with random KbvBundle-Builder and a custom signing function",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patient)
                            .withResponsibleDoctor(responsibleDoctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withRandomKbvBundle()
                            .setCustomSigningFunction(
                                encodedKbv -> new byte[] {0x01, 0x02, 0x03}))),
        arguments(
            "Direct Assignment with given random KbvBundle-Builder",
            (Function<DoctorActor, ErpInteraction<ErxTask>>)
                doctor ->
                    doctor.performs(
                        IssuePrescription.forPatient(patient)
                            .withResponsibleDoctor(responsibleDoctor)
                            .ofAssignmentKind(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                            .withKbvBundleFrom(
                                KbvErpBundleFaker.builder()
                                    .withKvnr(patient.getKvnr())
                                    .withAuthorDate(new Date())
                                    .toBuilder()))));
  }

  @ParameterizedTest(name = "{index}: {0}")
  @MethodSource
  void shouldCreateValidPrescriptionFromRandomKbvBundleBuilder(
      String label, Function<DoctorActor, ErpInteraction<ErxTask>> function) {

    log.trace("Execute {}", label);
    val draftTask = mock(ErxTask.class);
    when(draftTask.getTaskId())
        .thenReturn(TaskId.from(PrescriptionId.random())); // don't care about concrete value
    when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
    when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
    val createResponse = createErpResponse(draftTask);

    when(useErpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(useErpClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    doAnswer(
            (Answer<String>)
                invocation -> {
                  val args = invocation.getArguments();
                  val encoded = parser.encode((Resource) args[0], (EncodingType) args[1], false);

                  // make sure the resource provided is valid
                  val result = parser.validate(encoded);

                  if (!result.isSuccessful()) {
                    System.out.println(encoded);
                    ValidatorUtil.printValidationResult(result);
                  }

                  assertTrue(result.isSuccessful());
                  return encoded;
                })
        .when(useErpClient)
        .encode(any(), any());

    // produces valid Kbv Bundle from random Builder
    assertDoesNotThrow(() -> function.apply(prescribingDoctor));
  }

  @Test
  void shouldCreateInvalidPrescriptionWithExtensions() {
    val draftTask = mock(ErxTask.class);
    when(draftTask.getTaskId())
        .thenReturn(TaskId.from(PrescriptionId.random())); // don't care about concrete value
    when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
    when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
    val createResponse = createErpResponse(draftTask);

    when(useErpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(useErpClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    val extensionUrl = "https://test.erp.gematik.de";
    val extension = new Extension(extensionUrl).setValue(new StringType("Testvalue"));

    doAnswer(
            (Answer<String>)
                invocation -> {
                  final Object[] args = invocation.getArguments();
                  val kbvBundle = (KbvErpBundle) args[0];

                  // make sure the extensions were set correctly
                  List.of(
                          kbvBundle.getCoverage(),
                          kbvBundle.getPatient(),
                          kbvBundle.getComposition(),
                          kbvBundle.getMedication(),
                          kbvBundle.getMedicationRequest())
                      .forEach(
                          resource ->
                              assertEquals(1, resource.getExtensionsByUrl(extensionUrl).size()));

                  return parser.encode(kbvBundle, (EncodingType) args[1], true);
                })
        .when(useErpClient)
        .encode(any(), any());

    prescribingDoctor.performs(
        IssuePrescription.forPatient(patient)
            .withCoverageExtension(extension)
            .withPatientExtension(extension)
            .withCompositionExtension(extension)
            .withMedicationExtension(extension)
            .withMedicationRequestExtension(extension)
            .withRandomKbvBundle());
  }

  @Test
  void shouldManipulateKbvBundle() {
    val draftTask = mock(ErxTask.class);
    when(draftTask.getTaskId())
        .thenReturn(TaskId.from(PrescriptionId.random())); // don't care about concrete value
    when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
    when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
    val createResponse = createErpResponse(draftTask);

    when(useErpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
    when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(useErpClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

    doAnswer(
            (Answer<String>)
                invocation -> {
                  final Object[] args = invocation.getArguments();
                  val kbvBundle = (KbvErpBundle) args[0];

                  val dmpExtension =
                      kbvBundle
                          .getCoverage()
                          .getExtensionByUrl(
                              DeBasisProfilStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl());
                  val codingValue = dmpExtension.getValue().castToCoding(dmpExtension.getValue());
                  assertEquals("42", codingValue.getCode());
                  assertEquals(
                      DmpKennzeichen.CODE_SYSTEM.getCanonicalUrl(), codingValue.getSystem());

                  return parser.encode(kbvBundle, (EncodingType) args[1], true);
                })
        .when(useErpClient)
        .encode(any(), any());

    prescribingDoctor.performs(
        IssuePrescription.forPatient(patient)
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
    val mockUtil = new MockActorsUtils();

    val draftTask = spy(new ErxTask());
    doReturn(TaskId.from(PrescriptionId.random())).when(draftTask).getTaskId();
    doReturn(PrescriptionId.random()).when(draftTask).getPrescriptionId();
    doReturn(AccessCode.random()).when(draftTask).getAccessCode();
    doReturn(Task.TaskStatus.DRAFT).when(draftTask).getStatus();
    val createResponse = mockUtil.createErpResponse(draftTask, ErxTask.class, 201);
    val doc = mockUtil.actorStage.getDoctorNamed("Adelheid Ulmenwald");
    when(mockUtil.erpClientMock.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(mockUtil.erpClientMock.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val sigObserver = new ByteArrayOutputStream();
    val isPr =
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withSmartFuzzer(new FhirFuzzImpl(new FuzzerContext(FuzzConfig.getRandom())))
            .withRandomKbvBundle()
            .setSignatureObserver(sigObserver);
    assertDoesNotThrow(() -> doc.performs(isPr));
    assertTrue((Base64.getEncoder().encodeToString(sigObserver.toByteArray())).length() > 20);
  }

  @Test
  void shouldCreateValidPrescriptionFromRandomKbvBundleAsBG() {
    val mockUtil = new MockActorsUtils();

    val patient2 = new PatientActor("Hanna Bäcker");
    patient2.can(ProvidePatientBaseData.forGkvPatient(KVNR.random(), patient2.getName()));
    patient2.changeCoverageInsuranceType(InsuranceTypeDe.BG);
    patient2.setPayorType(PayorType.UK);

    val draftTask = spy(new ErxTask());
    doReturn(TaskId.from(PrescriptionId.random())).when(draftTask).getTaskId();
    doReturn(PrescriptionId.random()).when(draftTask).getPrescriptionId();
    doReturn(AccessCode.random()).when(draftTask).getAccessCode();
    doReturn(Task.TaskStatus.DRAFT).when(draftTask).getStatus();
    val createResponse = mockUtil.createErpResponse(draftTask, ErxTask.class, 201);
    val doc = mockUtil.actorStage.getDoctorNamed("Adelheid Ulmenwald");
    when(mockUtil.erpClientMock.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
    when(mockUtil.erpClientMock.request(any(TaskActivateCommand.class))).thenReturn(createResponse);
    val sigObserver = new ByteArrayOutputStream();
    val isPr =
        IssuePrescription.forPatient(patient2)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
            .withSmartFuzzer(new FhirFuzzImpl(new FuzzerContext(FuzzConfig.getRandom())))
            .withResponsibleDoctor(doc)
            .withRandomKbvBundle()
            .setSignatureObserver(sigObserver);
    assertDoesNotThrow(() -> doc.performs(isPr));
    assertTrue((Base64.getEncoder().encodeToString(sigObserver.toByteArray())).length() > 20);
  }

  private ErpResponse<ErxTask> createErpResponse(ErxTask draftTask) {
    return ErpResponse.forPayload(draftTask, ErxTask.class)
        .withStatusCode(201)
        .withHeaders(Map.of())
        .andValidationResult(createEmptyValidationResult());
  }
}
