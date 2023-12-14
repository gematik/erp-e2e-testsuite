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
import lombok.val;
import org.junit.jupiter.api.Test;

class ProfileTests {

  @Test
  void checkBackButton() {
    val element = Profile.USER_KVNR;
    assertTrue(element.getFullName().contains("health insurance number"));
    assertFalse(element.name().toLowerCase().contains("button"));
    assertEquals(
        element.getIosLocator().get(),
        AppiumBy.accessibilityId("stg_txt_edit_profile_insurance_id"));
  }
}
