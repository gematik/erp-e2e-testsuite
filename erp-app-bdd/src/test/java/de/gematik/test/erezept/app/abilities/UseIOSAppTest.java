/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.app.abilities;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import de.gematik.test.erezept.app.mobile.elements.Utility;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import io.appium.java_client.ios.IOSDriver;
import io.cucumber.java.Scenario;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;

class UseIOSAppTest {

  @Test
  void shouldGetStaticValues() {
    val driver = mock(IOSDriver.class);

    val appiumConfig = new AppiumConfiguration();
    appiumConfig.setMaxWaitTimeout(10);
    appiumConfig.setPollingInterval(5);

    val driverAbility = new UseIOSApp(driver, appiumConfig);
    assertTrue(driverAbility.getDriverName().toLowerCase().contains("ios"));
    assertNotNull(driverAbility.toString());
    assertEquals(10, driverAbility.getMaxWaitTimeout());
    assertEquals(5, driverAbility.getPollingInterval());

    assertDoesNotThrow(() -> driverAbility.finish(mock(Scenario.class)));
  }

  @Test
  void shouldGetElement() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseIOSApp(driver, new AppiumConfiguration());
    assertEquals(webElement, driverAbility.getWebElement(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldTapOnElement() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(Onboarding.NEXT_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(webElement);
    when(driver.getPageSource()).thenReturn("");
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseIOSApp(driver, new AppiumConfiguration());
    driverAbility.tap(Onboarding.NEXT_BUTTON);
    verify(webElement).click();
  }

  @Test
  void shouldTapOnElementAfterTooltips() {
    val driver = mock(IOSDriver.class);
    val tooltips = mock(WebElement.class);
    val webElement = mock(WebElement.class);
    when(driver.getPageSource())
        .thenReturn(
            Utility.TOOLTIPS
                .forPlatform(PlatformType.IOS)
                .toString()) // first tooltip calling pagesource 2 times
        .thenReturn(
            Utility.TOOLTIPS
                .forPlatform(PlatformType.IOS)
                .toString()) // first tooltip calling pagesource 2 times
        .thenReturn(
            Utility.TOOLTIPS
                .forPlatform(PlatformType.IOS)
                .toString()) // second tooltip calling pagesource 2 times
        .thenReturn(
            Utility.TOOLTIPS
                .forPlatform(PlatformType.IOS)
                .toString()) // second tooltip calling pagesource 2 times
        .thenReturn("");
    when(driver.findElement(Utility.TOOLTIPS.forPlatform(PlatformType.IOS)))
        .thenReturn(tooltips)
        .thenReturn(tooltips)
        .thenReturn(null);
    when(driver.findElement(Onboarding.NEXT_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(webElement);
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseIOSApp(driver, new AppiumConfiguration());
    driverAbility.removeTooltips();
    driverAbility.tap(Onboarding.NEXT_BUTTON);

    verify(tooltips, times(2)).click();
    verify(webElement).click();
  }

  @Test
  void shouldTapMultipleOnElement() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(Onboarding.NEXT_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(webElement);
    when(driver.getPageSource()).thenReturn("");
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseIOSApp(driver, new AppiumConfiguration());
    driverAbility.tap(2, Onboarding.NEXT_BUTTON);
    verify(webElement, times(2)).click();
  }

  @Test
  void shouldGetElementsAsList() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseIOSApp(driver, new AppiumConfiguration());
    val elementList = driverAbility.getWebElements(Onboarding.NEXT_BUTTON);
    assertEquals(1, elementList.size());
  }

  @Test
  void shouldFailOnTooSlowElement() {
    val appiumConfig = new AppiumConfiguration();
    appiumConfig.setMaxWaitTimeout(10);
    appiumConfig.setPollingInterval(5);

    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.getPageSource()).thenReturn("page source");
    when(driver.findElement(any()))
        .thenThrow(new NoSuchElementException("Not found 1")) // throw on first call
        .thenThrow(new NoSuchElementException("Not found 2")) // throw on second call
        .thenThrow(new NoSuchElementException("Not found 3")) // throw on third call
        .thenReturn(webElement);

    val driverAbility = new UseIOSApp(driver, appiumConfig);
    assertThrows(
        NoSuchElementException.class, () -> driverAbility.getWebElement(Onboarding.NEXT_BUTTON));
  }
}
