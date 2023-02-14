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

package de.gematik.test.erezept.app.abilities;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.app.cfg.AppConfiguration;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.Onboarding;
import io.appium.java_client.android.AndroidDriver;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

class UseTheAppTest {

  private static AppConfiguration config;

  @BeforeAll
  static void setup() {
    config = new AppConfiguration();
    config.setAppFile("test-app-file");
    config.setPlatform("Android");
    config.setUseVirtualeGK(true);
    config.setPackageName("de.gematik.erezept");
    config.setMaxWaitTimeout(500);
    config.setPollingInterval(5);
  }

  @Test
  void shouldGiveLengthOfFoundElements() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElements(any())).thenReturn(List.of(webElement, webElement));

    val driverAbility = new UseAndroidApp(driver, config);
    assertEquals(2, driverAbility.getWebElementListLen(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldTapOnElement() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    driverAbility.tap(Onboarding.NEXT_BUTTON);
    verify(webElement).click();
  }

  @Test
  void shouldTapMultipleOnElement() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    driverAbility.tap(2, Onboarding.NEXT_BUTTON);
    verify(webElement, times(2)).click();
  }

  @Test
  void shouldTapOnByLocator() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, config);
    driverAbility.tap(By.ById.name("identifier"));
    verify(webElement).click();
  }

  @Test
  void shouldTapOnPoint() {
    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, config);
    val point = new Point(200, 300);
    driverAbility.tap(point);
    verify(driver, times(1)).perform(any());
  }

  @Test
  void shouldDoubleTapOnPoint() {
    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, config);
    val point = new Point(200, 300);
    driverAbility.tap(2, point);
    verify(driver, times(2)).perform(any());
  }

  @Test
  void shouldShowDisplayed() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseAndroidApp(driver, config);
    assertTrue(driverAbility.isDisplayed(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldShowNotDisplayed() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(false); // element found but not displayed
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseAndroidApp(driver, config);
    assertFalse(driverAbility.isDisplayed(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldShowNotDisplayedWhenNothingFound() {
    val driver = mock(AndroidDriver.class);
    when(driver.findElements(any())).thenReturn(List.of()); // no elements found

    val driverAbility = new UseAndroidApp(driver, config);
    assertFalse(driverAbility.isDisplayed(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldSwipe() {
    val driver = mock(AndroidDriver.class);
    val options = mock(WebDriver.Options.class);
    val window = mock(WebDriver.Window.class);
    when(driver.manage()).thenReturn(options);
    when(options.window()).thenReturn(window);
    when(window.getSize()).thenReturn(new Dimension(300, 400));

    val driverAbility = new UseAndroidApp(driver, config);

    // swipe once in each direction
    val directions = SwipeDirection.values();
    for (var i = 1; i <= directions.length; i++) {
      val direction = directions[i - 1];
      driverAbility.swipe(direction);
      verify(driver, times(i)).perform(any());
    }
  }

  @Test
  void shouldInputText() {
    val inputText = "Hello World";
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);
    when(webElement.getText()).thenReturn(inputText);

    val driverAbility = new UseAndroidApp(driver, config);
    driverAbility.input(inputText, Onboarding.USER_PROFILE_FIELD);
    verify(webElement).sendKeys(inputText);
  }

  @Test
  void shouldInputPassword() {
    val password = "123";
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);
    when(webElement.getText()).thenReturn(password);

    val driverAbility = new UseAndroidApp(driver, config);
    driverAbility.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    verify(webElement).clear();
    verify(webElement).sendKeys("1"); // char by char
    verify(webElement).sendKeys("2");
    verify(webElement).sendKeys("3");
  }
}
