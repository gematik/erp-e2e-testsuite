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

import static org.junit.jupiter.api.Assertions.*;

import io.appium.java_client.AppiumBy;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class BottomNavTests {

  @Test
  void checkPrescriptionButton() {
    val element = BottomNav.PRESCRIPTION_BUTTON;
    assertTrue(element.name().toLowerCase().contains("button"));
    assertNotNull(element.getIosLocator().get());
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkPharmacySearchButton() {
    var element = BottomNav.PHARMACY_SEARCH_BUTTON;
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(element.getIosLocator().get(), AppiumBy.name("Apothekensuche"));
  }

  @Test
  void checkPrescriptionOrdersButton() {
    var element = BottomNav.PRESCRIPTION_ORDERS_BUTTON;
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(element.getIosLocator().get(), AppiumBy.name("Bestellungen"));
  }

  @Test
  void checkSettingsButton() {
    val element = BottomNav.SETTINGS_BUTTON;
    assertTrue(element.getFullName().contains("Settings Menu Button"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(element.getIosLocator().get(), AppiumBy.name("Einstellungen"));
    assertEquals(element.getAndroidLocator().get(), By.tagName("BottomNavigation.SettingsButton"));
  }

  @Test
  void checkRedeemButton() {
    val element = Receipt.REDEEM_PRESCRIPTION_BTN;
    assertTrue(element.getFullName().contains("Redeem the Prescription"));
    assertTrue(element.name().toLowerCase().contains("redeem_prescription"));
    assertEquals(
        element.getIosLocator().get(), AppiumBy.accessibilityId("erx_btn_redeem_prescriptions"));
  }
}
