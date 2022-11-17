/*
 * Copyright (c) 2022 gematik GmbH
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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.exceptions.UnavailablePageElementLocatorException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import io.appium.java_client.AppiumBy;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;

class PageElementTest {

  @Test
  void shouldGetFullname() {
    val element = Onboarding.NEXT;
    assertTrue(element.getFullName().contains("Onboarding"));
    assertTrue(element.getFullName().contains("Next"));
  }

  @Test
  void shouldThrowOnUnsupportedPlatform() {
    val element = Onboarding.SKIP;
    assertThrows(
        UnsupportedPlatformException.class, () -> element.forPlatform(PlatformType.DESKTOP));
  }

  @Test
  void shouldGetAllElementsCorrectly() {
    // add here the implementing class of PageElement to be automatically covered
    val pageElementEnums =
        List.of(Onboarding.class, CardWall.class, Debug.class, Prescriptions.class, Settings.class);

    val pageElements =
        pageElementEnums.stream()
            .flatMap(pageEnum -> Arrays.stream(pageEnum.getEnumConstants()))
            .toList();
    pageElements.forEach(
        e -> {
          val element = (PageElement) e;
          checkForPlatform(element, PlatformType.ANDROID);
          checkForPlatform(element, PlatformType.IOS);
        });
  }

  @Test
  void shouldThrowCorrectly() {
    List.of(FakeElement.NULL_SUPPLIER, FakeElement.SUPPLIER_NULL)
        .forEach(
            element -> {
              checkForPlatform(element, PlatformType.ANDROID);
              checkForPlatform(element, PlatformType.IOS);
            });
  }

  private void checkForPlatform(PageElement element, PlatformType platformType) {
    if ((platformType == PlatformType.ANDROID
            && (element.getAndroidLocator() == null || element.getAndroidLocator().get() == null))
        || (platformType == PlatformType.IOS
            && (element.getIosLocator() == null || element.getIosLocator().get() == null))) {
      assertThrows(
          UnavailablePageElementLocatorException.class, () -> element.forPlatform(platformType));
    } else {
      assertNotNull(
          element.forPlatform(platformType),
          format("The Locator of {0} is expected to be not null", element.getFullName()));
    }
  }

  @Getter
  @RequiredArgsConstructor
  private enum FakeElement implements PageElement {
    OK("Okay", () -> By.tagName("fake/ok"), () -> AppiumBy.accessibilityId("bnt_ok")),
    NULL_SUPPLIER("NULL Supplier", () -> null, () -> null),
    SUPPLIER_NULL("NULL Supplier", null, null),
    ;

    private final String elementName;
    private final Supplier<By> androidLocator;
    private final Supplier<By> iosLocator;
  }
}
