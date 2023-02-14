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

package de.gematik.test.erezept.app.mobile.elements;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.exceptions.UnavailablePageElementLocatorException;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import java.util.function.Supplier;
import lombok.val;
import org.openqa.selenium.By;

public interface PageElement {

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

  String getElementName();

  Supplier<By> getAndroidLocator();

  Supplier<By> getIosLocator();
}
