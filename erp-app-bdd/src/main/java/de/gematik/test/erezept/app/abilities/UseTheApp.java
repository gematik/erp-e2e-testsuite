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

package de.gematik.test.erezept.app.abilities;

import static java.text.MessageFormat.format;
import static org.assertj.core.api.Assertions.assertThat;

import de.gematik.test.erezept.app.cfg.AppConfiguration;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import io.appium.java_client.AppiumDriver;
import io.appium.java_client.AppiumFluentWait;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Ability;
import net.serenitybdd.screenplay.HasTeardown;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.FluentWait;

@Slf4j
public abstract class UseTheApp<T extends AppiumDriver> implements Ability, HasTeardown {

  protected T driver;
  @Getter private final PlatformType platformType;
  private final AppConfiguration appConfiguration;

  protected UseTheApp(T driver, PlatformType platformType, AppConfiguration appConfiguration) {
    this.driver = driver;
    this.platformType = platformType;
    this.appConfiguration = appConfiguration;
  }

  public String getDriverName() {
    return format(
        "Appium Driver for {0} connected to {1}", platformType, driver.getRemoteAddress());
  }

  protected int getMaxWaitTimeout() {
    return appConfiguration.getMaxWaitTimeout();
  }

  protected int getPollingInterval() {
    return appConfiguration.getPollingInterval();
  }

  public boolean useVirtualeGK() {
    return appConfiguration.isUseVirtualeGK();
  }

  public void tap(PageElement pageElement) {
    this.tap(pageElement.forPlatform(platformType));
  }

  public void tap(int times, PageElement pageElement) {
    assertThat(times).isGreaterThan(0);
    for (int i = 0; i < times; i++) {
      this.tap(pageElement);
    }
  }

  public void tap(Point point) {
    PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
    val tap = new Sequence(finger, 0);
    tap.addAction(
        finger.createPointerMove(
            Duration.ofMillis(0), PointerInput.Origin.viewport(), point.x, point.y));
    tap.addAction(finger.createPointerDown(PointerInput.MouseButton.MIDDLE.asArg()));
    tap.addAction(finger.createPointerUp(PointerInput.MouseButton.MIDDLE.asArg()));
    this.performGesture(tap);
  }

  public void tap(int times, Point point) {
    assertThat(times).isGreaterThan(0);
    for (int i = 0; i < times; i++) {
      this.tap(point);
    }
  }

  protected void tap(WebElement element) {
    log.info(format("Tap on element {0}", element));
    element.click();
  }

  protected void tap(By locator) {
    val element = this.getWebElement(locator);
    this.tap(element);
  }

  public void input(final String text, PageElement pageElement) {
    val element = this.getWebElement(pageElement);
    log.info(format("Type {0} into {1} ({2})", text, pageElement.getFullName(), element));
    element.clear(); // clear the default text in the input
    element.sendKeys(text);

    // refetch the element and check if appium sent the text correctly
    val input = this.getWebElement(pageElement).getText();
    assertThat(input).isEqualTo(text);
  }

  /**
   * As appium seem to lose some chars from time to time, passwords are typed char by char to slow
   * down the process and prevent losing characters.
   *
   * @param password is the password to be typed
   * @param pageElement is the input field where the password is typed in
   */
  public void inputPassword(final String password, PageElement pageElement) {
    val element = this.getWebElement(pageElement);
    log.info(
        format("Type Password {0} into {1} ({2})", password, pageElement.getFullName(), element));
    element.clear();
    for (val c : password.toCharArray()) {
      element.sendKeys(format("{0}", c));
    }
  }

  public void swipe(SwipeDirection direction) {
    swipe(direction, 1.0f);
  }

  public void swipe(SwipeDirection direction, float factor) {
    swipe(direction, factor, 100);
  }

  public void swipe(SwipeDirection direction, float factor, int millis) {
    log.info(format("Swipe {0} on the Screen", direction));
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

  public final void waitUntil(ExpectedCondition<Boolean> expected) {
    val wait = this.getFluentWaitDriver();
    wait.until(expected);
  }

  public final boolean isDisplayed(PageElement pageElement) {
    return isDisplayed(pageElement.forPlatform(this.platformType));
  }

  protected final boolean isDisplayed(By locator) {
    val elements = this.driver.findElements(locator);
    return elements.stream().map(WebElement::isDisplayed).findFirst().orElse(false);
  }

  public void performGesture(Sequence sequence) {
    driver.perform(Collections.singletonList(sequence));
  }

  public WebElement getWebElement(PageElement element) {
    return getWebElement(element.forPlatform(this.platformType));
  }

  protected abstract WebElement getWebElement(By locator);

  public abstract List<WebElement> getWebElementList(PageElement pageElement);

  public int getWebElementListLen(PageElement pageElement) {
    return this.getWebElementList(pageElement).size();
  }

  protected final By getLocator(PageElement pageElement) {
    return pageElement.forPlatform(this.platformType);
  }

  protected FluentWait<T> getFluentWaitDriver() {
    return new AppiumFluentWait<>(driver)
        .withTimeout(Duration.ofMillis(this.getMaxWaitTimeout()))
        .ignoring(NoSuchElementException.class)
        .pollingEvery(Duration.ofMillis(this.getPollingInterval()));
  }

  @Override
  public String toString() {
    return format("use the {0}", getDriverName());
  }

  @Override
  public void tearDown() {
    driver.quit();
    driver.close();
  }
}
