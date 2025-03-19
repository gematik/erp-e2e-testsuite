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

package de.gematik.test.erezept.app.task;

import static de.gematik.test.erezept.app.mobile.elements.Receipt.*;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.mocker.WebElementMockFactory;
import de.gematik.test.erezept.app.questions.ListRedeemedPrescriptions;
import de.gematik.test.erezept.app.questions.TheLastPrescriptionInTheMainScreen;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskGetByIdCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.WebElement;

class PrescriptionTests {

  private UseTheErpClient erpClient;
  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    erpClient = mock(UseTheErpClient.class);

    userName = GemFaker.fakerName();
    val aliceIos = OnStage.theActorCalled((userName));
    givenThat(aliceIos).can(app);
    givenThat(aliceIos).can(erpClient);
    givenThat(aliceIos).can(ManageDataMatrixCodes.sheGetsPrescribed());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @ParameterizedTest
  @EnumSource(
      value = PrescriptionFlowType.class,
      names = {"FLOW_TYPE_160", "FLOW_TYPE_200"})
  void checkIfTheDeletionFlowIsCorrect(PrescriptionFlowType flowType) {
    val deletePrescriptionTask = DeleteRedeemablePrescription.fromStack("letzte");
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    val taskId = TaskId.from(PrescriptionId.random(flowType));
    val dmcAbility = actor.abilityTo(ManageDataMatrixCodes.class);
    val dmc = DmcPrescription.ownerDmc(taskId, AccessCode.random());
    dmcAbility.appendDmc(dmc);

    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val task = mock(ErxTask.class);
    val medication = mock(KbvErpMedication.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);
    when(task.getStatus()).thenReturn(Task.TaskStatus.READY);
    when(kbvBundle.getMedication()).thenReturn(medication);
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(kbvBundle.getMedication().getMedicationName()).thenReturn("Schmerzmittel");
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);
    when(prescriptionBundle.getKbvBundle()).thenReturn(Optional.of(kbvBundle));
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(medication.getMedicationName()).thenReturn("Schmerzmittel");
    when(medicationRequest.isMultiple()).thenReturn(false);
    when(kbvBundle.getMedicationRequest().isMultiple()).thenReturn(false);
    val taskGetResponse =
        ErpResponse.forPayload(prescriptionBundle, ErxPrescriptionBundle.class)
            .withHeaders(Map.of())
            .withStatusCode(200)
            .andValidationResult(FhirTestResourceUtil.createEmptyValidationResult());
    when(erpClient.request(any(TaskGetByIdCommand.class))).thenReturn(taskGetResponse);

    val firstPrescription = WebElementMockFactory.createRedeemablePrescription();
    val secondPrescription = WebElementMockFactory.createRedeemablePrescription();

    when(app.getWebElementListLen(any())).thenReturn(2);
    when(app.getWebElements(Mainscreen.PRESCRIPTION_LIST_ELEMENT_STATUS))
        .thenReturn(List.of(firstPrescription, secondPrescription));
    when(app.getText(PrescriptionTechnicalInformation.TASKID))
        .thenReturn(PrescriptionId.random().getValue())
        .thenReturn(taskId.getValue());

    assertDoesNotThrow(() -> deletePrescriptionTask.performAs(actor));

    // check if the dmc was moved on the stack
    assertTrue(dmcAbility.getDmcs().isEmpty());
    assertFalse(dmcAbility.getDeletedDmcs().isEmpty());
    assertEquals(dmc, dmcAbility.getDeletedDmcs().getFirst());
  }

  @Test
  void shouldDeleteBatchArchivedPrescription() {
    val task = DeleteBatchArchivedPrescription.insideTheApp();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    List<WebElement> mockElements = new ArrayList<>();
    WebElement element1 = mock(WebElement.class);
    WebElement element2 = mock(WebElement.class);
    mockElements.add(element1);
    mockElements.add(element2);

    when(app.getWebElements(REDEEMED_PRESCRIPTION_STATUS_LABEL)).thenReturn(mockElements);
    when(app.isPresent(Receipt.ARCHIVED_PRESCRIPTIONS_BTN)).thenReturn(true);

    assertDoesNotThrow(() -> actor.attemptsTo(task));
    verify(app, times(1)).tap(BottomNav.PRESCRIPTION_BUTTON);
    verify(app, times(1)).tap(Receipt.ARCHIVED_PRESCRIPTIONS_BTN);

    verify(app, times(2)).tap(REDEEMED_PRESCRIPTION_STATUS_LABEL);
    verify(app, times(2)).tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    verify(app, times(2)).tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);
    verify(app, times(2)).acceptAlert();

    verify(app, times(1)).tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
  }

  @Test
  void shouldDeleteBatchRedeemablePrescriptions() {
    val task = DeleteBatchRedeemablePrescriptions.insideTheApp();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    List<WebElement> mockElements = new ArrayList<>();
    WebElement element1 = mock(WebElement.class);
    WebElement element2 = mock(WebElement.class);
    mockElements.add(element1);
    mockElements.add(element2);

    when(app.isPresent(Receipt.REDEEM_PRESCRIPTION_BTN)).thenReturn(true);
    when(app.getWebElements(REDEEMABLE_PRESCRIPTION_CARD_BUTTON)).thenReturn(mockElements);

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.PRESCRIPTION_BUTTON);
    verify(app, times(3)).tap(Mainscreen.REFRESH_BUTTON);

    verify(app, times(2)).tap(Receipt.REDEEMABLE_PRESCRIPTION_CARD_BUTTON);
    verify(app, times(2)).tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    verify(app, times(2)).tap(PrescriptionDetails.DELETE_PRESCRIPTION_ITEM_BUTTON);
    verify(app, times(2)).acceptAlert();
  }

  @Test
  void shouldDeleteRedeemedPrescription() {
    val task = DeleteRedeemedPrescription.insideTheApp();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    assertDoesNotThrow(() -> actor.attemptsTo(task));
    verify(app, times(1)).tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    verify(app, times(1)).tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);
    verify(app, times(1)).acceptAlert();
  }

  @Test
  void shouldListRedeemablePrescriptions() {
    val question = ListRedeemedPrescriptions.isEmpty();
    val actor = OnStage.theActorCalled(userName);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    when(driverAbility.isPresent(REDEEMED_PRESCRIPTION_STATUS_LABEL)).thenReturn(true);
    assertTrue(actor.asksFor(question));
  }

  @Test
  void shouldBePresent() {
    val question = TheLastPrescriptionInTheMainScreen.isPresent("pre", "validity", "status");
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.isPresent(any())).thenReturn(true);
    assertTrue(actor.asksFor(question));
    verify(app, times(1)).tap(Mainscreen.REFRESH_BUTTON);
  }

  @Test
  void shouldWaitUntilGone() {
    val question = TheLastPrescriptionInTheMainScreen.waitTillIsGone();
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.isPresent(XpathPageElement.xPathPageElement("xpath"))).thenReturn(false);
    assertFalse(actor.asksFor(question));
    verify(app, times(3)).tap(Mainscreen.REFRESH_BUTTON);
  }
}
