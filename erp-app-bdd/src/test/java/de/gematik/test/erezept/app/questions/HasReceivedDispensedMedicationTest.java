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

package de.gematik.test.erezept.app.questions;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.MedicationDispenseDetails;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionsViewElement;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.MedicationDispenseSearchByIdCommand;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.erp.ErxMedicationDispenseFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class HasReceivedDispensedMedicationTest {

  private String userName;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val appAbility = mock(UseIOSApp.class);
    when(appAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(appAbility);
    val erpClientAbility = mock(UseTheErpClient.class);
    givenThat(theAppUser).can(erpClientAbility);
    givenThat(theAppUser).can(ReceiveDispensedDrugs.forHimself());

    // make sure the teardown does not run into an NPE
    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClientAbility.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldFailOnDifferentAmountOfDispensedMedications() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dispensedMedications = actor.abilityTo(ReceiveDispensedDrugs.class);

    val prescriptionId = PrescriptionId.random();
    dispensedMedications.append(prescriptionId, Instant.now());

    val kvnr = KVNR.random();
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationDispenses = mock(ErxMedicationDispenseBundle.class);
    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);
    when(medicationDispenses.getMedicationDispenses())
        .thenReturn(
            List.of(
                ErxMedicationDispenseFaker.builder()
                    .withKvnr(kvnr)
                    .withPerfomer("123")
                    .withPrescriptionId(prescriptionId)
                    .fake(),
                ErxMedicationDispenseFaker.builder()
                    .withKvnr(kvnr)
                    .withPerfomer("123")
                    .withPrescriptionId(prescriptionId)
                    .fake()));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val medicationDispenseResponse =
        ErpResponse.forPayload(medicationDispenses, ErxMedicationDispenseBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(prescriptionId.getValue());
    when(app.getWebElementListLen(MedicationDispenseDetails.DISPENSED)).thenReturn(1);
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(erpClient.request(any(MedicationDispenseSearchByIdCommand.class)))
        .thenReturn(medicationDispenseResponse);

    val hasReceived = HasReceivedDispensedMedication.fromStack("erste");
    assertFalse(actor.asksFor(hasReceived));
  }

  @Test
  void shouldPassOnSameAmountOfDispensedMedications() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dispensedMedications = actor.abilityTo(ReceiveDispensedDrugs.class);

    val prescriptionId = PrescriptionId.random();
    dispensedMedications.append(prescriptionId, Instant.now());

    val kvnr = KVNR.random();
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationDispenses = mock(ErxMedicationDispenseBundle.class);
    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);
    when(medicationDispenses.getMedicationDispenses())
        .thenReturn(
            List.of(
                ErxMedicationDispenseFaker.builder()
                    .withKvnr(kvnr)
                    .withPerfomer("123")
                    .withPrescriptionId(prescriptionId)
                    .fake(),
                ErxMedicationDispenseFaker.builder()
                    .withKvnr(kvnr)
                    .withPerfomer("123")
                    .withPrescriptionId(prescriptionId)
                    .fake()));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val medicationDispenseResponse =
        ErpResponse.forPayload(medicationDispenses, ErxMedicationDispenseBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(prescriptionId.getValue());
    when(app.getWebElementListLen(MedicationDispenseDetails.DISPENSED)).thenReturn(2);
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(erpClient.request(any(MedicationDispenseSearchByIdCommand.class)))
        .thenReturn(medicationDispenseResponse);

    val hasReceived = HasReceivedDispensedMedication.fromStack("erste");

    assertTrue(actor.asksFor(hasReceived));
  }

  @Test
  void shouldThrowOnMissingArchivedPrescription() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dispensedMedications = actor.abilityTo(ReceiveDispensedDrugs.class);
    dispensedMedications.append(PrescriptionId.random(), Instant.now());
    val getTaskResponse =
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue());
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);

    val hasReceived = HasReceivedDispensedMedication.fromStack("erste");

    assertThrows(MissingPreconditionError.class, () -> actor.asksFor(hasReceived));
  }

  @Test
  void shouldThrowOnMissingMedicationDispense() {
    val actor = OnStage.theActorCalled(userName);
    val erpClient = actor.abilityTo(UseTheErpClient.class);
    val app = actor.abilityTo(UseIOSApp.class);
    val dispensedMedications = actor.abilityTo(ReceiveDispensedDrugs.class);

    val prescriptionId = PrescriptionId.random();
    dispensedMedications.append(prescriptionId, Instant.now());

    val kbvBundle = mock(KbvErpBundle.class);

    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    val medicationDispenseResponse =
        ErpResponse.forPayload(
                FhirTestResourceUtil.createOperationOutcome(), ErxMedicationDispenseBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());

    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(prescriptionId.getValue());
    when(app.getWebElementListLen(MedicationDispenseDetails.DISPENSED)).thenReturn(2);
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(erpClient.request(any(MedicationDispenseSearchByIdCommand.class)))
        .thenReturn(medicationDispenseResponse);

    val hasReceived = HasReceivedDispensedMedication.fromStack("erste");

    assertThrows(MissingPreconditionError.class, () -> actor.asksFor(hasReceived));
  }
}
