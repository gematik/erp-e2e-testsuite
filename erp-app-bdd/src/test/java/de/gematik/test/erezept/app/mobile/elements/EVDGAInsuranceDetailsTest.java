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
import java.text.MessageFormat;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EVDGAInsuranceDetailsTest {

  @ParameterizedTest
  @EnumSource(EVDGAInsuranceDetails.class)
  void shouldNotHaveAndroidLocators(EVDGAInsuranceDetails option) {
    val locator = assertDoesNotThrow(option::getAndroidLocator);
    assertNull(locator.get());
    assertNotNull(option.getElementName());
  }

  @ParameterizedTest
  @EnumSource(EVDGAInsuranceDetails.class)
  void shouldHaveIosLocators(EVDGAInsuranceDetails option) {
    val locator = assertDoesNotThrow(option::getIosLocator);
    assertNotNull(locator.get());
    assertNotNull(option.getElementName());
  }

  @Test
  void searchResultEntriesShouldNotHaveAndroidLocator() {
    val dummySearchEntry = EVDGAInsuranceDetails.forInsuranceEntry("TEST GKV-SV");
    assertNull(dummySearchEntry.getAndroidLocator().get());
  }

  @Test
  void searchResultEntriesShouldHaveIosLocator() {
    val dummySearchEntry = EVDGAInsuranceDetails.forInsuranceEntry("TEST GKV-SV");
    val locator = dummySearchEntry.getIosLocator();
    assertNotNull(locator.get());
    val expectedPredicate =
        AppiumBy.iOSNsPredicateString(
            MessageFormat.format(
                "name == \"{0}\" AND label == \"{0}\" AND type == \"XCUIElementTypeButton\"",
                "TEST GKV-SV"));
    assertEquals(expectedPredicate, locator.get());
  }

  @Test
  void shouldCreateInsuranceEntry() {
    val insuranceElement = EVDGAInsuranceDetails.forInsuranceEntry("TEST GKV-SV");
    assertNotNull(insuranceElement);
  }

  @Test
  void shouldHaveMatchingInsuranceName() {
    val insuranceElement = EVDGAInsuranceDetails.forInsuranceEntry("TEST GKV-SV");
    assertEquals("Insurance Search Entry for TEST GKV-SV", insuranceElement.getElementName());
  }
}
