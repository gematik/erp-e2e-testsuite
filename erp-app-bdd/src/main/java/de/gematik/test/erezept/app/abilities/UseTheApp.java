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
import de.gematik.test.erezept.app.exceptions.InvalidLocatorException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.SwipeDirection;
import de.gematik.test.erezept.app.mobile.locators.GenericLocator;
import de.gematik.test.erezept.app.mobile.locators.LocatorDictionary;
import de.gematik.test.erezept.app.mobile.locators.PlatformSpecificLocator;
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
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;
import org.openqa.selenium.support.ui.FluentWait;

@Slf4j
public abstract class UseTheApp implements Ability, HasTeardown {

  protected AppiumDriver driver;
  @Getter private final PlatformType platformType;
  private final AppConfiguration appConfiguration;
  private static final LocatorDictionary locatorDict = LocatorDictionary.getInstance();

  protected UseTheApp(
      AppiumDriver driver, PlatformType platformType, AppConfiguration appConfiguration) {
    this.driver = driver;
    this.platformType = platformType;
    this.appConfiguration = appConfiguration;
  }

  protected int getMaxWaitTimeout() {
    return appConfiguration.getMaxWaitTimeout();
  }

  public boolean useVirtualeGK() {
    return appConfiguration.isUseVirtualeGK();
  }

  public static UseTheApp with(
      AppiumDriver driver, PlatformType platformType, AppConfiguration appConfiguration) {
    UseTheApp app;
    if (platformType == PlatformType.ANDROID) {
      app = new UseAndroidApp(driver, appConfiguration);
    } else if (platformType == PlatformType.IOS) {
      app = new UseIOSApp(driver, appConfiguration);
    } else {
      throw new UnsupportedPlatformException(platformType);
    }
    return app;
  }

  public void tap(String identifier) {
    val element = this.getWebElement(identifier);
    this.tap(element);
  }

  public void tap(int times, String identifier) {
    assertThat(times).isGreaterThan(0);
    for (int i = 0; i < times; i++) {
      this.tap(identifier);
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

  public void tap(WebElement element) {
    log.info(format("Tap on element {0}", element));
    element.click();
  }

  public void tap(By locator) {
    val element = this.getWebElement(locator);
    this.tap(element);
  }

  public void input(final String text, String identifier) {
    val element = this.getWebElement(identifier);
    log.info(format("Type {0} into {1} ({2})", text, identifier, element));
    element.clear(); // clear the default text in the input
    element.sendKeys(text);

    // refetch the element and check if appium sent the text correctly
    val input = this.getWebElement(identifier).getText();
    assertThat(input).isEqualTo(text);
  }

  /**
   * As appium seem to lose some chars from time to time, passwords are typed char by char to slow
   * down the process and prevent losing characters.
   *
   * @param password is the password to be typed
   * @param identifier is the input field where the password is typed in
   */
  public void inputPassword(final String password, String identifier) {
    this.inputPassword(password, identifier, false);
  }

  /**
   * Just as PoC to check if x/y guessing works reliably
   *
   * @param password
   * @param identifier
   * @param showPassword
   */
  public void inputPassword(final String password, String identifier, boolean showPassword) {
    val element = this.getWebElement(identifier);
    log.info(format("Type Password {0} into {1} ({2})", password, identifier, element));
    element.clear();
    for (val c : password.toCharArray()) {
      element.sendKeys(format("{0}", c));
    }

    if (showPassword) {
      // try to guess the position of "eye-icon"
      val rect = element.getRect();
      val x = (int) (rect.getX() + rect.getWidth() * 0.95);
      val y = (int) (rect.getY() + rect.getHeight() * 0.5);
      val p = new Point(x, y);
      this.tap(p);
    }
  }

  public void swipe(SwipeDirection direction) {
    log.info(format("Swipe {0} on the Screen", direction));
    val screenDimension = driver.manage().window().getSize();
    val swipe = direction.swipeOn(screenDimension);
    this.performGesture(swipe);
  }

  public void performGesture(Sequence sequence) {
    driver.perform(Collections.singletonList(sequence));
  }

  public WebElement getWebElement(String identifier) {
    log.info(format("Fetch element via semantic locator <{0}>", identifier));
    val locator = this.getLocator(identifier);
    return getWebElement(locator);
  }

  public abstract WebElement getWebElement(By locator);

  public abstract List<WebElement> getWebElementList(String identifier);

  public int getWebElementListLen(String identifier) {
    return this.getWebElementList(identifier).size();
  }

  /**
   * By this method, the sub-classes can easily search for a platform-specific locator
   *
   * @param identifier can be either the semantic name or the identifier from the
   *     locators-dictionary
   * @return a concrete platform-specific By locator
   */
  public final By getLocator(String identifier) {
    val locator = getPlatformLocator(identifier);
    return locator.getLocator();
  }

  public final PlatformSpecificLocator getPlatformLocator(String identifier) {
    // first, look by its semantic name
    var locatorOpt = locatorDict.getOptionallyBySemanticName(identifier);

    GenericLocator genericLocator;
    if (locatorOpt.isEmpty()) {
      // if not found by semantic name, try finding by ID
      // this will definitely throw an exception if the locator is unknown to the dictionary
      genericLocator = locatorDict.getByIdentifier(identifier);
    } else {
      genericLocator = locatorOpt.orElseThrow(() -> new InvalidLocatorException(identifier));
    }

    return genericLocator.getSpecificLocator(platformType);
  }

  protected FluentWait<AppiumDriver> getFluentWaitDriver() {
    return new AppiumFluentWait<>(driver)
        .withTimeout(Duration.ofSeconds(this.getMaxWaitTimeout()))
        .pollingEvery(Duration.ofMillis(50));
  }

  public String getDriverName() {
    return format(
        "Appium Driver for {0} connected to {1}", platformType, driver.getRemoteAddress());
  }

  @Override
  public String toString() {
    return format("use the {0}", getDriverName());
  }

  @Override
  public void tearDown() {
    driver.quit();
  }
}
