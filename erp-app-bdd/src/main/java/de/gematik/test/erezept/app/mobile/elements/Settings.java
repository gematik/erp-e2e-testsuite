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

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum Settings implements PageElement {
  SETTINGS_HEADER_LINE(
      "Settings Header Line",
      () -> null,
      () ->
          AppiumBy.iOSNsPredicateString(
              "type == \"XCUIElementTypeStaticText\" AND label == \"Einstellungen\"")),
  LEAVE_BUTTON(
      "Leave Settings Menu Button",
      () -> By.tagName("BottomNavigation.PrescriptionButton"),
      BottomNav.PRESCRIPTION_BUTTON.getIosLocator()),
  DEBUG_BUTTON("Debug Menu Button", () -> null, () -> AppiumBy.accessibilityId("stg_btn_debug")),
  LEGAL_TEXT(
      "Legal text element",
      () -> null,
      () -> AppiumBy.accessibilityId("stg_lno_txt_header_legal_info")),

  TERMS_OF_USE_TITLE(
      "Nutzungsbedingungen Kopfzeile",
      () -> null,
      () -> AppiumBy.iOSNsPredicateString("label==\"Nutzungsbedingungen E-Rezept-App\"")),
  DATA_PROTECTION_TITLE(
      "Datenschutzerklärung Kopfzeile",
      () -> null,
      () -> AppiumBy.iOSNsPredicateString("label==\"Datenschutzerklärung\"")),

  ONBOARDING_PRIVACY(
      "Stand of the user privacy",
      () -> null,
      () -> AppiumBy.accessibilityId("onb_txt_terms_of_privacy")),

  ONBOARDING_TERM_OF_USE(
      "onboarding button term of use",
      () -> null,
      () -> AppiumBy.accessibilityId("onb_txt_terms_of_use")),

  CLOSE_TERM_OF_USE(
      "Close term of use button",
      () -> null,
      () -> AppiumBy.accessibilityId("stg_btn_terms_of_use_close")),

  IMPRINT_BUTTON("link to the imprint", () -> null, () -> AppiumBy.accessibilityId("Impressum")),
  IMPRINT_LEGEND(
      "the imprint to assert for",
      () -> null,
      () -> AppiumBy.accessibilityId("Dr. med. Markus Leyck Dieken")),
  TERMS_BUTTON(
      "Terms of use button", () -> null, () -> AppiumBy.accessibilityId("Nutzungsbedingungen")),
  PRIVACY_BUTTON(
      "Datenschutz of use button", () -> null, () -> AppiumBy.accessibilityId("Datenschutz")),

  OPEN_SOURCE_BUTTON(
      "Open Source button", () -> null, () -> AppiumBy.accessibilityId("Open Source Lizenzen")),
  OPEN_SOURCE_LEGEND(
      "Open Source legend", () -> null, () -> AppiumBy.accessibilityId("FHIRModels 0.4.0")),
  NEW_USER_PROFILE_BUTTON(
      "Add new user profile button", null, () -> AppiumBy.accessibilityId("stg_btn_new_profile"));

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
