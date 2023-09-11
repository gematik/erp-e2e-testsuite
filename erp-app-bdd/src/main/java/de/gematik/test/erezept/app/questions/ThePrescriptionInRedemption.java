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

package de.gematik.test.erezept.app.questions;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.Mainscreen;
import de.gematik.test.erezept.app.mobile.elements.PrescriptionDetails;
import de.gematik.test.erezept.app.mobile.elements.Settings;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Question;

@Slf4j
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ThePrescriptionInRedemption implements Question<Boolean> {
  private final String prescription;

  @Override
  public Boolean answeredBy(Actor actor) {

    val driver = SafeAbility.getAbilityThatExtends(actor, UseTheApp.class);
    driver.tapByLabel(Mainscreen.PRESCRIPTION_LIST_ELEMENT_NAME, prescription);
    driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR);
    driver.tap(PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM);

    val message = driver.isPresent(PrescriptionDetails.PRESCRIPTION_CANNOT_BE_DELETED_INFO);

    driver.acceptAlert();
    driver.tap(Settings.LEAVE_BUTTON);
    return message;
  }

  public static ThePrescriptionInRedemption canNotBeDeleted(String prescription) {
    return new ThePrescriptionInRedemption(prescription);
  }
}
