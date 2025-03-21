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
public enum PrescriptionDetails implements PageElement {
  PRESCRIPTION_CANNOT_BE_DELETED_INFO(
      "Alert information",
      () ->
          By.xpath(
              "(//XCUIElementTypeAlert[@name='Dieses Rezept wird im Rahmen einer Behandlung für Sie"
                  + " eingelöst und kann währenddessen nicht gelöscht werden.'])")),
  DELETE_BUTTON_TOOLBAR(
      "Delete Prescription tool bar item",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_toolbar_item")),
  REDEEM_ON_SITE(
      "Redeem Prescription directly on site in the pharmacy",
      () -> AppiumBy.accessibilityId("rdm_btn_pharmacy_tile")),
  ASSIGN_TO_PHARMACY_BUTTON(
      "Assign Prescription to a pharmacy remotely",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_redeem")),

  PRESCRIPTION_TITLE("Prescription Title", () -> AppiumBy.accessibilityId("prsc_dtl_txt_title")),
  DIRECT_ASSIGNMENT_BADGE(
      "Direct Assignment Badge",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_direct_assignment_info")),
  PRESCRIPTION_VALIDITY_TEXT(
      "Prescription Validity Text",
      () -> AppiumBy.accessibilityId("prsc_dtl_txt_prescription_validity")),
  PRESCRIPTION_ADDITIONAL_PAYMENT(
      "Additional Payment",
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeButton[@name='prsc_dtl_btn_scanned_prescription_info']/XCUIElementTypeStaticText[@name!='Zuzahlung']")),
  PRESCRIPTION_MEDICATION(
      "Prescription Medication", () -> AppiumBy.iOSNsPredicateString("label == \"Medikament\"")),
  DELETE_BUTTON_TOOLBAR_ITEM(
      "Delete Prescription button",
      () -> AppiumBy.accessibilityId("prsc_dtl_toolbar_menu_btn_delete")),
  DELETE_PRESCRIPTION_ITEM_BUTTON(
      "Delete Prescription button", () -> AppiumBy.name("Horizontaler Rollbalken, 1 Seite")),
  TECHNICAL_INFORMATION(
      "Technische Informationen",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_technical_informations")),
  LEAVE_DETAILS_BUTTON("Leave Prescription Details", () -> AppiumBy.name("Rezepte")),
  ;

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
