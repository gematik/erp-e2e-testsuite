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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseAndroidApp;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedeemButtonTest {
  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckRedeemButtonAvailability() {
    val useMockAppAbility = mock(UseAndroidApp.class);
    when(useMockAppAbility.isPresent(Receipt.REDEEM_PRESCRIPTION_BTN))
        .thenReturn(true); // pretend to find one
    val actor = OnStage.theActor("Alice").can(useMockAppAbility);
    assertTrue(actor.asksFor(IsRedeemable.inMainScreen()));
  }
}
