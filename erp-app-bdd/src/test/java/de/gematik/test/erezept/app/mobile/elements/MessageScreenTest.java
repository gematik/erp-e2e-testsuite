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

import static org.junit.jupiter.api.Assertions.*;

import io.appium.java_client.AppiumBy;
import org.junit.jupiter.api.Test;

class MessageScreenTest {
  @Test
  void shouldHaveIOSLocators() {
    assertEquals(AppiumBy.name("ord_txt_list"), MessageScreen.MESSAGES_LIST.getIosLocator().get());
    assertEquals(
        AppiumBy.accessibilityId("ord_detail_txt_med_list"),
        MessageScreen.PRESCRIPTION_LIST.getIosLocator().get());
    assertEquals(
        AppiumBy.name("Nachrichten"), MessageScreen.BACK_TO_MESSAGE_SCREEN.getIosLocator().get());
    assertEquals(
        AppiumBy.name("E-Rezept App Team"),
        MessageScreen.E_REZEPT_APP_TEAM_TITLE.getIosLocator().get());
  }

  @Test
  void shouldNotHaveAndroidLocators() {
    assertNull(MessageScreen.MESSAGES_LIST.getAndroidLocator().get());
    assertNull(MessageScreen.PRESCRIPTION_LIST.getAndroidLocator().get());
    assertNull(MessageScreen.BACK_TO_MESSAGE_SCREEN.getAndroidLocator().get());
    assertNull(MessageScreen.E_REZEPT_APP_TEAM_TITLE.getAndroidLocator().get());
  }
}
