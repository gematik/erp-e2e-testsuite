/*
 * Copyright 2023 gematik GmbH
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

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RedeemedPrescriptionTest {
  private UseIOSApp iosAbility;
  private final String iosUserName = "Alice";

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    iosAbility = mock(UseIOSApp.class);
    when(iosAbility.getPlatformType()).thenReturn(PlatformType.IOS);

    val aliceIos = OnStage.theActorCalled((iosUserName));
    givenThat(aliceIos).can(iosAbility);
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void lastRedeemedPrescriptionWithStatusAndTaskIdTest() {
    val actor = OnStage.theActorCalled(iosUserName);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.isPresent(any())).thenReturn(true);
    when(driverAbility.getText(any())).thenReturn("0001");

    assertTrue(
        actor.asksFor(
            RedeemedPrescription.lastRedeemedPrescriptionWithStatusAndTaskId(
                "Schmertzmittel", "Engelöst", "0001")));
  }

  @Test
  void PrescriptionListIsElemenThereTest() {
    val actor = OnStage.theActorCalled(iosUserName);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.isPresent(Receipt.PRESCRIPTION_STATUS_LABEL)).thenReturn(true);
    assertTrue(actor.asksFor(PrescriptionList.isThereElements()));
  }

  @Test
  void PrescriptionListIsElemenNotThereTest() {
    val actor = OnStage.theActorCalled(iosUserName);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.isPresent(Receipt.PRESCRIPTION_STATUS_LABEL)).thenReturn(false);
    assertFalse(actor.asksFor(PrescriptionList.isThereElements()));
  }

  @Test
  void taskIdofTheLastPrescriptionInTheMainScreenTest() {
    val question = TheTaskId.ofTheLastPrescriptionInTheMainScreen();
    val actor = OnStage.theActorCalled(iosUserName);
    val driverAbility = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);

    when(driverAbility.getText(Receipt.TASKID)).thenReturn("label");
    when(driverAbility.isPresent(Receipt.PRESCRIPTION_STATUS_LABEL)).thenReturn(false);
    
    assertTrue(actor.asksFor(question).equals("label"));
  }
}
