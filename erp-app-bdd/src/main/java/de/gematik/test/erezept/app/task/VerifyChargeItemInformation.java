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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.test.erezept.app.abilities.UseTheApp;
import de.gematik.test.erezept.app.mobile.elements.ChargeItemDrawer;
import de.gematik.test.erezept.app.parsers.ChargeItemParser;
import de.gematik.test.erezept.fhir.r4.erp.ErxChargeItemBundle;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

@RequiredArgsConstructor
public class VerifyChargeItemInformation implements Task {
  private final ErxChargeItemBundle erxChargeItemBundle;

  @Override
  public <T extends Actor> void performAs(T actor) {
    val app = SafeAbility.getAbility(actor, UseTheApp.class);

    val actualHandedOverDate = app.getText(ChargeItemDrawer.HANDED_OVER_TEXT_FIELD);
    val actualEnterer = app.getText(ChargeItemDrawer.PHARMACY_TEXT_FIELD);
    val actualEnteredDate = app.getText(ChargeItemDrawer.ENTERED_DATE_TEXT_FIELD);
    val actualPrice = app.getText(ChargeItemDrawer.PRICE_TEXT_FIELD);

    val expectedHandedOverDate = ChargeItemParser.getExpectedHandedOverDate(erxChargeItemBundle);
    val expectedEnterer = ChargeItemParser.getExpectedEnterer(erxChargeItemBundle);
    val expectedEnteredDate = ChargeItemParser.getExpectedEnteredDate(erxChargeItemBundle);
    val expectedPrice = ChargeItemParser.getExpectedPrice(erxChargeItemBundle);

    // TODO: will be fixed with https://service.gematik.de/browse/ERA-13629
    // assertEquals(expectedHandedOverDate, actualHandedOverDate, "Handed over date was wrong in the
    // charge item");
    assertEquals(expectedEnterer, actualEnterer, "Enterer was wrong in the charge item");
    assertEquals(
        expectedEnteredDate, actualEnteredDate, "Entered date was wrong in the charge item");
    assertEquals(expectedPrice, actualPrice, "Price was wrong in the charge item");
  }

  public static VerifyChargeItemInformation forErxChargeItemBundle(
      ErxChargeItemBundle erxChargeItemBundle) {
    return new VerifyChargeItemInformation(erxChargeItemBundle);
  }
}
