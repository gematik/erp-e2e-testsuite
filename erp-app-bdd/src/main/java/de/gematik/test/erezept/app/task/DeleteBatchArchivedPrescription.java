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

package de.gematik.test.erezept.app.task;

import static de.gematik.test.erezept.app.mobile.elements.Receipt.REDEEMED_PRESCRIPTION_STATUS_LABEL;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@Slf4j
public class DeleteBatchArchivedPrescription implements Task {
  public static DeleteBatchArchivedPrescription insideTheApp() {
    return new DeleteBatchArchivedPrescription();
  }

  @Override
  public <T extends Actor> void performAs(T actor) {
    val driver = SafeAbility.getAbility(actor, UseTheApp.class);
    driver.tap(BottomNav.PRESCRIPTION_BUTTON);

    if (driver.isPresent(Receipt.ARCHIVED_PRESCRIPTIONS_BTN)) {
      driver.tap(Receipt.ARCHIVED_PRESCRIPTIONS_BTN);

      val elements = driver.getWebElements(REDEEMED_PRESCRIPTION_STATUS_LABEL);

      log.info(format("Found {0} receipt(s) to be deleted", elements.size()));
      for (int i = 0; i < elements.size(); i++) {
        log.info(format("Deleting {0}/{1} receipt(s)", i, elements.size()));
        driver.tap(REDEEMED_PRESCRIPTION_STATUS_LABEL);
        driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
        driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);
        driver.acceptAlert();
      }
    }
    driver.waitUntilElementIsClickable(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    driver.tap(PrescriptionDetails.LEAVE_DETAILS_BUTTON);
    driver.tap(BottomNav.SETTINGS_BUTTON);
  }
}
