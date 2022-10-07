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

import de.gematik.test.erezept.app.cfg.AppConfiguration;
import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.exceptions.FeatureNotImplementedException;
import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;

@Slf4j
public class UseIOSApp extends UseTheApp {

  protected UseIOSApp(AppiumDriver driver, AppConfiguration appConfiguration) {
    super(driver, PlatformType.IOS, appConfiguration);
  }

  @Override
  public WebElement getWebElement(By locator) {
    log.info(format("Try to fetch element {0}", locator));

    val wait = this.getFluentWaitDriver();
    val start = Instant.now();
    try {
      val el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
      val duration = Duration.between(start, Instant.now());
      log.info(format("Found element <{0}> after {1}ms", el, duration.toMillis()));
      return el;
    } catch (NoSuchElementException nsee) {
      val duration = Duration.between(start, Instant.now());
      log.error(
          format("Failed to fetch element with <{0}> after {1}ms", locator, duration.toMillis()));
      throw nsee;
    }
  }

  @Override
  public List<WebElement> getWebElementList(String identifier) {
    throw new FeatureNotImplementedException("getWebElementList on iOS");
  }
}
