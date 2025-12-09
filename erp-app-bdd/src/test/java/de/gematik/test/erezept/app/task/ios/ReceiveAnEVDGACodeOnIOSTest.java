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

import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.createTestActor;
import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.setupTestBundle;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.PerformableExpectation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReceiveAnEVDGACodeOnIOSTest {

  private Actor theAppUser;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val dmcList = ManageDataMatrixCodes.sheGetsPrescribed();
    theAppUser = createTestActor(dmcList);
    setupTestBundle(dmcList, theAppUser);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldReceiveEVDGACode() {
    val app = theAppUser.abilityTo(UseIOSApp.class);

    // Skip the VisibleStatus validation
    doNothing().when(theAppUser).attemptsTo(any(PerformableExpectation.class));

    val receiveAnEvdgaCode = ReceiveAnEVDGACodeOnIOS.fromStack(DequeStrategy.LIFO);
    assertDoesNotThrow(() -> theAppUser.attemptsTo(receiveAnEvdgaCode));

    verify(app, times(2)).tap(EVDGADetails.MAIN_ACTION_BUTTON);
    verify(app, times(1)).tap(EVDGADetails.LEAVE_DIGA_DETAILS);
  }

  @Test
  void shouldThrowPreconditionErrorOnMissingBundle() {
    doReturn(Optional.empty()).when(theAppUser).asksFor(any(MovingToPrescription.class));

    val receiveAnEvdgaCode = ReceiveAnEVDGACodeOnIOS.fromStack("erste");
    assertThrows(MissingPreconditionError.class, () -> theAppUser.attemptsTo(receiveAnEvdgaCode));
  }
}
