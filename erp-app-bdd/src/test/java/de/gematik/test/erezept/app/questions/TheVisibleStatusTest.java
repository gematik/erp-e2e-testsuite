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

package de.gematik.test.erezept.app.questions;

import static de.gematik.bbriccs.fhir.codec.utils.FhirTestResourceUtil.*;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.codec.EmptyResource;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.EVDGAStatus;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.rest.ErpResponse;
import de.gematik.test.erezept.client.usecases.TaskAbortCommand;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import java.util.Map;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TheVisibleStatusTest {

  private String userName;
  private ErxPrescriptionBundle evdgaBundle;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val useAppAbility = mock(UseIOSApp.class);
    when(useAppAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    val prescriptionId = PrescriptionId.random();
    val taskId = TaskId.from(prescriptionId);
    val task = mock(ErxTask.class);
    evdgaBundle = mock(ErxPrescriptionBundle.class);

    when(evdgaBundle.getTask()).thenReturn(task);
    when(evdgaBundle.getTask().getTaskId()).thenReturn(taskId);

    // assemble the screenplay
    userName = GemFaker.fakerName();
    val theAppUser = OnStage.theActorCalled(userName);
    givenThat(theAppUser).can(useAppAbility);
    val erpClient = mock(ErpClient.class);
    val useErpClientAbility = mock(UseTheErpClient.class);
    when(useErpClientAbility.getClient()).thenReturn(erpClient);
    givenThat(theAppUser).can(useErpClientAbility);
    givenThat(theAppUser).can(ManageDataMatrixCodes.sheGetsPrescribed());
    when(useAppAbility.isDisplayed(EVDGADetails.DIGA_TITLE)).thenReturn(true);

    // make sure the teardown does not run into an NPE
    val mockResponse =
        ErpResponse.forPayload(createOperationOutcome(), EmptyResource.class)
            .withStatusCode(404)
            .withHeaders(Map.of())
            .andValidationResult(createEmptyValidationResult());
    when(erpClient.request(any(TaskAbortCommand.class))).thenReturn(mockResponse);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldReturnReadyForRequestWhenAppShowsRequestable() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.OPEN_VALIDITY_DRAWER)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.READY_FOR_REQUEST, status);
  }

  @Test
  void shouldReturnWaitingOrAcceptedWhenAppShowsInProgress() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.DIGA_REQUESTED_ICON)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.WAITING_OR_ACCEPTED, status);
  }

  @Test
  void shouldReturnDeclinedWhenAppShowsDeclineNote() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.DIGA_DECLINE_NOTE)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.DECLINED, status);
  }

  @Test
  void shouldReturnGrantedWhenAppShowsCode() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.COPY_CODE_ICON)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.GRANTED, status);
  }

  @Test
  void shouldReturnDownloadedWhenAppShowsDownloadConfirmation() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.DIGA_DOWNLOADED_DISPLAY)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.DOWNLOADED, status);
  }

  @Test
  void shouldReturnActivatedWhenAppShowsActivatedConfirmation() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.DIGA_ACTIVATED_DISPLAY)).thenReturn(true);
    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.ACTIVATED, status);
  }

  @Test
  void shouldReturnNullWhenNoStatusIsMatched() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(any())).thenReturn(false);
    when(app.isDisplayed(EVDGADetails.DIGA_TITLE)).thenReturn(true);

    EVDGAStatus status = actor.asksFor(TheVisibleStatus.ofThe(evdgaBundle));
    assertEquals(EVDGAStatus.NULL, status);
  }

  @Test
  void shouldThrowAssertionErrorWhenNotOnDIGADetailsScreen() {
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);

    when(app.isDisplayed(EVDGADetails.DIGA_TITLE)).thenReturn(false);

    val theVisibleStatus = TheVisibleStatus.ofThe(evdgaBundle);
    assertThrows(AssertionError.class, () -> theVisibleStatus.answeredBy(actor));
  }
}
