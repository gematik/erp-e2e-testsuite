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
package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmartcardFactory;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigateThroughCardwallTest {

  private final SmartcardArchive sca = SmartcardFactory.getArchive();

  private String userName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    userName = GemFaker.fakerName();
    val aliceIos = OnStage.theActorCalled((userName));
    givenThat(aliceIos).can(app);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldHaveTheCorrectCardwallBehavior() {
    val task = NavigateThroughCardwall.byMappingVirtualEgkFrom(sca);
    val actor = OnStage.theActorCalled(userName);
    val app = actor.abilityTo(UseIOSApp.class);
    
    when(app.getText(Profile.USER_KVNR)).thenReturn("X110406067");

    assertDoesNotThrow(() -> actor.attemptsTo(task));
    verify(app, times(1)).tap(BottomNav.PRESCRIPTION_BUTTON);
    verify(app, times(1)).tap(Mainscreen.LOGIN_BUTTON);
    verify(app, times(1)).tap(CardWall.ADD_HEALTH_CARD_BUTTON);

    verify(app, times(1)).input("123123", CardWall.CAN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.CAN_ACCEPT_BUTTON);
    verify(app, times(1)).inputPassword("123456", CardWall.PIN_INPUT_FIELD);
    verify(app, times(1)).tap(CardWall.PIN_ACCEPT_BUTTON);

    verify(app, times(1)).tap(CardWall.DONT_SAVE_CREDENTIAL_BUTTON);
    verify(app, times(1)).tap(CardWall.CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON);
    verify(app, times(1)).tap(CardWall.START_NFC_READOUT_BUTTON);
    
    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
    verify(app, times(1)).tap(Settings.BOTTOM_USER_PROFILE);

    val aliceIos = OnStage.theActorCalled((userName));
    val egk = aliceIos.abilityTo(ProvideEGK.class);
    assertNotNull(egk);
    assertEquals(sca.getEgkByKvnr("X110406067"), egk.getEgk());
  }
}
