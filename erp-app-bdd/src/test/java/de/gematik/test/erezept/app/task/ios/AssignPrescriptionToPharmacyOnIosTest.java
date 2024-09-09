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

package de.gematik.test.erezept.app.task.ios;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionTechnicalInformation;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionsViewElement;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.resources.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ProvideApoVzdInformation;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
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

class AssignPrescriptionToPharmacyOnIosTest {

  private final SmartcardArchive sca = SmartcardArchive.fromResources();
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});

    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(app);
    val erpClientAbility = mock(UseTheErpClient.class);
    givenThat(theAppUser).can(erpClientAbility);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ProvideEGK.sheOwns(sca.getEgk(0)));

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
  void shouldAssignPrescriptionToPharmacy() {
    val pharmacy = OnStage.theActorCalled("Pharmacy");
    val apoVzdName = "Pharmacy-TEST-ONLY";
    pharmacy.can(ProvideApoVzdInformation.withName(apoVzdName));
    pharmacy.can(ManageCommunications.heExchanges());
    pharmacy.can(UseSMCB.itHasAccessTo(sca.getSmcB(0)));

    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);

    // mock the erp-client
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val kbvBundle = KbvErpBundleFaker.builder().withPrescriptionId(prescriptionId).fake();

    val task = mock(ErxTask.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());

    val action = AssignPrescriptionToPharmacyOnIos.fromStack("erste").toPharmacy(pharmacy);
    assertDoesNotThrow(() -> actor.attemptsTo(action));

    val pharmacyCommunications = SafeAbility.getAbility(pharmacy, ManageCommunications.class);
    assertFalse(pharmacyCommunications.getExpectedCommunications().isEmpty());
  }

  @Test
  void shouldFailAssigningPrescriptionToPharmacyIfPrescriptionIsMissing() {
    val pharmacy = OnStage.theActorCalled("Pharmacy");
    val apoVzdName = "Pharmacy-TEST-ONLY";
    pharmacy.can(ProvideApoVzdInformation.withName(apoVzdName));
    pharmacy.can(ManageCommunications.heExchanges());
    pharmacy.can(UseSMCB.itHasAccessTo(sca.getSmcB(0)));

    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val erpClient = actor.abilityTo(UseTheErpClient.class);

    // mock the erp-client
    val dmcList = actor.abilityTo(ManageDataMatrixCodes.class);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val accessCode = AccessCode.random();
    dmcList.appendDmc(DmcPrescription.ownerDmc(taskId, accessCode));

    val oO = FhirTestResourceUtil.createOperationOutcome();
    val getTaskResponse =
        ErpResponse.forPayload(oO, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(404)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue());

    val action = AssignPrescriptionToPharmacyOnIos.fromStack("erste").toPharmacy(pharmacy);
    assertThrows(MissingPreconditionError.class, () -> actor.attemptsTo(action));

    val pharmacyCommunications = SafeAbility.getAbility(pharmacy, ManageCommunications.class);
    assertTrue(pharmacyCommunications.getExpectedCommunications().isEmpty());
  }
}
