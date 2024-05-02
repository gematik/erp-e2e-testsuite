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

package de.gematik.test.erezept.app.abilities;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.app.exceptions.AppErrorException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.ScrollDirection;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.*;
import de.gematik.test.erezept.app.mocker.WebElementMockFactory;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import io.appium.java_client.AppiumFluentWait;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import io.cucumber.java.Scenario;
import io.cucumber.java.Status;
import java.time.Duration;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;

class UseTheAppTest {

  private static AppiumConfiguration appiumConfig;

  @BeforeAll
  static void setup() {
    appiumConfig = new AppiumConfiguration();
  }

  @Test
  void shouldGiveLengthOfFoundElements() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElements(any())).thenReturn(List.of(webElement, webElement));

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertEquals(2, driverAbility.getWebElementListLen(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldTapOnElementByLabel() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    val elements = List.of(webElement);
    when(driver.findElement(any())).thenReturn(webElement);
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseIOSApp(driver, appiumConfig);

    when(driver.findElements(any(By.class))).thenReturn(elements);
    when(elements.get(0).getText()).thenReturn("label");

    driverAbility.tapByLabel(Onboarding.NEXT_BUTTON, "label");
    verify(webElement).click();
  }

  @Test
  void shouldTapByOrder() {
    val driver = mock(IOSDriver.class);
    val first = mock(WebElement.class);
    val second = mock(WebElement.class);
    when(second.isDisplayed()).thenReturn(true);
    when(second.isEnabled()).thenReturn(true);
    val elements = List.of(first, second);
    when(driver.findElements(any())).thenReturn(elements);

    val driverAbility = new UseIOSApp(driver, appiumConfig);
    driverAbility.tap(Onboarding.NEXT_BUTTON, 1);
    verify(first, times(0)).click();
    verify(second, times(1)).click();
  }

  @Test
  void shouldThrowByTappingExceedingOrder() {
    val driver = mock(IOSDriver.class);
    val first = mock(WebElement.class);
    val second = mock(WebElement.class);
    val elements = List.of(first, second);
    when(driver.findElements(any())).thenReturn(elements);

    val driverAbility = new UseIOSApp(driver, appiumConfig);
    assertThrows(NoSuchElementException.class, () -> driverAbility.tap(Onboarding.NEXT_BUTTON, 2));
    verify(first, times(0)).click();
    verify(second, times(0)).click();
  }

  @Test
  void shouldThrowByTappingNegativeOrder() {
    val driver = mock(IOSDriver.class);
    val first = mock(WebElement.class);
    val second = mock(WebElement.class);
    val elements = List.of(first, second);
    when(driver.findElements(any())).thenReturn(elements);

    val driverAbility = new UseIOSApp(driver, appiumConfig);
    assertThrows(NoSuchElementException.class, () -> driverAbility.tap(Onboarding.NEXT_BUTTON, -1));
    verify(first, times(0)).click();
    verify(second, times(0)).click();
  }

  @Test
  void shouldThrowOnTappingDisabledElement() {
    val driver = mock(IOSDriver.class);
    val mockElement = WebElementMockFactory.getDisplayableMockElement(true, false);
    doThrow(ElementClickInterceptedException.class).when(mockElement).click();

    val pageElement = Onboarding.NEXT_BUTTON;

    when(driver.findElement(pageElement.forPlatform(PlatformType.IOS))).thenReturn(mockElement);
    when(driver.getPageSource()).thenReturn("");
    val driverAbility = new UseIOSApp(driver, appiumConfig);
    assertThrows(ElementNotInteractableException.class, () -> driverAbility.tap(pageElement));
  }

  @Test
  void shouldTapOnPoint() {
    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    val point = new Point(200, 300);
    driverAbility.tap(point);
    verify(driver, times(1)).perform(any());
  }

  @Test
  void shouldDoubleTapOnPoint() {
    val driver = mock(AndroidDriver.class);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    val point = new Point(200, 300);
    driverAbility.tap(2, point);
    verify(driver, times(2)).perform(any());
  }

  @Test
  void shouldCheckElementPresent() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertTrue(driverAbility.isPresent(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldCheckElementEnabled() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);
    when(webElement.isEnabled()).thenReturn(true);
    when(webElement.isDisplayed()).thenReturn(true);
    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    val isEnabled = driverAbility.isEnabled(Onboarding.NEXT_BUTTON);
    assertTrue(isEnabled);
  }

  @Test
  void shouldCheckIsElementPresentByXpath() {
    val driver = mock(IOSDriver.class);
    val webElement = mock(WebElement.class);
    when(driver.findElement(any())).thenReturn(webElement);
    when(driver.getPageSource()).thenReturn("XPATH");
    when(webElement.isEnabled()).thenReturn(true);
    val driverAbility = new UseIOSApp(driver, appiumConfig);

    val element = driverAbility.isPresent(XpathPageElement.xPathPageElement("@name='XPATH'"));
    assertTrue(element);
  }

  @Test
  void shouldShowDisplayed() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(driver.findElement(any())).thenReturn(webElement);

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertTrue(driverAbility.isDisplayed(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldShowNotDisplayed() {
    val driver = mock(AndroidDriver.class);
    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(false); // element found but not displayed
    when(driver.findElements(any())).thenReturn(List.of(webElement));

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    assertFalse(driverAbility.isDisplayed(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldShowNotDisplayedWhenNothingFound() {
    val driver = mock(AndroidDriver.class);
    when(driver.findElements(any())).thenReturn(List.of()); // no elements found

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
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

    val driverAbility = new UseAndroidApp(driver, appiumConfig);

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

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
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

    val driverAbility = new UseAndroidApp(driver, appiumConfig);
    driverAbility.inputPassword(password, Onboarding.PASSWORD_INPUT_FIELD);
    verify(webElement).clear();
    verify(webElement).sendKeys("1"); // char by char
    verify(webElement).sendKeys("2");
    verify(webElement).sendKeys("3");
  }

  @Test
  void shouldNotTapOnElementIfNotPresent() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;

    when(driver.findElements(any())).thenReturn(List.of());
    when(driver.getPageSource()).thenReturn("");

    driverAbility.tapIfDisplayed(pageElement);
    verify(driverAbility, never()).tap(pageElement);
  }

  @Test
  void shouldTapIfPresent() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;

    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(webElement.isEnabled()).thenReturn(true);
    when(driver.findElement(pageElement.forPlatform(PlatformType.IOS))).thenReturn(webElement);
    when(driver.getPageSource()).thenReturn(pageElement.forPlatform(PlatformType.IOS).toString());

    driverAbility.tapIfDisplayed(pageElement);
    verify(driverAbility, times(1)).tap(pageElement);
  }

  @Test
  void shouldWaitUntilElementIsVisibleAndEnabled() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;
    val locator = pageElement.forPlatform(driverAbility.getPlatformType());

    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(webElement.isEnabled()).thenReturn(true);
    when(driver.findElement(locator))
        .thenReturn(null) // 1st call for isVisible
        .thenReturn(webElement) // 2nd call for isVisible
        .thenReturn(null) // 1st call for isClickable
        .thenReturn(webElement); // 2nd call for isClickable
    when(driver.getPageSource()).thenReturn("dummy page source");
    val fluentDriver =
        new AppiumFluentWait<>(driver)
            .withTimeout(Duration.ofMillis(10))
            .ignoring(NoSuchElementException.class)
            .pollingEvery(Duration.ofMillis(1));
    when(driverAbility.getFluentWaitDriver()).thenReturn(fluentDriver);

    driverAbility.waitUntilElementIsVisible(pageElement);
    driverAbility.waitUntilElementIsClickable(pageElement);
    verify(driverAbility, times(1)).waitUntilElementIsVisible(pageElement);
    verify(driverAbility, times(1)).waitUntilElementIsClickable(pageElement);

    // two for isVisible and two for isClickable: each calling twice findElement
    verify(driver, times(4)).findElement(locator);
  }

  @Test
  void shouldWaitUntilElementIsPresent() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;
    val locator = pageElement.forPlatform(driverAbility.getPlatformType());

    val webElement = mock(WebElement.class);
    when(webElement.isDisplayed()).thenReturn(true);
    when(webElement.isEnabled()).thenReturn(true);
    when(driver.findElements(locator))
        .thenReturn(List.of()) // first time no match!
        .thenReturn(List.of(webElement));
    val fluentDriver =
        new AppiumFluentWait<>(driver)
            .withTimeout(Duration.ofMillis(10))
            .ignoring(NoSuchElementException.class)
            .pollingEvery(Duration.ofMillis(1));
    when(driverAbility.getFluentWaitDriver()).thenReturn(fluentDriver);

    driverAbility.waitUntilElementIsPresent(pageElement);
    verify(driverAbility, times(1)).waitUntilElementIsPresent(pageElement);
    verify(driver, times(2)).findElements(locator);
  }

  @Test
  void shouldWaitUntilElementIsSelected() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;
    val locator = pageElement.forPlatform(driverAbility.getPlatformType());

    val webElement = mock(WebElement.class);
    when(webElement.isSelected()).thenReturn(false).thenReturn(true);
    when(driver.findElement(locator))
        .thenThrow(NoSuchElementException.class) // not found on the first try
        .thenReturn(webElement);
    val fluentDriver =
        new AppiumFluentWait<>(driver)
            .withTimeout(Duration.ofMillis(10))
            .ignoring(NoSuchElementException.class)
            .pollingEvery(Duration.ofMillis(1));
    when(driverAbility.getFluentWaitDriver()).thenReturn(fluentDriver);

    driverAbility.waitUntilElementIsSelected(pageElement);
    verify(driverAbility, times(1)).waitUntilElementIsSelected(pageElement);
    verify(driver, times(3)).findElement(locator);
  }

  @Test
  void shouldRethrowOnWaitUntilTimeout() {
    val driver = mock(IOSDriver.class);
    val driverAbility = spy(new UseIOSApp(driver, appiumConfig));
    val pageElement = Onboarding.PASSWORD_INPUT_FIELD;
    val locator = pageElement.forPlatform(driverAbility.getPlatformType());

    when(driver.getPageSource()).thenReturn("dummy page source");
    when(driver.findElement(locator)).thenThrow(NoSuchElementException.class);
    val fluentDriver =
        new AppiumFluentWait<>(driver)
            .withTimeout(Duration.ofMillis(10))
            .ignoring(NoSuchElementException.class)
            .pollingEvery(Duration.ofMillis(1));
    when(driverAbility.getFluentWaitDriver()).thenReturn(fluentDriver);

    assertThrows(
        TimeoutException.class, () -> driverAbility.waitUntilElementIsSelected(pageElement));
    verify(driverAbility, times(1)).waitUntilElementIsSelected(pageElement);
    verify(driver, atMost(10)).findElement(locator);
  }

  @Test
  void checkIfTapWithTooltipAndDrawerIsWorkingForIOS() {
    val driver = mock(IOSDriver.class);
    UseIOSApp useIOSApp = new UseIOSApp(driver, appiumConfig);

    val loginButton = WebElementMockFactory.getDisplayableMockElement(true, true);
    val bottomDrawerDecline = WebElementMockFactory.getDisplayableMockElement(true, true);
    val tooltip = WebElementMockFactory.getDisplayableMockElement(true, true);

    when(driver.findElement(Mainscreen.LOGIN_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(loginButton);
    when(driver.findElement(Utility.DECLINE_LOGIN.forPlatform(PlatformType.IOS)))
        .thenReturn(bottomDrawerDecline);
    when(driver.getPageSource())
        .thenReturn(Utility.TOOLTIPS.forPlatform(PlatformType.IOS).toString())
        .thenReturn("");
    when(driver.findElement(Utility.TOOLTIPS.forPlatform(PlatformType.IOS)))
        .thenReturn(tooltip)
        .thenReturn(null); // show tooltips only one time

    useIOSApp.tap(Mainscreen.LOGIN_BUTTON);
  }

  @Test
  void shouldScrollUntilFound() {
    val driver = mock(IOSDriver.class);
    val useIOSApp = spy(new UseIOSApp(driver, appiumConfig));

    val loginButton = WebElementMockFactory.getDisplayableMockElement(true, true);
    when(driver.findElement(Mainscreen.LOGIN_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(loginButton);
    when(driver.getPageSource()).thenReturn("").thenReturn("").thenReturn("erx_btn_login");

    useIOSApp.scrollIntoView(ScrollDirection.DOWN, Mainscreen.LOGIN_BUTTON);
    verify(useIOSApp, times(3)).getOptionalWebElement(Mainscreen.LOGIN_BUTTON);
    verify(driver, times(2)).executeScript(eq("mobile:scroll"), any());
  }

  @Test
  void shouldNotScrollWhenFoundDirectly() {
    val driver = mock(IOSDriver.class);
    val useIOSApp = spy(new UseIOSApp(driver, appiumConfig));

    val loginButton = WebElementMockFactory.getDisplayableMockElement(true, true);
    when(driver.findElement(Mainscreen.LOGIN_BUTTON.forPlatform(PlatformType.IOS)))
        .thenReturn(loginButton);
    when(driver.getPageSource()).thenReturn("erx_btn_login");

    useIOSApp.scrollIntoView(ScrollDirection.DOWN, Mainscreen.LOGIN_BUTTON);
    verify(useIOSApp, times(1)).getOptionalWebElement(Mainscreen.LOGIN_BUTTON);
    verify(driver, times(0)).executeScript(eq("mobile:scroll"), any());
  }

  @Test
  void shouldDetectErrorAlert() {
    val driver = mock(IOSDriver.class);
    val app = new UseIOSApp(driver, appiumConfig);
    when(driver.getPageSource()).thenReturn(format("type=\"XCUIElementTypeAlert\" name=\"Fehler"));

    val mockElement = mock(WebElement.class);
    when(driver.findElement(ErrorAlert.pageElement().getIosLocator().get()))
        .thenReturn(mockElement);

    assertThrows(AppErrorException.class, () -> app.tap(Onboarding.NEXT_BUTTON));
  }

  @Test
  void shouldAcceptAlert() {
    val driver = mock(IOSDriver.class);
    val app = new UseIOSApp(driver, appiumConfig);

    val targetLocator = mock(WebDriver.TargetLocator.class);
    val alert = mock(Alert.class);
    when(driver.switchTo()).thenReturn(targetLocator);
    when(targetLocator.alert()).thenReturn(alert);

    assertDoesNotThrow(app::acceptAlert);

    verify(targetLocator, times(1)).alert();
    verify(alert, times(1)).accept();
  }

  @Test
  void shouldReportScenarioStatus() {
    val driver = mock(IOSDriver.class);
    val app = new UseIOSApp(driver, appiumConfig);
    val scenario = mock(Scenario.class);
    when(scenario.getStatus()).thenReturn(Status.PASSED);
    assertDoesNotThrow(() -> app.finish(scenario));
    verify(driver, times(1))
        .executeScript(
            (String)
                argThat(
                    argument -> ((String) argument).startsWith("seetest:client.setReportStatus")));
  }
}
