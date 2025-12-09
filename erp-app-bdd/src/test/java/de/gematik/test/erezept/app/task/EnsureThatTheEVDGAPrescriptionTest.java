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

package de.gematik.test.erezept.app.task;

import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.createTestActor;
import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.setupTestBundle;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvEvdgaBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvHealthAppRequest;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.PerformableExpectation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnsureThatTheEVDGAPrescriptionTest {
  private Actor theAppUser;
  private ErxPrescriptionBundle prescriptionBundle;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val dmcList = ManageDataMatrixCodes.sheGetsPrescribed();
    theAppUser = createTestActor(dmcList);
    prescriptionBundle = setupTestBundle(dmcList, theAppUser);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldReceiveEVDGACode() {
    val app = theAppUser.abilityTo(UseIOSApp.class);

    KbvEvdgaBundle evdgaBundle = mock(KbvEvdgaBundle.class);
    when(prescriptionBundle.getEvdgaBundle()).thenReturn(Optional.of(evdgaBundle));
    KbvHealthAppRequest appRequest = mock(KbvHealthAppRequest.class);
    when(evdgaBundle.getHealthAppRequest()).thenReturn(appRequest);

    when(app.getText(EVDGADetails.DIGA_TITLE)).thenReturn("DIGA_TEST_TITLE");
    when(appRequest.getName()).thenReturn("DIGA_TEST_TITLE");

    val task = mock(ErxTask.class);
    when(prescriptionBundle.getTask()).thenReturn(task);

    SimpleDateFormat formatter = new SimpleDateFormat("dd.MM.yyyy");
    val dc = DateConverter.getInstance();

    val startDate = new Date();
    val endDate = new Date();

    when(task.getAuthoredOn()).thenReturn(startDate);
    when(app.getText(EVDGADetails.VALIDITY_START)).thenReturn(formatter.format(startDate));

    when(task.getExpiryDate()).thenReturn(endDate);
    when(app.getText(EVDGADetails.VALIDITY_END))
        .thenReturn(formatter.format(dc.localDateToDate(dc.dateToLocalDate(endDate).minusDays(1))));

    // Skip the VisibleStatus validation
    doNothing().when(theAppUser).attemptsTo(any(PerformableExpectation.class));

    val ensureEvdgaPrescription =
        EnsureThatTheEVDGAPrescription.fromStack(DequeStrategy.LIFO).isShownCorrectly();
    assertDoesNotThrow(() -> theAppUser.attemptsTo(ensureEvdgaPrescription));

    verify(app, times(1)).swipeIntoView(SwipeDirection.DOWN, EVDGADetails.DIGA_TITLE);
    verify(app, times(1)).swipeIntoView(SwipeDirection.UP, EVDGADetails.OPEN_VALIDITY_DRAWER);
    verify(app, times(1)).tap(EVDGADetails.OPEN_VALIDITY_DRAWER);
    verify(app, times(1)).swipe(SwipeDirection.DOWN);
    verify(app, times(1)).tap(EVDGADetails.LEAVE_DIGA_DETAILS);
  }

  @Test
  void shouldThrowPreconditionErrorOnMissingPrescription() {

    doReturn(Optional.empty()).when(theAppUser).asksFor(any(MovingToPrescription.class));

    val ensureEvdgaPrescription =
        EnsureThatTheEVDGAPrescription.fromStack("erste").isShownCorrectly();
    assertThrows(
        MissingPreconditionError.class, () -> theAppUser.attemptsTo(ensureEvdgaPrescription));
  }

  @Test
  void shouldThrowPreconditionErrorOnMissingBundle() {

    doReturn(Optional.empty()).when(prescriptionBundle).getEvdgaBundle();

    val ensureEvdgaPrescription =
        EnsureThatTheEVDGAPrescription.fromStack("erste").isShownCorrectly();
    assertThrows(MissingFieldException.class, () -> theAppUser.attemptsTo(ensureEvdgaPrescription));
  }
}
