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

package de.gematik.test.erezept.app.abilities;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.app.cfg.ScenarioFinalizer;
import de.gematik.test.erezept.app.exceptions.AppErrorException;
import de.gematik.test.erezept.app.mobile.ListPageElement;
import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.ErrorAlert;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import de.gematik.test.erezept.app.mobile.elements.Utility;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumFluentWait;
import io.appium.java_client.serverevents.CustomEvent;
import io.cucumber.java.Scenario;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

@Slf4j
public abstract class UseTheApp<T extends AppiumDriver> implements Ability {

  protected final T driver;
  @Getter @Setter private String currentUserProfile;
  @Getter private final PlatformType platformType;
  private final AppiumConfiguration appiumConfiguration;
  private final ScenarioFinalizer scenarioFinalizer;

  protected UseTheApp(
      T driver, PlatformType platformType, AppiumConfiguration appiumConfiguration) {
    this.driver = driver;
    this.platformType = platformType;
    this.appiumConfiguration = appiumConfiguration;
    this.scenarioFinalizer = new ScenarioFinalizer(this.appiumConfiguration.getUrl());
    log.info("{} Driver started with session ID {}", platformType, driver.getSessionId());
  }

  public String getDriverName() {
    return format(
        "Appium Driver for {0} connected to {1}", platformType, driver.getRemoteAddress());
  }

  protected int getMaxWaitTimeout() {
    return appiumConfiguration.getMaxWaitTimeout();
  }

  @SneakyThrows
  public void pauseApp() {
    Thread.sleep(1000);
  }

  @SneakyThrows
  public void longPauseApp() {
    Thread.sleep(60000);
  }

  protected int getPollingInterval() {
    return appiumConfiguration.getPollingInterval();
  }

  public void removeTooltips() {
    this.checkForErrorAlerts(driver.getPageSource());

    val changed = new AtomicBoolean(false);
    List.of(Utility.values())
        .forEach(
            utility -> {
              if (isDisplayed(utility.forPlatform(platformType))) {
                log.info("Found Utility Element {}", utility.getElementName());
                tap(getWebElement(utility.forPlatform(platformType)));
                changed.set(true);
              }
            });

    if (changed.get()) removeTooltips(); // recursively remove until nothing changes anymore
  }

  private boolean isDisplayed(By by) {
    boolean result;
    try {
      result = this.getWebElement(by) != null;
    } catch (NoSuchElementException e) {
      result = false;
    }

    return result;
  }

  @SneakyThrows
  protected void checkForErrorAlerts(String pageSource) {
    val alertElement = ErrorAlert.pageElement();
    val alertOpt = this.getOptionalWebElement(alertElement, pageSource);
    alertOpt.ifPresent(
        alert -> {
          val errorLines =
              alert.findElements(By.className("XCUIElementTypeStaticText")).stream()
                  .map(WebElement::getText)
                  .toList();
          val errorMessage = alertElement.extractErrorMessage(errorLines);
          throw new AppErrorException(errorMessage);
        });
  }

  public void tapIfDisplayed(int retries, PageElement pageElement) {
    while (retries-- > 0) {
      if (this.isDisplayed(pageElement)) {
        this.tap(pageElement);
        break;
      }
    }
  }

  public void tapIfDisplayed(PageElement pageElement) {
    tapIfDisplayed(1, pageElement);
  }

  public void tap(PageElement pageElement) {
    log.info("Tap ''{}'' ({})", pageElement.getFullName(), this.getLocator(pageElement));
    val element = this.getWebElement(pageElement);
    this.tap(element);
  }

  public void tap(PageElement pageElement, int index) {
    this.tap(ListPageElement.forElement(pageElement, index));
  }

  public void tap(ListPageElement listPageElement) {
    val pageElement = listPageElement.getPageElement();
    val index = listPageElement.getIndex();
    val elements = this.getWebElements(pageElement);

    if (index >= elements.size() || index < 0) {
      throw new NoSuchElementException(
          format(
              "Wanted to tap on the {0}th element of {1} but only {2} elements received from"
                  + " App-Driver",
              index, pageElement, elements.size()));
    }

    this.tap(elements.get(index));
  }

