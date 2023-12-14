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

package de.gematik.test.erezept.app.task;

import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.BottomNav;
import de.gematik.test.erezept.app.mobile.elements.Receipt;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ProvideApoVzdInformation;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NavigateThroughRedeemablePrescriptionsTest {

  private String userName;
  private String pharmacyName;

  @BeforeEach
  void setUp() {
    OnStage.setTheStage(new Cast() {});
    val app = mock(UseIOSApp.class);
    when(app.getPlatformType()).thenReturn(PlatformType.IOS);

    userName = GemFaker.fakerName();
    pharmacyName = GemFaker.pharmacyName();

    val aliceIos = OnStage.theActorCalled((userName));
    val pharmacy = OnStage.theActorCalled((pharmacyName));
    givenThat(aliceIos).can(app);
    givenThat(pharmacy).can(ProvideApoVzdInformation.withName("Apotheke"));
    givenThat(pharmacy).can(ManageCommunications.sheExchanges());
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldCheckNavigateToRedeemablePrescriptionList() {
    val actor = OnStage.theActorCalled(userName);
    val actorPharmacy = OnStage.theActorCalled(pharmacyName);
    val task = NavigateThroughRedeemablePrescriptions.redeemTo(actorPharmacy);
    val prescriptionManager = SafeAbility.getAbility(actorPharmacy, ManageCommunications.class);
    val app = actor.abilityTo(UseIOSApp.class);

    // Act
    prescriptionManager
        .getExpectedCommunications()
        .append(
            ExchangedCommunication.from(actor.getName())
                .to(actorPharmacy.getName())
                .dispenseRequest());

    assertDoesNotThrow(() -> actor.attemptsTo(task));

    verify(app, times(1)).tap(BottomNav.PRESCRIPTION_BUTTON);
    verify(app, times(1)).tap(Receipt.REDEEM_PRESCRIPTION_BTN);
    verify(app, times(1)).tap(Receipt.RESERVE_IN_PHARMACY);

    val pharmacyName =
        SafeAbility.getAbility(actorPharmacy, ProvideApoVzdInformation.class).getApoVzdName();

    verify(app, times(1)).input(pharmacyName, Receipt.INPUT_SEARCH_BOX);
    verify(app, times(1)).tap(Receipt.SEARCH_BUTTON);
    verify(app, times(1)).tap(Receipt.SELECT_PHARMACY);
    verify(app, times(1)).tap(Receipt.SELECT_DELIVERY_METHOD_PICKUP, 1);
    verify(app, times(1)).tap(Receipt.REDEEM_PHARMACY_PRESCRIPTION);
    verify(app, times(1)).tap(Receipt.SUCCESSFULLY_REDEEM_TO_START_PAGE);

    ExchangedCommunication communication =
        prescriptionManager.getExpectedCommunications().consumeFirst();
    assertEquals(actor.getName(), communication.getSenderName());
    assertEquals(actorPharmacy.getName(), communication.getReceiverName());
  }
}
