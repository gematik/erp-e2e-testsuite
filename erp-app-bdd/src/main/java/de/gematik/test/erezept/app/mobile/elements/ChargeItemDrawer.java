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

package de.gematik.test.erezept.app.mobile.elements;

import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum ChargeItemDrawer implements PageElement {
  HANDED_OVER_TEXT_FIELD(
      "Text field of the handed over date",
      () ->
          By.xpath(
              "//XCUIElementTypeStaticText[@name=\"Ausgestellt"
                  + " am\"]/preceding-sibling::XCUIElementTypeStaticText[1]")),
  PHARMACY_TEXT_FIELD(
      "Text field of the pharmacy",
      () ->
          By.xpath(
              "//XCUIElementTypeStaticText[@name=\"Eingelöst"
                  + " in\"]/preceding-sibling::XCUIElementTypeStaticText[1]")),
  ENTERED_DATE_TEXT_FIELD(
      "Text field of the entered date",
      () ->
          By.xpath(
              "//XCUIElementTypeStaticText[@name=\"Eingelöst"
                  + " am\"]/preceding-sibling::XCUIElementTypeStaticText[1]")),
  OPEN_CHARGE_ITEM_OVERVIEW_BUTTON(
      "Text field of the entered date", () -> By.name("Zur Übersicht der Apothekenrechnungen")),
  PRICE_TEXT_FIELD(
      "Text field of the price",
      () ->
          By.xpath(
              "//XCUIElementTypeStaticText[@name=\"Gesamtpreis\"]/preceding-sibling::XCUIElementTypeStaticText[1]"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
