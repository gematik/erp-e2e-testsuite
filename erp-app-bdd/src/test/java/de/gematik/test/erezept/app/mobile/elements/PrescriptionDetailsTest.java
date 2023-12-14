/*
 * Copyright 2023 gematik GmbH
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
import java.util.Arrays;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class PrescriptionDetailsTest {

  @Test
  void checkDeleteButton() {
    val element = PrescriptionDetails.PRESCRIPTION_CANNOT_BE_DELETED_INFO;
    assertTrue(element.getFullName().contains("Alert information"));
    assertEquals(
        element.getIosLocator().get(),
        By.xpath(
            "(//XCUIElementTypeAlert[@name='Das Rezept ist gerade in Bearbeitung durch eine Apotheke und kann nicht gelöscht werden.'])"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkNewDeleteButton() {
    val element = PrescriptionDetails.DELETE_BUTTON_TOOLBAR;
    assertTrue(element.getFullName().contains("Delete Prescription tool bar item"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(
        element.getIosLocator().get(), AppiumBy.accessibilityId("prsc_dtl_btn_toolbar_item"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkToolBarDeleteButton() {
    val element = PrescriptionDetails.DELETE_BUTTON_TOOLBAR_ITEM;
    assertTrue(element.getFullName().contains("Delete Prescription button"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(element.getIosLocator().get(), AppiumBy.xpath("//*[@label='Löschen']"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkDeletePrescriptionItemButton() {
    val element = PrescriptionDetails.DELETE_PRESCRIPTION_ITEM_BUTTON;
    assertTrue(element.getFullName().contains("Delete Prescription button"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.xpath("//*[@name='Horizontaler Rollbalken, 1 Seite']"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkBackButton() {
    val element = PrescriptionDetails.LEAVE_DETAILS_BUTTON;
    assertTrue(element.getFullName().contains("Leave Prescription Details"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.xpath("(//XCUIElementTypeButton[@name='Rezepte'])[1]"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void shouldNotHaveNullLocatorForIos() {
    Arrays.stream(PrescriptionDetails.values())
        .map(PrescriptionDetails::getIosLocator)
        .forEach(element -> assertNotNull(element.get()));
  }

  @Test
  void shouldNotHaveLocatorForAndroid() {
    Arrays.stream(PrescriptionDetails.values())
        .map(PrescriptionDetails::getAndroidLocator)
        .forEach(element -> assertNull(element.get()));
  }
}