  public void tap(int times, PageElement pageElement) {
    assertTrue(times > 0, "Tap times must be greater than 0");
    for (int i = 0; i < times; i++) {
      this.tap(pageElement);
    }
  }

  public void tap(Point point) {
    val finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    val tap = new Sequence(finger, 0);
    tap.addAction(
        finger.createPointerMove(
            Duration.ofMillis(0), PointerInput.Origin.viewport(), point.x, point.y));
    tap.addAction(finger.createPointerDown(PointerInput.MouseButton.MIDDLE.asArg()));
    tap.addAction(finger.createPointerUp(PointerInput.MouseButton.MIDDLE.asArg()));
    log.info("Tap on Point at {}/{}", point.x, point.y);
    this.performGesture(tap);
  }

  public void tap(int times, Point point) {
    assertTrue(times > 0, "Tap times must be greater than 0");
    for (int i = 0; i < times; i++) {
      this.tap(point);
    }
  }

  private void tap(WebElement element) {
    element.click();
  }

  public void input(final String text, PageElement pageElement) {
    log.info("Type {} into {} ({})", text, pageElement.getFullName(), this.getLocator(pageElement));
    val element = this.getWebElement(pageElement);
    element.clear(); // clear the default text in the input
    element.sendKeys(text);
  }

  /**
   * As appium seem to lose some chars from time to time, passwords are typed char by char to slow
   * down the process and prevent losing characters.
   *
   * @param password is the password to be typed
   * @param pageElement is the input field where the password is typed in
   */
  public void inputPassword(final String password, PageElement pageElement) {
    log.info(
        "Type Password {} into {} ({})",
        password,
        pageElement.getFullName(),
        this.getLocator(pageElement));
    val element = this.getWebElement(pageElement);
    element.clear();
    for (val c : password.toCharArray()) {
      element.sendKeys(format("{0}", c));
    }
  }

  public void acceptAlert() {
    driver.switchTo().alert().accept();
  }

  public void swipe(SwipeDirection direction) {
    swipe(direction, 0.5f);
  }

  public void swipe(SwipeDirection direction, float factor) {
    swipe(direction, factor, 500);
  }

  public void swipe(SwipeDirection direction, float factor, int millis) {
    log.info("Swipe {} on the Screen", direction);
    var screenDimension = driver.manage().window().getSize();

    val swipeDimension =
        switch (direction) {
          case UP, DOWN -> {
            val height = screenDimension.height * factor;
            yield new Dimension(screenDimension.width, (int) height);
          }
          case LEFT, RIGHT -> {
            val width = screenDimension.width * factor;
            yield new Dimension((int) width, screenDimension.height);
          }
        };

    val swipe = direction.swipeOn(swipeDimension, millis);
    this.performGesture(swipe);
  }

  public void swipeIntoView(SwipeDirection direction, PageElement pageElement) {
    log.info("Swipe {} until element {} is found", direction, pageElement.getFullName());
    var swipeCounter = 20;
    boolean isDisplayed =
        this.getOptionalWebElement(pageElement).map(WebElement::isDisplayed).orElse(false);
    while (!isDisplayed && swipeCounter >= 0) {
      swipeCounter--;

      swipe(direction, 0.6F);
      isDisplayed =
          this.getOptionalWebElement(pageElement).map(WebElement::isDisplayed).orElse(false);
    }
  }

  protected final void waitUntil(ExpectedCondition<?> expected) {
    waitUntil(expected, getMaxWaitTimeout());
  }

  protected final void waitUntil(ExpectedCondition<?> expected, int millis) {
    val wait = this.getFluentWaitDriver();
    try {
      wait.withTimeout(Duration.ofMillis(millis)).until(expected);
    } catch (TimeoutException te) {
      this.checkForErrorAlerts(this.driver.getPageSource());
      throw te; // rethrow TimeoutException if no ErrorAlerts were found
    }
  }

  public final void waitUntilElementIsVisible(PageElement pageElement) {
    waitUntilElementIsVisible(pageElement, getMaxWaitTimeout());
  }

