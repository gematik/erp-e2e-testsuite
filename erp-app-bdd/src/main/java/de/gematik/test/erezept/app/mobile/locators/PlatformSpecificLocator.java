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

package de.gematik.test.erezept.app.mobile.locators;

import de.gematik.test.erezept.app.exceptions.InvalidLocatorStrategyException;
import io.appium.java_client.AppiumBy;
import lombok.Data;
import org.openqa.selenium.By;

@Data
public class PlatformSpecificLocator {

  private String locatorId;
  private String strategy;

  public LocatorStrategy getStrategy() {
    return LocatorStrategy.fromString(strategy);
  }

  public By getLocator() {
    By ret;
    switch (getStrategy()) {
      case ACCESSIBILITY_ID:
        ret = AppiumBy.accessibilityId(locatorId);
        break;
      case ESPRESSO_TAGNAME:
        ret = By.tagName(locatorId);
        break;
      case XPATH:
        ret = By.xpath(locatorId);
        break;
      default:
        throw new InvalidLocatorStrategyException(getStrategy());
    }
    return ret;
  }
}
