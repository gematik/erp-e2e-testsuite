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

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import de.gematik.test.core.expectations.requirements.*;
import de.gematik.test.erezept.*;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.rest.*;
import de.gematik.test.erezept.client.usecases.*;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.parser.*;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.*;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.*;
import java.util.*;
import java.util.function.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;
import org.junit.jupiter.api.*;
import org.mockito.stubbing.*;

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
    val smcb = smartcards.getSmcbByICCSN(docConfig.getSmcbIccsn());
    val hba = smartcards.getHbaByICCSN(docConfig.getHbaIccsn());

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
