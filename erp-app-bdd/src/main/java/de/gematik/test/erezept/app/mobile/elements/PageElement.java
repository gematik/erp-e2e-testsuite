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

package de.gematik.test.erezept.app.mobile.elements;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.exceptions.UnavailablePageElementLocatorException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumBy;
import java.util.UUID;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.val;
import org.openqa.selenium.By;

public interface PageElement {

  Pattern XPATH_PATTERN = Pattern.compile("@\\w+\\='(.+)'", Pattern.CASE_INSENSITIVE);

  default String getPage() {
    return this.getClass().getSimpleName();
  }

  default String getFullName() {
    return format("{0} > {1}", this.getPage(), this.getElementName());
  }

  default By forPlatform(PlatformType platform) {
    val supplier =
        switch (platform) {
          case ANDROID -> getAndroidLocator();
          case IOS -> getIosLocator();
          default -> throw new UnsupportedPlatformException(
              format("No locators available for {0}", platform));
        };

    if (supplier == null) {
      throw new UnavailablePageElementLocatorException(this, platform);
    }

    val locator = supplier.get();
    if (locator == null) {
      throw new UnavailablePageElementLocatorException(this, platform);
    }

    return locator;
  }

  default String extractSourceLabel(PlatformType platform) {
    val locator = this.forPlatform(platform);

    if (locator instanceof By.ByXPath xpathLocator) {
      val matcher = XPATH_PATTERN.matcher(xpathLocator.toString());
      if (matcher.find()) {
        return matcher.group(1);
      } else {
        return UUID.randomUUID().toString(); // ensure nothing is found in the page source
      }
    }

    val locatorValue = locator.toString().split(": ")[1];
    if (locator instanceof AppiumBy.ByIosNsPredicate) {
      // ByIosNsPredicates have a special syntax: for now we care only about == comparison
      return locatorValue.replace("==", "=");
    } else {
      return locatorValue;
    }
  }

  String getElementName();

  Supplier<By> getAndroidLocator();

  Supplier<By> getIosLocator();
}
