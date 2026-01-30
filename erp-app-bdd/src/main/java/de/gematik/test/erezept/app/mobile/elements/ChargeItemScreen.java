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
public enum ChargeItemScreen implements PageElement {
  THREE_DOT_MENU_BUTTON(
      "Three dot menu",
      () -> AppiumBy.accessibilityId("stg_btn_charge_item_list_navigation_bar_menu")),
  CONFIRM_GRANT_CONSENT_BUTTON(
      "Confirm to grant charge item consent", () -> AppiumBy.name("Empfangen")),
  REVOKE_CONSENT_MENU_ENTRY_BUTTON(
      "Menu entry to revoke charge item consent",
      () -> AppiumBy.accessibilityId("stg_txt_charge_item_list_menu_entry_deactivate")),
  CONFIRM_REVOKE_CONSENT_BUTTON(
      "Confirm to grant charge item consent", () -> AppiumBy.name("Deaktivieren")),
  CHARGE_ITEM_LIST_HEADER_TEXT_FIELD(
      "Header of the charge items list",
      () -> AppiumBy.accessibilityId("stg_btn_charge_item_list_container")),
  CHARGE_ITEM_LIST_ELEMENT(
      "Charge item list element", () -> AppiumBy.accessibilityId("stg_btn_charge_item_list_row")),
  THREE_DOT_MENU_DETAIL_SCREEN_BUTTON(
      "Three dot menu in charge item detail screen", () -> AppiumBy.name("Kontakt Optionen")),
  DELETE_CHARGE_ITEM_MENU_ENTRY_BUTTON(
      "Menu entry to delete the charge item", () -> AppiumBy.name("Löschen")),
  CONFIRM_DELETE_CHARGE_ITEM_BUTTON(
      "Confirm to delete the charge item", () -> AppiumBy.name("Löschen")),
  LEAVE_CHARGE_ITEM_DETAIL_SCREEN_BUTTON(
      "Navigate back to the list of charge items from the details of one specific",
      () -> AppiumBy.name("Apothekenrechnungen")),
  LEAVE_BUTTON("Leave button", () -> AppiumBy.name("Profil"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
