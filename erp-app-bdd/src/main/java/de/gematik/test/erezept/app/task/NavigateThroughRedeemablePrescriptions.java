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

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.abilities.ProvideApoVzdInformation;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class NavigateThroughRedeemablePrescriptions implements Task {

  private final Actor receivingPharmacy;

  public static NavigateThroughRedeemablePrescriptions redeemTo(Actor pharmacy) {
    return new NavigateThroughRedeemablePrescriptions(pharmacy);
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val driver = SafeAbility.getAbility(actor, UseTheApp.class);

    driver.tap(BottomNav.PRESCRIPTION_BUTTON);

    driver.tap(Mainscreen.REFRESH_BUTTON);
    driver.waitUntilElementIsVisible(Receipt.REDEEM_PRESCRIPTION_BTN);

    driver.tap(Receipt.REDEEM_PRESCRIPTION_BTN);
    driver.tap(Receipt.RESERVE_IN_PHARMACY);

    // required the step "Und die Apotheke Am Flughafen hat Zugriff auf ihre SMC-B"
    val pharmacyApoVzdAbility =
        SafeAbility.getAbility(receivingPharmacy, ProvideApoVzdInformation.class);
    val pharmacyName = pharmacyApoVzdAbility.getApoVzdName();
    log.info("Getting pharmacy from ProvideApoVzdInformation ability");

    driver.input(pharmacyName, Receipt.INPUT_SEARCH_BOX);

    driver.tap(Receipt.SEARCH_BUTTON);
    driver.tap(Receipt.SELECT_PHARMACY);
    driver.tap(Receipt.SELECT_DELIVERY_METHOD_PICKUP, 1);

    driver.tap(Receipt.REDEEM_PHARMACY_PRESCRIPTION);
    driver.tap(Receipt.SUCCESSFULLY_REDEEM_TO_START_PAGE);

    val prescriptionManager = SafeAbility.getAbility(receivingPharmacy, ManageCommunications.class);
    prescriptionManager
        .getExpectedCommunications()
        .append(
            ExchangedCommunication.from(actor.getName())
                .to(receivingPharmacy.getName())
                .dispenseRequest());
  }
}
