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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.cfg.AppConfiguration;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.mobile.elements.PageElement;
import io.appium.java_client.ios.IOSDriver;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class UseIOSApp extends UseTheApp<IOSDriver> {

  public UseIOSApp(IOSDriver driver, AppConfiguration appConfiguration) {
    super(driver, PlatformType.IOS, appConfiguration);
  }

  @Override
  protected WebElement getWebElement(By locator) {
    return waitForElement(locator, ExpectedConditions::presenceOfElementLocated);
  }

  public List<WebElement> getWebElementList(PageElement pageElement) {
    return waitForElement(pageElement, ExpectedConditions::presenceOfAllElementsLocatedBy);
  }

  private <T> T waitForElement(
      PageElement pageElement, Function<By, ExpectedCondition<T>> expectation) {
    return waitForElement(this.getLocator(pageElement), expectation);
  }

  private <T> T waitForElement(By locator, Function<By, ExpectedCondition<T>> expectation) {
    log.info(format("Try to fetch element {0}", locator));
    val wait = this.getFluentWaitDriver();
    val start = Instant.now();
    try {
      val el = wait.until(expectation.apply(locator));
      val duration = Duration.between(start, Instant.now());
      log.info(format("Found element <{0}> after {1}ms", el, duration.toMillis()));
      return el;
    } catch (TimeoutException | NoSuchElementException e) {
      val duration = Duration.between(start, Instant.now());
      val errorMsg =
          format("Failed to fetch element with <{0}> after {1}ms", locator, duration.toMillis());
      log.error(errorMsg);
      throw new NoSuchElementException(errorMsg, e);
    }
  }
}
