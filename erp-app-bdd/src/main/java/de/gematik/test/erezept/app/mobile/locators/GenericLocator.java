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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.cfg.PlatformType;
import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import lombok.Data;
import lombok.val;
import org.openqa.selenium.By;

@Data
public class GenericLocator {

  private String semanticName;
  private String identifier;
  private PlatformSpecificLocator android;
  private PlatformSpecificLocator ios;

  public PlatformSpecificLocator getSpecificLocator(PlatformType platform) {
    PlatformSpecificLocator ret;
    switch (platform) {
      case ANDROID:
        ret = android;
        break;
      case IOS:
        ret = ios;
        break;
      default:
        throw new UnsupportedPlatformException(format("No locators available for {0}", platform));
    }
    return ret;
  }

  public By forPlatform(PlatformType platform) {
    val specific = getSpecificLocator(platform);
    return specific.getLocator();
  }
}
