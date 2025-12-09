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
import de.gematik.test.erezept.app.mobile.elements.EVDGADetails;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.questions.MovingToEVDGAPrescription;
import de.gematik.test.erezept.app.questions.TheVisibleStatus;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DmcStack;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.core.steps.Instrumented;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.ensure.Ensure;

@Slf4j
@RequiredArgsConstructor
public class DeleteAnEVDGAOnIOS implements Task {

  private final DequeStrategy deque;
  private final EVDGAStatus expectedStatus;

  public static DeleteAnEVDGAOnIOS.Builder fromStack(String order) {
    return fromStack(DequeStrategy.fromString(order));
  }

  public static DeleteAnEVDGAOnIOS.Builder fromStack(DequeStrategy deque) {
    return new DeleteAnEVDGAOnIOS.Builder(deque);
  }

  @Override
  @Step("{0} fragt eine DIGA beim Kostenträger an")
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseIOSApp.class);
    val dmcAbility = SafeAbility.getAbility(actor, ManageDataMatrixCodes.class);
    val dmc = deque.chooseFrom(dmcAbility.chooseStack(DmcStack.ACTIVE));

    ErxPrescriptionBundle evdgaBundle = actor.asksFor(MovingToEVDGAPrescription.with(deque));

    actor.attemptsTo(Ensure.that(TheVisibleStatus.ofThe(evdgaBundle)).isEqualTo(expectedStatus));

    app.tap(EVDGADetails.DIGA_THREE_DOT_MENU);
    // Element existiert bereits in Prescription details
    app.tap(PrescriptionDetails.DELETE_PRESCRIPTION_ITEM_BUTTON);
    app.acceptAlert();

    dmcAbility.moveToDeleted(dmc);
    app.waitUntilElementIsVisible(Mainscreen.REFRESH_BUTTON, 60000);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {

    private final DequeStrategy deque;

    public DeleteAnEVDGAOnIOS with(String status) {
      return with(EVDGAStatus.fromString(status));
    }

    public DeleteAnEVDGAOnIOS with(EVDGAStatus status) {
      return Instrumented.instanceOf(DeleteAnEVDGAOnIOS.class).withProperties(deque, status);
    }
  }
}
