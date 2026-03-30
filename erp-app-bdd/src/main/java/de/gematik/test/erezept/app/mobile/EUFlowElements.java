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

package de.gematik.test.erezept.app.mobile;

import de.gematik.test.erezept.app.mobile.elements.PageElement;
import io.appium.java_client.AppiumBy;
import java.text.MessageFormat;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@RequiredArgsConstructor
@Getter
public enum EUFlowElements implements PageElement {
  GRANT_EU_CONSENT_BUTTON(
      "Grant EU consent button", () -> AppiumBy.accessibilityId("rdm_btn_eu_consent_accept")),
  SELECT_PRESCRIPTIONS_BUTTON(
      "Select prescriptions button", () -> AppiumBy.xpath("(//XCUIElementTypeButton)[3]")),
  SELECT_COUNTRY_BUTTON("Select country button", () -> AppiumBy.name("Land wählen")),
  SELECT_PRESCRIPTIONS_SCREEN_LEAVE_BUTTON(
      "Leave screen to select/deselect prescriptions button", () -> AppiumBy.name("Zurück")),
  COUNTRY_SEARCH_BAR_TEXT_FIELD(
      "Search bar text field for country",
      () -> AppiumBy.accessibilityId("ctl_txt_search_bar_field")),
  GENERATE_EU_REDEEM_CODE_BUTTON(
      "Generate EU redeem code button", () -> AppiumBy.name("Einlösecode generieren")),
  EU_REDEEM_BUTTON("EU redeem button", () -> AppiumBy.name("Einlösen")),
  KVNR_TEXT_FIELD(
      "Displayed KVNR text field",
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeStaticText[@name=\"Meine Krankenversicherungsnummer"
                  + " ist:\"]/following-sibling::XCUIElementTypeStaticText[1]")),
  EU_ACCESS_CODE_TEXT_FIELD(
      "Displayed EU-Access-Code text field",
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeStaticText[@name=\"Mein Einlösecode"
                  + " lautet:\"]/following-sibling::XCUIElementTypeStaticText[1]"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  public static PageElement forPrescriptionName(String prescriptionName) {
    return new PrescriptionEntry(prescriptionName);
  }

  public static PageElement forCountryName(String countryName) {
    return new CountryEntry(countryName);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class PrescriptionEntry implements PageElement {
    private final String prescriptionName;

    @Override
    public String getElementName() {
      return MessageFormat.format("Pharmacy Search Entry for {0}", prescriptionName);
    }

    @Override
    public Supplier<By> getAndroidLocator() {
      return () -> null;
    }

    @Override
    public Supplier<By> getIosLocator() {
      return () ->
          AppiumBy.iOSNsPredicateString(
              MessageFormat.format(
                  "type == \"XCUIElementTypeButton\" AND name CONTAINS" + " \"{0}\"",
                  prescriptionName));
    }
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class CountryEntry implements PageElement {
    private final String countryName;

    @Override
    public String getElementName() {
      return MessageFormat.format("Country Search Entry for {0}", countryName);
    }

    @Override
    public Supplier<By> getAndroidLocator() {
      return () -> null;
    }

    @Override
    public Supplier<By> getIosLocator() {
      return () ->
          AppiumBy.iOSNsPredicateString(
              MessageFormat.format(
                  "type == \"XCUIElementTypeButton\" AND name CONTAINS" + " \"{0}\"", countryName));
    }
  }
}
