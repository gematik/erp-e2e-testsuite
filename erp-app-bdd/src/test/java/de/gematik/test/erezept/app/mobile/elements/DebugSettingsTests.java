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

import static org.junit.jupiter.api.Assertions.*;

import io.appium.java_client.AppiumBy;
import lombok.val;
import org.junit.jupiter.api.Test;

class DebugSettingsTests {

  @Test
  void checkLeaveButton() {
    val element = DebugSettings.LEAVE_BUTTON;
    assertTrue(element.getFullName().contains("Leave Debug Settings Menu Button"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertEquals(element.getIosLocator().get(), AppiumBy.xpath("//*[@label='Einstellungen']"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkEnvironmentSelector() {
    val element = DebugSettings.ENVIRONMENT_SELECTOR;
    assertTrue(element.getFullName().contains("Environment Selector"));
    assertEquals(element.getIosLocator().get(), AppiumBy.xpath("//*[@label='Environment']"));
    assertNull(element.getAndroidLocator().get());
  }

  @Test
  void checkRUEnvironmentSelector() {
    val element = DebugSettings.RU_ENVIRONMENT;
    assertTrue(element.getFullName().contains("Environment Selector for the RU"));
    assertEquals(element.getIosLocator().get(), AppiumBy.xpath("//*[@label='RU']"));
    assertNull(element.getAndroidLocator().get());
  }
}
