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
import io.appium.java_client.AppiumDriver;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.core.exceptions.NoSuchElementException;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

@Slf4j
public class UseAndroidApp extends UseTheApp {

  protected UseAndroidApp(AppiumDriver driver, AppConfiguration appConfiguration) {
    super(driver, PlatformType.ANDROID, appConfiguration);
  }

  @Override
  public WebElement getWebElement(By locator) {
    log.info(format("Try to fetch element <{0}>", locator));
    val start = Instant.now();
    try {
      val element = probeWebElements(locator, () -> driver.findElement(locator));
      val duration = Duration.between(start, Instant.now());
      log.info(format("Found element <{0}> after {1}ms", element, duration.toMillis()));
      return element;
    } catch (NoSuchElementException nsee) {
      val duration = Duration.between(start, Instant.now());
      log.error(
          format("Failed to fetch element with <{0}> after {1}ms", locator, duration.toMillis()));
      throw nsee;
    }
  }

  @Override
  public List<WebElement> getWebElementList(String identifier) {
    val locator = this.getLocator(identifier);
    log.info(format("Try to fetch element <{0}> via {1}", identifier, locator));
    return probeWebElements(locator, () -> driver.findElements(locator));
  }

  /**
   * this method fixes the issue of espresso driver, which blocks infinitely if you try to find an
   * element which is not there yet, and it does not seem to retry. This method will start
   * repeatedly the findElement on the driver and probe for the element for the configured
   * maxWaitTimeout
   *
   * @param locator is the By locator which is used to locate the WebElement on the device
   * @param supplier is the supplier which will apply the action on the driver
   * @return the WebElement if it is present within maxWaitTimeout on the screen
   * @throws org.openqa.selenium.NoSuchElementException if WebElement is not on the Screen
   */
  private <R> R probeWebElements(By locator, Supplier<R> supplier) {
    // 500 ms per probe should be enough; still might cause problems on long round-trip-times to the
    // appium server
    val probeDuration = 0.5;
    val probeDurationMs = (int) (probeDuration * 1000);
    int retries =
        (int) Math.ceil(this.getMaxWaitTimeout() / probeDuration); // divide by 50ms per retry

    R webElements = null;

    while (retries > 0) {
      retries--;
      ExecutorService executor = Executors.newSingleThreadExecutor();
      Future<R> future = executor.submit(new BlockingCall<>(supplier));

      try {
        webElements = future.get(probeDurationMs, TimeUnit.MILLISECONDS);
        // we got our element, break out of the while-loop
        break;
      } catch (InterruptedException ie) {
        log.warn(
            format(
                "Thread which was looking for element <{0}> was unexpectedly interrupted",
                locator));

        // SONAR says, never swallow an InterruptedException: re-throw or re-interrupt
        Thread.currentThread().interrupt();
        throw new NoSuchElementException(
            format("An element could not be located on screen with locator {0}", locator), ie);
      } catch (TimeoutException | ExecutionException e) {
        log.info(format("Element {0} not found; still {1} retries remaining", locator, retries));
      }
    }

    if (null == webElements) {
      throw new NoSuchElementException(
          format("An element could not be located on screen with locator {0}", locator));
    } else {
      return webElements;
    }
  }

  @AllArgsConstructor
  static class BlockingCall<R> implements Callable<R> {

    private Supplier<R> supplier;

    @Override
    public R call() {
      return supplier.get();
    }
  }
}
