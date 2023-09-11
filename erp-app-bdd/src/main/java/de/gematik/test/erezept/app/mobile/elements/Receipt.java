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

package de.gematik.test.erezept.app.mobile.elements;

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum Receipt implements PageElement {
  RESERVE_IN_PHARMACY(
      "Reserve in the pharmacy",
      () -> null,
      () -> AppiumBy.accessibilityId("rdm_btn_delivery_tile")),

  REDEEM_PRESCRIPTION_BTN(
      "Redeem the Prescription",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_btn_redeem_prescriptions")),

  INPUT_SEARCH_BOX(
      "Search box for the medicine",
      () -> null,
      () -> AppiumBy.name("Nach Name oder Adresse suchen")),

  SEARCH_BUTTON("search for pharmacy", () -> null, () -> AppiumBy.name("Search")),

  SELECT_PHARMACY("select the pharmacy", () -> null, () -> AppiumBy.name("PharmacyPlaceholder")),

  SELECT_DELIVERY_METHOD_PICKUP(
      "select the delivery method pickup in the pharmacy",
      () -> null,
      () -> AppiumBy.accessibilityId("pha_detail_btn_location")),

  REDEEM_PHARMACY_PRESCRIPTION(
      "redeem effectively the prescription",
      () -> null,
      () -> AppiumBy.accessibilityId("pha_redeem_btn_redeem")),

  SUCCESSFULLY_REDEEM_TO_START_PAGE(
      "after redeem the prescription go to the main page",
      () -> null,
      () -> AppiumBy.xpath("//*[@label='Zur Startseite']")),

  FULL_DAYS_VALID_RECEIPT(
    "28 days validity recipt",
    () -> null,
    () -> AppiumBy.xpath("//*[@label='Noch 28 Tage gültig']")),

  ARCHIVED_PRESCRIPTIONS_BTN(
    "Eingelöste Rezepte",
    () -> null,
    () -> AppiumBy.xpath("//*[@label='Eingelöste Rezepte']")),

  REDEEMABLE_PRESCRIPTION_CARD_BUTTON(
    "get the top redeemed prescription",
    () -> null,
    () -> AppiumBy.xpath("//*[@label='Einlösbar']")),

  TECHNICAL_INFORMATION(
    "Technische Informationen",
    () -> null,
    () -> AppiumBy.accessibilityId("prsc_dtl_btn_technical_informations")),

  PRESCRIPTION_STATUS_LABEL(
    "prescription status label",
    () -> null,
    () -> AppiumBy.accessibilityId("erx_detailed_prescription_name-erx_detailed_prescription_validity-erx_detailed_status")),

  REDEEMED_PRESCRIPTION_STATUS_LABEL(
    "redeemed prescription status label",
    () -> null,
    () -> AppiumBy.accessibilityId("erx_detailed_prescription_name-erx_detailed_prescription_validity-erx_detailed_status-erx_detailed_status")),
  TASKID(
    "Task Identifier",
    () -> null,
    () -> AppiumBy.accessibilityId("prsc_dtl_ti_task_id")),

  GOBACK_TO_DETAILS(
    "from technical information go back to detail page",
    () -> null,
    () -> AppiumBy.name("Detail")),

  RECEIPT_DATE_OF_ISSUE_LABEL(
    "Date of issue",
    () -> null,
    () -> AppiumBy.accessibilityId("prsc_dtl_txt_authored_on")),

  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
