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

import de.gematik.test.erezept.app.mobile.PlatformType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.openqa.selenium.By;

import java.util.function.Supplier;


class XpathElementTests {
  @Mock
  private By mockBy;

  @Test
  void shouldGetPageElementTest() {
    val element = mock(XpathPageElement.class);
    try (MockedStatic<By> mockedStatic = mockStatic(By.class)) {
      mockedStatic.when(() -> By.xpath("xpath")).thenReturn(mockBy);

      String xpathPageElement = XpathPageElement.xPathPageElement("xpath").getPage();

      assertEquals("dynamic element",xpathPageElement);
    }
  }

  @Test
  void shouldGetPageElementElementNameTest() {
    val element = mock(XpathPageElement.class);
    try (MockedStatic<By> mockedStatic = mockStatic(By.class)) {
      mockedStatic.when(() -> By.xpath("xpath")).thenReturn(mockBy);

      String xpathPageElement = XpathPageElement.xPathPageElement("xpath").getElementName();

      assertEquals("dynamic element",xpathPageElement);
    }
  }

  @Test
  void shouldGetPageElementFullNameTest() {
    val element = mock(XpathPageElement.class);
    try (MockedStatic<By> mockedStatic = mockStatic(By.class)) {
      mockedStatic.when(() -> By.xpath("xpath")).thenReturn(mockBy);

      String xpathPageElement = XpathPageElement.xPathPageElement("xpath").getFullName();

      assertEquals("dynamic xpath element", xpathPageElement);
    }
  }

  @Test
  void shouldGetPageElementForPlatformTest() {
    val iosLocator = XpathPageElement.xPathPageElement("dd").forPlatform(PlatformType.IOS);
     assertEquals("By.xpath: dd", iosLocator.toString());
  }

  @Test
  void shouldGetPageElementgetIosLocatorTest() {
    Supplier<By> iosLocator = XpathPageElement.xPathPageElement("dd").getIosLocator();
    assertEquals("By.xpath: dd", iosLocator.get().toString());
    }

  @Test
  void shouldGetPageElementgetAndroidLocatorTest() {
    Supplier<By> androidLocator = XpathPageElement.xPathPageElement("dd").getAndroidLocator();
    assertEquals("By.xpath: dd", androidLocator.get().toString());
  }
}
