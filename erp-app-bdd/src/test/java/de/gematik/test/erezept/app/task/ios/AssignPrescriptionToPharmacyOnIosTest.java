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

package de.gematik.test.erezept.app.task.ios;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.exceptions.AppStateMissmatchException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;

class AssignPrescriptionToPharmacyOnIosTest extends ErpFhirParsingTest {

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
    val erpClient = mock(ErpClient.class);
    val erpClientAbility = mock(UseTheErpClient.class);
    when(erpClientAbility.getClient()).thenReturn(erpClient);
    givenThat(theAppUser).can(erpClientAbility);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    givenThat(theAppUser).can(ProvideEGK.sheOwns(sca.getEgk(0)));

    // make sure the teardown does not run into an NPE
    val mockResponse =
        ErpResponse.forPayload(FhirTestResourceUtil.createOperationOutcome(), Resource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
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

    val providePatientBaseData = mock(ProvidePatientBaseData.class);
    when(providePatientBaseData.getFullName())
        .thenReturn("Prof. Dr. med Friedrich-Wilhelm Grossherzog");
    actor.can(providePatientBaseData);

    val getTaskResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(getTaskResponse);
    when(app.getWebElementListLen(any(PrescriptionsViewElement.class))).thenReturn(1);
    when(app.getText(PrescriptionTechnicalInformation.TASKID)).thenReturn(taskId.getValue());
    when(app.getText(BottomNav.MESSAGES_BUTTON)).thenReturn("1 Objekt").thenReturn("2 Objekte");
    // return a patient name that is 101 chars long -> it should get shortened
    when(app.getText(PrescriptionDetails.ADDRESS_NAME_FIELD))
        .thenReturn(
            "Maximilian-Friedrich-Alexander-Johann-Theodore-Benediktus-Konstantin-Emmerich-Zacharias-Schwarzburger");

    val mockedContactAddressNameInputElement = mock(WebElement.class);
    when(mockedContactAddressNameInputElement.getLocation()).thenReturn(new Point(16, 312));
    when(mockedContactAddressNameInputElement.getSize()).thenReturn(new Dimension(361, 59));
    when(app.getWebElement(PrescriptionDetails.CONTACT_ADDRESS_NAME_INPUT))
        .thenReturn(mockedContactAddressNameInputElement);

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

  @Test
  void shouldFailAssigningPrescriptionToPharmacyIfMessageCounterNotIncreased() {
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
    // return "1 Objekt" before and after assigning the prescription -> should throw an exception
    when(app.getText(BottomNav.MESSAGES_BUTTON)).thenReturn("1 Objekt").thenReturn("1 Objekt");
    // return a patient name that is exactly 100 chars long -> it should not get shortened
    when(app.getText(PrescriptionDetails.ADDRESS_NAME_FIELD))
        .thenReturn(
            "Maximilian-Friedrich-Alexander-Johann-Theodor-Benediktus-Konstantin-Emmerich-Zacharias-Schwarzburger");
    val action = AssignPrescriptionToPharmacyOnIos.fromStack("erste").toPharmacy(pharmacy);
    assertThrows(AppStateMissmatchException.class, () -> actor.attemptsTo(action));
  }

  @Test
  void shouldFailAssigningPrescriptionToPharmacyIfMessageNotShown() {
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
    // return the correct TaskId to ensure the prescription is present on the main screen
    // (for MovingToPrescription)
    // after that return a different TaskId for the check of the prescription in the latest message
    // -> should throw an exception
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(taskId.getValue())
        .thenReturn(PrescriptionId.random().getValue());
    when(app.getText(BottomNav.MESSAGES_BUTTON)).thenReturn("1 Objekt").thenReturn("2 Objekte");
    // return a patient name that is exactly 100 chars long -> so it should not get shortened
    when(app.getText(PrescriptionDetails.ADDRESS_NAME_FIELD))
        .thenReturn(
            "Maximilian-Friedrich-Alexander-Johann-Theodor-Benediktus-Konstantin-Emmerich-Zacharias-Schwarzburger");
    val action = AssignPrescriptionToPharmacyOnIos.fromStack("erste").toPharmacy(pharmacy);
    assertThrows(AppStateMissmatchException.class, () -> actor.attemptsTo(action));
  }

  @Test
  void shouldFailAssigningPrescriptionToPharmacyIfMessageDoesntContainPrescription() {
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
    when(app.getText(BottomNav.MESSAGES_BUTTON)).thenReturn("1 Objekt").thenReturn("2 Objekte");
    // return a patient name that is exactly 100 chars long -> so it should not get shortened
    when(app.getText(PrescriptionDetails.ADDRESS_NAME_FIELD))
        .thenReturn(
            "Maximilian-Friedrich-Alexander-Johann-Theodor-Benediktus-Konstantin-Emmerich-Zacharias-Schwarzburger");

    try (MockedStatic<ListPageElement> utilities = Mockito.mockStatic(ListPageElement.class)) {
      utilities
          .when(() -> ListPageElement.forElement(MessageScreen.PRESCRIPTION_LIST, 0))
          .thenThrow(NoSuchElementException.class);

      val action = AssignPrescriptionToPharmacyOnIos.fromStack("erste").toPharmacy(pharmacy);
      assertThrows(AppStateMissmatchException.class, () -> actor.attemptsTo(action));
    }
  }
}
