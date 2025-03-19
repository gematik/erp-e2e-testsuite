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

import lombok.val;
import org.junit.jupiter.api.Test;

class ProfileSelectorElementTest {

  @Test
  void shouldNotHaveAndroidLocator() {
    val pse = ProfileSelectorElement.forUser("alice").fromMainScreen();
    val locatorSupplier = pse.getAndroidLocator();
    assertNull(locatorSupplier.get());
  }

  @Test
  void shouldCreateLocatorForMainscreen() {
    val pse = ProfileSelectorElement.forUser("alice").fromMainScreen();
    val locator = pse.getIosLocator().get();
    assertTrue(pse.getElementName().contains("main screen"));
    assertTrue(locator.toString().contains("alice"));
    assertTrue(locator.toString().contains("pro_btn_selection_profile_row"));
  }

  @Test
  void shouldCreateLocatorForSettingsMenu() {
    val pse = ProfileSelectorElement.forUser("alice").fromSettingsMenu();
    val locator = pse.getIosLocator().get();
    assertTrue(pse.getElementName().contains("settings menu"));
    assertTrue(locator.toString().contains("alice"));
    assertFalse(locator.toString().contains("pro_btn_selection_profile_row"));
  }
}
