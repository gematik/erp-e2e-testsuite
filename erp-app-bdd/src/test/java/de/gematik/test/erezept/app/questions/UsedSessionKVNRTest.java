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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Profile;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SerenityJUnit5Extension.class)
class UsedSessionKVNRTest {

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldDetectAvailableKVNR() {
    val app = mock(UseIOSApp.class);
    when(app.getText(Profile.USER_KVNR)).thenReturn("X11551155"); // pretend to find one
    when(app.isPresent(Settings.SETTINGS_HEADER_LINE)).thenReturn(true);
    val actor = OnStage.theActorCalled(GemFaker.fakerName());
    actor.can(app);
    val readKvnr = actor.asksFor(UsedSessionKVNR.fromUserProfile());
    assertEquals("X11551155", readKvnr);
    verify(app, times(1)).tap(BottomNav.SETTINGS_BUTTON);
  }

  @Test
  void shouldReTapSettingsButton() {
    val app = mock(UseIOSApp.class);
    when(app.getText(Profile.USER_KVNR)).thenReturn("X11551155"); // pretend to find one
    when(app.isPresent(Settings.SETTINGS_HEADER_LINE)).thenReturn(false);
    val actor = OnStage.theActorCalled(GemFaker.fakerName());
    actor.can(app);
    val readKvnr = actor.asksFor(UsedSessionKVNR.fromUserProfile());
    assertEquals("X11551155", readKvnr);
    verify(app, times(2)).tap(BottomNav.SETTINGS_BUTTON);
  }
}
