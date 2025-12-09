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
public enum EVDGADetails implements PageElement {
  LEAVE_DIGA_DETAILS(
      "Back button in EVDGA details",
      () -> By.xpath("(//XCUIElementTypeButton[@name='Rezepte'])[1]")),
  DIGA_THREE_DOT_MENU(
      "DIGA three dot menu", () -> AppiumBy.accessibilityId("diga_dtl_btn_toolbar_item")),
  DIGA_TITLE("DIGA name as title", () -> AppiumBy.accessibilityId("diga_dtl_txt_name_header")),
  DIGA_EXTENDED_DETAILS(
      "DIGA details", () -> By.xpath("//XCUIElementTypeButton[@name=\"Details\"]")),
  DIGA_OVERVIEW(
      "DIGA frontpage details (overview)",
      () -> By.xpath("//XCUIElementTypeButton[@name='Überblick']")),
  DISMISS_FHIR_VZD_DIALOG(
      "dismiss button", () -> By.xpath("//XCUIElementTypeButton[@name='Okay']")),
  TECHNICAL_INFORMATION(
      "DIGA technical info", () -> AppiumBy.accessibilityId("prsc_dtl_btn_technical_informations")),
  OPEN_VALIDITY_DRAWER(
      "Tap on calendar icon to open drawer",
      () -> By.xpath("//XCUIElementTypeImage[@name='calendar.badge.clock']")),

  VALIDITY_START(
      "The field showing the start of the EVDGA validity",
      () -> AppiumBy.accessibilityId("diga_dtl_valid_txt_start_date")),
  VALIDITY_END(
      "The field showing the end of the EVDGA validity",
      () -> AppiumBy.accessibilityId("diga_dtl_valid_txt_end_date")),
  DIGA_DECLINE_NOTE(
      "the field displaying the decline note from ktr",
      () -> AppiumBy.accessibilityId("diga_dtl_txt_decline_note")),
  DIGA_REQUESTED_ICON(
      "The icon only shown when DIGA has been requested or is in progress",
      () -> By.xpath("//XCUIElementTypeImage[@name='hourglass']")),
  COPY_CODE_ICON(
      "The icon only shown when DIGA has been granted and code received",
      () -> By.xpath("//XCUIElementTypeImage[@name='doc.on.doc']")),
  DIGA_DOWNLOADED_DISPLAY(
      "screen shows four checkmarks during this step, we are checking that the 4th exists",
      () -> By.xpath("(//XCUIElementTypeImage[@name='checkmark'])[4]")),
  DIGA_ACTIVATED_DISPLAY(
      /*May show 6 checkmarks because clipboard gets copied on execution, but must be minimum of 5*/
      "screen shows five checkmarks during this step, we are checking that the 5th exists",
      () -> By.xpath("(//XCUIElementTypeImage[@name='checkmark'])[5]")),
  CHOOSE_INSURANCE_BOTTOM_BAR(
      "DIGA select insurance",
      () -> AppiumBy.accessibilityId("diga_dtl_btn_main_select_insurance")),
  MAIN_ACTION_BUTTON(
      "main action button in bottom bar",
      () -> AppiumBy.accessibilityId("diga_dtl_btn_main_action"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
