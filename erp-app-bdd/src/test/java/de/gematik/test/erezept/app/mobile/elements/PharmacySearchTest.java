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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class PharmacySearchTest {
  @ParameterizedTest
  @EnumSource(PharmacySearch.class)
  void shouldNotHaveAndroidLocators(PharmacySearch option) {
    val locator = assertDoesNotThrow(option::getAndroidLocator);
    assertNull(locator.get());
    assertNotNull(option.getElementName());
  }

  @ParameterizedTest
  @EnumSource(PharmacySearch.class)
  void shouldHaveIosLocators(PharmacySearch option) {
    val locator = assertDoesNotThrow(option::getIosLocator);
    assertNotNull(locator.get());
    assertNotNull(option.getElementName());
  }

  @Test
  void shouldContainPharmacyNameInSearchResultEntryElement() {
    val pe = PharmacySearch.forPharmacyEntry("Apotheke123");
    val androidLocator = assertDoesNotThrow(pe::getAndroidLocator);
    assertNull(androidLocator.get());

    val iosLocatorSupplier = assertDoesNotThrow(pe::getIosLocator);
    val iosLocator = assertDoesNotThrow(iosLocatorSupplier::get);
    assertTrue(iosLocator.toString().contains("Apotheke123"));
    assertTrue(pe.getElementName().contains("Apotheke123"));
  }
}
