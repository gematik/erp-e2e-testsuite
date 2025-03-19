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

package de.gematik.test.erezept.app.mobile.elements;

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum BottomNav implements PageElement {
  PRESCRIPTION_BUTTON(
      "Prescriptions Menu Button",
      () -> null,
      () ->
          AppiumBy.iOSNsPredicateString(
              "type == \"XCUIElementTypeButton\" AND label == \"Rezepte\"")),

  PHARMACY_SEARCH_BUTTON(
      "Pharmacy Search Menu Button", () -> null, () -> AppiumBy.name("Apothekensuche")),

  PRESCRIPTION_ORDERS_BUTTON(
      "Prescription Orders Menu Button", () -> null, () -> AppiumBy.name("Bestellungen")),

  SETTINGS_BUTTON(
      "Settings Menu Button",
      () -> By.tagName("BottomNavigation.SettingsButton"),
      () -> AppiumBy.name("Einstellungen")),
  ;
  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
