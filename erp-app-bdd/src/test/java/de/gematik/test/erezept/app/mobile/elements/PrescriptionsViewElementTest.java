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

import de.gematik.test.erezept.app.exceptions.UnavailablePageElementLocatorException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumBy;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrescriptionsViewElementTest {

  @Test
  void shouldExtractOnlyPrescriptionName() {
    val pe = PrescriptionsViewElement.named("Ibuprofen");
    assertEquals("Ibuprofen", pe.extractSourceLabel(PlatformType.IOS));
    assertEquals("Prescription Ibuprofen", pe.getElementName());
  }

  @Test
  void shouldExtractOnlyLocator() {
    val pe = PrescriptionsViewElement.withoutName();
    assertDoesNotThrow(() -> pe.extractSourceLabel(PlatformType.IOS));
    assertEquals("any Prescription", pe.getElementName());
  }

  @Test
  void shouldNotHaveAndroidImplementation() {
    val pe = PrescriptionsViewElement.named("Ibuprofen");
    assertThrows(
        UnavailablePageElementLocatorException.class, () -> pe.forPlatform(PlatformType.ANDROID));
  }

  @Test
  void shouldHaveIosImplementation() {
    val pe = PrescriptionsViewElement.named("Ibuprofen");
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(AppiumBy.ByIosNsPredicate.class, by.getClass());
  }

  @Test
  void shouldHaveIosImplementationWithPrescriptionName() {
    val pe = PrescriptionsViewElement.named("Ibuprofen");
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(AppiumBy.ByIosNsPredicate.class, by.getClass());
    assertTrue(by.toString().contains("Ibuprofen"));
  }

  @Test
  void shouldHaveIosImplementationWithoutPrescriptionName() {
    val pe = PrescriptionsViewElement.withoutName();
    val by = pe.forPlatform(PlatformType.IOS);
    assertNotNull(by);
    assertEquals(AppiumBy.ByIosNsPredicate.class, by.getClass());
  }
}
