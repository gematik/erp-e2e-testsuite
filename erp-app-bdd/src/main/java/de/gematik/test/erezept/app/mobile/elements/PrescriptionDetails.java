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
  ADDRESS_NAME_FIELD(
      "Name of the patient in the address field",
      () -> AppiumBy.accessibilityId("pha_redeem_address_name")),
  EDIT_ADDRESS_BUTTON(
      "square and pencil button to edit the address",
      () -> AppiumBy.accessibilityId("square.and.pencil")),
  CONTACT_ADDRESS_NAME_INPUT(
      "name field of address in contact view", () -> AppiumBy.name("pha_contact_address_name")),
  CLEAR_CONTACT_ADDRESS_NAME_BUTTON(
      "clear button for name field of address in contact view",
      () -> AppiumBy.name("Text löschen")),
  SAVE_CONTACT_BUTTON(
      "button to save the contact information", () -> AppiumBy.name("pha_contact_btn_save")),
  CHOOSE_PHARMACY_BUTTON(
      "Choose a pharmacy", () -> AppiumBy.accessibilityId("pha_redeem_btn_add_pharmacy")),
  PRESCRIPTION_TITLE("Prescription Title", () -> AppiumBy.accessibilityId("prsc_dtl_txt_title")),
  DIRECT_ASSIGNMENT_BADGE(
      "Direct Assignment Badge",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_direct_assignment_info")),
  PRESCRIPTION_STATUS_TEXT(
      "Prescription Status Text",
      () -> AppiumBy.accessibilityId("prsc_dtl_txt_prescription_validity")),
  PRESCRIPTION_STATUS_INFO(
      "Prescription Status Info",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_prescription_validity_info")),
  PRESCRIPTION_ADDITIONAL_PAYMENT(
      "Additional Payment",
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeButton[@name='prsc_dtl_btn_scanned_prescription_info']/XCUIElementTypeStaticText[@name!='Zuzahlung']")),
  PRESCRIPTION_MEDICATION(
      "Prescription Medication", () -> AppiumBy.accessibilityId("prsc_dtl_btn_medication")),
  DELETE_BUTTON_TOOLBAR_ITEM(
      "Delete Prescription button",
      () -> AppiumBy.accessibilityId("prsc_dtl_toolbar_menu_btn_delete")),
  DELETE_PRESCRIPTION_ITEM_BUTTON(
      "Delete Prescription button", () -> AppiumBy.name("Horizontaler Rollbalken, 1 Seite")),
  TECHNICAL_INFORMATION(
      "Technische Informationen",
      () -> AppiumBy.accessibilityId("prsc_dtl_btn_technical_informations")),
  LEAVE_DETAILS_BUTTON("Leave Prescription Details", () -> AppiumBy.name("Rezepte")),
  BACK_BUTTON(
      "Prescription Details Back Button",
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeNavigationBar[@name=\"Rezeptdetails\"]/XCUIElementTypeButton[1]")),
  SELECT_INSURANCE(
      "Select insurance button",
      () -> AppiumBy.accessibilityId("diga_dtl_btn_main_select_insurance"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
