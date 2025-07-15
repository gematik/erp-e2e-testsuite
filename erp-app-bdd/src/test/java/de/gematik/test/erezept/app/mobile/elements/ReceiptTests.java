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

import static de.gematik.test.erezept.app.mobile.elements.Receipt.*;
import static org.junit.jupiter.api.Assertions.*;

import io.appium.java_client.AppiumBy;
import lombok.val;
import org.junit.jupiter.api.Test;

class ReceiptTests {
  @Test
  void reserveInTheFarmacyTest() {
    val element = RESERVE_IN_PHARMACY;
    assertTrue(element.getFullName().contains("Reserve in the pharmacy"));
    assertEquals(element.getIosLocator().get(), AppiumBy.accessibilityId("rdm_btn_delivery_tile"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void inputSearchTest() {
    val element = INPUT_SEARCH_BOX;
    assertTrue(element.getFullName().contains("Search box for the medicine"));
    assertEquals(element.getIosLocator().get(), AppiumBy.name("Nach Name oder Adresse suchen"));
  }

  @Test
  void selectMethodPickUpTest() {
    val element = SELECT_DELIVERY_METHOD_PICKUP;
    assertTrue(element.getFullName().contains("select the delivery method pickup in the pharmacy"));
    assertEquals(
        element.getIosLocator().get(), AppiumBy.accessibilityId("pha_detail_btn_location"));
  }

  @Test
  void redeemPharmacyPrescriptionTest() {
    val element = REDEEM_PHARMACY_PRESCRIPTION;
    assertTrue(element.getFullName().contains("redeem effectively the prescription"));
    assertEquals(element.getIosLocator().get(), AppiumBy.accessibilityId("pha_redeem_btn_redeem"));
  }

  @Test
  void redeemablePrescriptionCardButtonTest() {
    val element = REDEEMABLE_PRESCRIPTION_CARD_BUTTON;
    assertTrue(element.getFullName().contains("get the top redeemed prescription"));
    assertEquals(element.getIosLocator().get(), AppiumBy.xpath("//*[@label='Einlösbar']"));
  }

  @Test
  void TechnicalInformationTest() {
    val element = TECHNICAL_INFORMATION;
    assertTrue(element.getFullName().contains("Technische Informationen"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.accessibilityId("prsc_dtl_btn_technical_informations"));
  }

  @Test
  void PrescriptionStatusLabel() {
    val element = PRESCRIPTION_STATUS_LABEL;
    assertTrue(element.getFullName().contains("prescription status label"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.accessibilityId(
            "erx_detailed_prescription_name-erx_detailed_prescription_validity-erx_detailed_status"));
  }

  @Test
  void RedeemedPrescriptionStatusLabel() {
    val element = REDEEMED_PRESCRIPTION_STATUS_LABEL;
    assertTrue(element.getFullName().contains("redeemed prescription status label"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.accessibilityId(
            "erx_detailed_prescription_name-erx_detailed_prescription_validity-erx_detailed_status-erx_detailed_status"));
  }

  @Test
  void goBackToDetailsButtonTest() {
    val element = GOBACK_TO_DETAILS;
    assertTrue(element.getFullName().contains("from technical information go back to detail page"));
    assertEquals(element.getIosLocator().get(), AppiumBy.name("Detail"));
  }

  @Test
  void receiptDateOfIssueLabel() {
    val element = RECEIPT_DATE_OF_ISSUE_LABEL;
    assertTrue(element.getFullName().contains("Date of issue"));
    assertEquals(
        element.getIosLocator().get(), AppiumBy.accessibilityId("prsc_dtl_txt_authored_on"));
  }
}
