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

import de.gematik.test.erezept.app.abilities.UseIOSApp;
import de.gematik.test.erezept.app.mobile.EVDGAStatus;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.questions.MovingToEVDGAPrescription;
import de.gematik.test.erezept.app.questions.TheVisibleStatus;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ExchangedCommunication;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
@RequiredArgsConstructor
public class RequestAnEVDGAonIOS implements Task {

  private final DequeStrategy deque;
  private final Actor ktr;

  public static RequestAnEVDGAonIOS.Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static RequestAnEVDGAonIOS.Builder fromStack(DequeStrategy deque) {
    return new RequestAnEVDGAonIOS.Builder(deque);
  }

  @Override
  @Step("{0} fragt eine DIGA beim Kostenträger an")
  public <T extends Actor> void performAs(T actor) {

    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val communicationsKTR = SafeAbility.getAbility(ktr, ManageCommunications.class);

    ErxPrescriptionBundle evdgaBundle = actor.asksFor(MovingToEVDGAPrescription.with(deque));

    // überprüfe ob keine Versicherung assoziiert ist
    if (app.isDisplayed(EVDGADetails.CHOOSE_INSURANCE_BOTTOM_BAR)) {
      app.acceptAlert();
      app.tap(EVDGADetails.CHOOSE_INSURANCE_BOTTOM_BAR);
      // gets the name associated with the SmcB
      val ktrSMCBName = ktr.abilityTo(UseSMCB.class).getSmcB().getOwnerData().getCommonName();
      actor.attemptsTo(SelectEVDGAInsuranceOnIOS.fromListNamed(ktrSMCBName));
    }

    app.tapIfDisplayed(EVDGADetails.MAIN_ACTION_BUTTON);
    app.swipe(SwipeDirection.DOWN);
    app.waitUntilElementIsVisible(EVDGADetails.DIGA_REQUESTED_ICON, 15000);

    actor.attemptsTo(
        Ensure.that(TheVisibleStatus.ofThe(evdgaBundle))
            .isEqualTo(EVDGAStatus.WAITING_OR_ACCEPTED));

    // Inform the KTR to expect the dispense Request
    communicationsKTR
        .getExpectedCommunications()
        .append(
            ExchangedCommunication.sentBy(actor)
                .to(ktr)
                .dispenseRequestBasedOn(evdgaBundle.getTask().getPrescriptionId()));

    app.tap(EVDGADetails.LEAVE_DIGA_DETAILS);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final DequeStrategy deque;

    public RequestAnEVDGAonIOS from(Actor ktr) {
      return new RequestAnEVDGAonIOS(deque, ktr);
    }
  }
}
