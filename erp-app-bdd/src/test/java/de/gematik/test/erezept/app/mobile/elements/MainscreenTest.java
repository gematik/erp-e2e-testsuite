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

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;

@Slf4j
class MainscreenTest {
  @Test
  void shouldContainProfileNameInProfileButtonElement() {
    val profileButtonElement = Mainscreen.forProfile("Alice");
    val androidLocator = assertDoesNotThrow(profileButtonElement::getAndroidLocator);
    assertNull(androidLocator.get());

    val iosLocatorSupplier = assertDoesNotThrow(profileButtonElement::getIosLocator);
    val iosLocator = assertDoesNotThrow(iosLocatorSupplier::get);
    assertTrue(iosLocator.toString().contains("Alice"));
    assertTrue(profileButtonElement.getElementName().contains("Alice"));
  }
}
