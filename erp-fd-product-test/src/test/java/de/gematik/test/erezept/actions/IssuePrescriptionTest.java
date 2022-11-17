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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.ErpConfiguration;
import de.gematik.test.erezept.ErpInteraction;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.rest.MediaType;
import de.gematik.test.erezept.client.usecases.TaskActivateCommand;
import de.gematik.test.erezept.client.usecases.TaskCreateCommand;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.StringType;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

class IssuePrescriptionTest {

  private static FhirParser fhir;
  private static DoctorActor doctor;
  private static PatientActor patient;
  private static UseTheErpClient useErpClient;

  @BeforeAll
  static void setup() {
    CoverageReporter.getInstance().startTestcase("don't care");
    fhir = new FhirParser();

    // init doctor
    doctor = new DoctorActor("Bernd Claudius");

    val config = ErpConfiguration.create();
    val docConfig = config.getDoctorConfig(doctor.getName());
    docConfig.setKonnektor("Soft-Konn");
    useErpClient = mock(UseTheErpClient.class);
    val smartcards = config.getSmartcards();
    val smcb = smartcards.getSmcbByICCSN(docConfig.getSmcbIccsn(), docConfig.getCryptoAlgorithm());
    val hba = smartcards.getHbaByICCSN(docConfig.getHbaIccsn(), docConfig.getCryptoAlgorithm());

    val provideBaseData = ProvideDoctorBaseData.fromConfiguration(docConfig);
    val useKonnektor =
        UseTheKonnektor.with(smcb).and(hba).on(config.instantiateDoctorKonnektor(docConfig));
    doctor.can(useErpClient);
    doctor.can(provideBaseData);
    doctor.can(useKonnektor);

    // init patient
    patient = new PatientActor("Sina Hüllmann");
    patient.can(ProvidePatientBaseData.forGkvPatient("X123456789", patient.getName()));
  }

  @Test
  void shouldCreateValidPrescriptionFromRandomKbvBundleBuilder() {
    List.of(
            (Supplier<ErpInteraction<ErxTask>>)
                () ->
                    doctor.performs(
                        IssuePrescription.forPatient(patient)
                            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY)
                            .withRandomKbvBundle()),
            () ->
                doctor.performs(
                    IssuePrescription.forPatient(patient)
                        .ofAssignmentKind(PrescriptionAssignmentKind.DIRECT_ASSIGNMENT)
                        .withKbvBundleFrom(
                            KbvErpBundleBuilder.faker(patient.getKvnr(), new Date()))))
        .forEach(
            supplier -> {
              val draftTask = mock(ErxTask.class);
              when(draftTask.getUnqualifiedId())
                  .thenReturn(
                      PrescriptionId.random().getValue()); // don't care about concrete value
              when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
              when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
              when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
              val createResponse = new ErpResponse(201, Map.of(), draftTask);

              when(useErpClient.getSendMime()).thenReturn(MediaType.FHIR_XML);
              when(useErpClient.request(any(TaskCreateCommand.class))).thenReturn(createResponse);
              when(useErpClient.request(any(TaskActivateCommand.class))).thenReturn(createResponse);

              doAnswer(
                      (Answer<String>)
                          invocation -> {
                            final Object[] args = invocation.getArguments();
                            val encoded =
                                fhir.encode((Resource) args[0], (EncodingType) args[1], true);

                            // make sure the resource provided is valid
                            val result = fhir.validate(encoded);

                            if (!result.isSuccessful()) {
                              System.out.println(encoded);
                              result.getMessages().forEach(System.out::println);
                            }

                            assertTrue(result.isSuccessful());

                            return encoded;
                          })
                  .when(useErpClient)
                  .encode(any(), any());

              // produces valid Kbv Bundle from random Builder
              assertDoesNotThrow(supplier::get);
            });
  }

  @Test
  void shouldCreateInvalidPrescriptionWithExtensions() {
    val draftTask = mock(ErxTask.class);
    when(draftTask.getUnqualifiedId())
        .thenReturn(PrescriptionId.random().getValue()); // don't care about concrete value
    when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
    when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
    val createResponse = new ErpResponse(201, Map.of(), draftTask);

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

                  return fhir.encode(kbvBundle, (EncodingType) args[1], true);
                })
        .when(useErpClient)
        .encode(any(), any());

    doctor.performs(
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
    when(draftTask.getUnqualifiedId())
        .thenReturn(PrescriptionId.random().getValue()); // don't care about concrete value
    when(draftTask.getPrescriptionId()).thenReturn(PrescriptionId.random());
    when(draftTask.getAccessCode()).thenReturn(AccessCode.random());
    when(draftTask.getStatus()).thenReturn(Task.TaskStatus.DRAFT);
    val createResponse = new ErpResponse(201, Map.of(), draftTask);

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
                              DeBasisStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl());
                  val codingValue = dmpExtension.getValue().castToCoding(dmpExtension.getValue());
                  assertEquals("42", codingValue.getCode());
                  assertEquals(
                      DmpKennzeichen.CODE_SYSTEM.getCanonicalUrl(), codingValue.getSystem());

                  return fhir.encode(kbvBundle, (EncodingType) args[1], true);
                })
        .when(useErpClient)
        .encode(any(), any());

    doctor.performs(
        IssuePrescription.forPatient(patient)
            .withResourceManipulator(
                b -> {
                  val ext =
                      b.getCoverage()
                          .getExtensionByUrl(
                              DeBasisStructDef.GKV_DMP_KENNZEICHEN.getCanonicalUrl());
                  ext.getValue().castToCoding(ext.getValue()).setCode("42");
                })
            .withRandomKbvBundle());
  }
}
