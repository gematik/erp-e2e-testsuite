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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class DebugSettingsTests {

  @Test
  void checkLeaveButton() {
    val element = DebugSettings.LEAVE_BUTTON;
    assertTrue(element.getFullName().contains("Leave Debug Settings Menu Button"));
    assertTrue(element.name().toLowerCase().contains("button"));
    assertNull(element.getAndroidLocator().get());
    assertNotNull(element.getIosLocator().get());
  }

  @Test
  void checkEnvironmentSelector() {
    val element = DebugSettings.ENVIRONMENT_SELECTOR;
    assertTrue(element.getFullName().contains("Environment Selector"));
    assertNull(element.getAndroidLocator().get());
    assertNotNull(element.getIosLocator().get());
  }

  @ParameterizedTest
  @EnumSource(names = {"RU_ENVIRONMENT", "RU_DEV_ENVIRONMENT", "TU_ENVIRONMENT"})
  void checkRUEnvironmentSelector(DebugSettings element) {
    assertTrue(element.getFullName().contains("Environment Selector for the"));
    assertNull(element.getAndroidLocator().get());
    assertNotNull(element.getIosLocator().get());
  }
}
