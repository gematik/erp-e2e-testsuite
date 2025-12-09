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

import static de.gematik.test.erezept.app.mocker.EvdgaTestDummyFactory.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.questions.MovingToPrescription;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import net.serenitybdd.screenplay.ensure.PerformableExpectation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RequestAnEVDGAonIOSTest {

  private Actor theAppUser;
  private Actor theInsurance;

  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});

    val dmcList = ManageDataMatrixCodes.sheGetsPrescribed();
    theAppUser = createTestActor(dmcList);
    setupTestBundle(dmcList, theAppUser);
    theInsurance = createTestInsurance();
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldRequestEvdgaFromKtr() {
    val app = theAppUser.abilityTo(UseIOSApp.class);
    val comms = theInsurance.abilityTo(ManageCommunications.class);

    when(app.isDisplayed(EVDGADetails.CHOOSE_INSURANCE_BOTTOM_BAR)).thenReturn(true);

    // Skip the VisibleStatus validation
    doNothing().when(theAppUser).attemptsTo(any(PerformableExpectation.class));
    // skip the insurance selection step
    doNothing()
        .when(theAppUser)
        .attemptsTo(SelectEVDGAInsuranceOnIOS.fromListNamed(any(String.class)));
    // mock the incoming communications
    ManagedList<ExchangedCommunication> mockList = mock(ManagedList.class);
    when(comms.getExpectedCommunications()).thenReturn(mockList);

    val requestAnEvdga = RequestAnEVDGAonIOS.fromStack(DequeStrategy.LIFO).from(theInsurance);
    assertDoesNotThrow(() -> theAppUser.attemptsTo(requestAnEvdga));

    verify(app, times(1)).tap(EVDGADetails.CHOOSE_INSURANCE_BOTTOM_BAR);
    verify(app, times(1)).tapIfDisplayed(EVDGADetails.MAIN_ACTION_BUTTON);
    verify(app, times(1)).waitUntilElementIsVisible(EVDGADetails.DIGA_REQUESTED_ICON, 15000);
    verify(app, times(1)).tap(EVDGADetails.LEAVE_DIGA_DETAILS);
  }

  @Test
  void shouldThrowPreconditionErrorOnMissingBundle() {
    doReturn(Optional.empty()).when(theAppUser).asksFor(any(MovingToPrescription.class));

    val requestAnEvdga = RequestAnEVDGAonIOS.fromStack("erste").from(theInsurance);
    assertThrows(MissingPreconditionError.class, () -> theAppUser.attemptsTo(requestAnEvdga));
  }
}