  public final void waitUntilElementIsVisible(PageElement pageElement, int maxWaitTimeout) {
    log.info(
        "Wait until element {} is visible with timeout of {} millis",
        pageElement.getFullName(),
        maxWaitTimeout);
    waitUntil(
        ExpectedConditions.presenceOfElementLocated(this.getLocator(pageElement)), maxWaitTimeout);
    waitUntil(
        ExpectedConditions.visibilityOfElementLocated(this.getLocator(pageElement)),
        maxWaitTimeout);
  }

  public final void waitUntilElementIsPresent(PageElement pageElement) {
    waitUntilElementIsPresent(pageElement, getMaxWaitTimeout());
  }

  public final void waitUntilElementIsPresent(PageElement pageElement, int maxWaitTimeout) {
    log.info(
        "Wait until element {} is present with timeout of {} millis",
        pageElement.getFullName(),
        maxWaitTimeout);
    waitUntil(
        ExpectedConditions.presenceOfAllElementsLocatedBy(this.getLocator(pageElement)),
        maxWaitTimeout);
  }

  public final void waitUntilElementIsSelected(PageElement pageElement) {
    log.info("Wait until element {} is selected", pageElement.getFullName());
    waitUntil(ExpectedConditions.elementToBeSelected(this.getLocator(pageElement)));
  }

  public final void waitUntilElementIsClickable(PageElement pageElement) {
    log.info("Wait until element {} is clickable", pageElement.getFullName());
    waitUntil(ExpectedConditions.elementToBeClickable(getWebElement(pageElement)));
  }

  public final boolean isDisplayed(PageElement pageElement) {
    val element = this.getOptionalWebElement(pageElement);
    return element.map(WebElement::isDisplayed).orElse(false);
  }

  public final boolean isEnabled(PageElement pageElement) {
    val element = this.getOptionalWebElement(pageElement);
    return element.map(we -> we.isEnabled() && we.isDisplayed()).orElse(false);
  }

  public boolean isPresent(PageElement pageElement) {
    return this.getOptionalWebElement(pageElement).isPresent();
  }

  public void performGesture(Sequence sequence) {
    driver.perform(Collections.singletonList(sequence));
  }

  public String getText(PageElement element) {
    return this.getWebElement(element).getText();
  }

  public final WebElement getWebElement(PageElement element) {
    return this.getWebElement(this.getLocator(element));
  }

  protected abstract WebElement getWebElement(By locator);

  protected abstract Optional<WebElement> getOptionalWebElement(PageElement pageelement);

  protected abstract Optional<WebElement> getOptionalWebElement(
      PageElement pageelement, String pageSource);

  public abstract List<WebElement> getWebElements(PageElement pageElement);

  public int getWebElementListLen(PageElement pageElement) {
    return this.getWebElements(pageElement).size();
  }

  protected final By getLocator(PageElement pageElement) {
    return pageElement.forPlatform(this.platformType);
  }

  protected FluentWait<T> getShortPollingFluentWaitDriver() {
    return getFluentWaitDriver(this.getMaxWaitTimeout() / 4, this.getPollingInterval());
  }

  protected FluentWait<T> getFluentWaitDriver() {
    return getFluentWaitDriver(this.getMaxWaitTimeout(), this.getPollingInterval());
  }

  protected FluentWait<T> getFluentWaitDriver(int timeout, int pollingInterval) {
    return new AppiumFluentWait<>(driver)
        .withTimeout(Duration.ofMillis(timeout))
        .ignoring(NoSuchElementException.class)
        .pollingEvery(Duration.ofMillis(pollingInterval));
  }

  public void logEvent(String message) {
    log.info(message);
    val event = new CustomEvent();
    event.setVendor("gematik");
    event.setEventName(message);
    driver.logEvent(event);
  }

  public void finish(Scenario scenario) {
    val scenarioId = scenario.getId();
    val scenarioName = scenario.getName();
    val scenarioStatus = scenario.getStatus();

    log.info("Scenario {} - {}: {}", scenarioName, scenarioId, scenarioStatus);

    val sendTestInfoStatus =
        scenarioFinalizer.sendTestInfo(
            driver.getSessionId().toString(), scenarioName, scenarioStatus.toString());

    log.info(
        "Sending test info for scenario: {} {}",
        scenarioName,
        sendTestInfoStatus ? "was successful" : "failed");
  }

  @Override
  public String toString() {
    return format("use the {0}", getDriverName());
  }
}
