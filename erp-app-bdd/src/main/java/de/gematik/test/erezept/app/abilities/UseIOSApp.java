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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.mobile.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import de.gematik.test.erezept.app.mobile.elements.Utility;
import de.gematik.test.erezept.config.dto.app.AppiumConfiguration;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;

@Slf4j
public class UseIOSApp extends UseTheApp<IOSDriver> {

  public UseIOSApp(IOSDriver driver, AppiumConfiguration appiumConfiguration) {
    super(driver, PlatformType.IOS, appiumConfiguration);
  }

  @Override
  protected Optional<WebElement> getOptionalWebElement(PageElement pageElement) {
    return this.getOptionalWebElement(pageElement, driver.getPageSource());
  }

  @Override
  protected Optional<WebElement> getOptionalWebElement(PageElement pageElement, String pageSource) {
    val label = pageElement.extractSourceLabel(this.getPlatformType());
    var found = pageSource.contains(label);

    var retries = 1;
    while (!found && retries-- > 0) {
      // perform one single retry if element was not found
      // this should prevent missing elements due to slow rendering
      log.info(
          "Retry: Element {} was not found in page source by label {}",
          pageElement.getFullName(),
          label);
      found = driver.getPageSource().contains(label);
    }

    if (found) {
      return waitForElement(pageElement, ExpectedConditions::presenceOfElementLocated);
    } else {
      return Optional.empty();
    }
  }

  @Override
  protected WebElement getWebElement(By locator) {
    return waitForElement(locator, ExpectedConditions::presenceOfElementLocated)
        .orElseThrow(
            () -> new NoSuchElementException(format("Element {0} could not be located", locator)));
  }

  @Override
  public List<WebElement> getWebElements(PageElement pageElement) {
    return waitForElement(pageElement, ExpectedConditions::presenceOfAllElementsLocatedBy)
        .orElse(new ArrayList<>(0));
  }

  private <T> Optional<T> waitForElement(
      PageElement pageElement, Function<By, ExpectedCondition<T>> expectation) {
    if (pageElement.getClass().equals(Utility.class)) {
      // use short polling for utility elements
      return waitForElement(
          this.getLocator(pageElement), expectation, this.getShortPollingFluentWaitDriver());
    } else {
      return waitForElement(this.getLocator(pageElement), expectation);
    }
  }

  private <T> Optional<T> waitForElement(
      By locator, Function<By, ExpectedCondition<T>> expectation) {
    return waitForElement(locator, expectation, this.getFluentWaitDriver());
  }

  private <T> Optional<T> waitForElement(
      By locator, Function<By, ExpectedCondition<T>> expectation, FluentWait<IOSDriver> wait) {
    log.trace(format("Try to fetch element {0}", locator));
    val start = Instant.now();
    try {
      val el = wait.until(expectation.apply(locator));
      val duration = Duration.between(start, Instant.now());
      log.info(format("Found element for <{0}> after {1}ms", locator, duration.toMillis()));
      return Optional.ofNullable(el);
    } catch (TimeoutException | NoSuchElementException e) {
      val duration = Duration.between(start, Instant.now());
      val warnMessage =
          format("Failed to fetch element with <{0}> after {1}ms", locator, duration.toMillis());

      // no elements found: check for app errors alerts!
      // this will throw an AppErrorException because the error alert is probably hiding the element
      this.checkForErrorAlerts(this.driver.getPageSource());

      log.info(warnMessage);
      return Optional.empty();
    }
  }
}
