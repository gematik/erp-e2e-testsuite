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
 */

package de.gematik.test.erezept.app.task;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

public class DeleteRedeemedPrescription implements Task {
  public static DeleteRedeemedPrescription insideTheApp() {
    return new DeleteRedeemedPrescription();
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val driver = SafeAbility.getAbility(actor, UseTheApp.class);
    driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);
    driver.acceptAlert();
  }
}
