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

import de.gematik.test.erezept.app.exceptions.UnsupportedPlatformException;
import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumBy;
import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
@SuppressWarnings("java:S5852")
public enum ErrorAlert implements PageElement {

  GENERIC("Fehler"),
  SMARTCARD("Verbindung zur Karte unterbrochen"),
  IDP_ERROR("IDPError");
  
  private static final Pattern ERROR_CODE_PATTERN = Pattern.compile(".*Fehlernummern:\\s+(.*)$");
  
  private final String name;
  

  @Override
  public String extractSourceLabel(PlatformType platform) {
    if (!platform.equals(PlatformType.IOS)) {
      throw new UnsupportedPlatformException(platform, "Error Alert Handling");
    }

    return format("type=\"XCUIElementTypeAlert\" name=\"{0}", name);
  }

  @Override
  public String getElementName() {
    return "Generic App Error Alert";
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  @Override
  public Supplier<By> getIosLocator() {
    return () ->
        AppiumBy.iOSNsPredicateString(format("type == \"XCUIElementTypeAlert\" AND label CONTAINS \"{0}\"", name));
  }
  
  public String extractErrorMessage(List<String> errorLines) {
    val sb = new StringBuilder();
    errorLines.stream()
        .map(line -> line.replace("\n", " "))
        .forEach(
            line -> {
              val matcher = ERROR_CODE_PATTERN.matcher(line);
              if (matcher.matches()) {
                sb.append(matcher.group(1));
              } else {
                sb.append(format("{0}: ", line));
              }
            });
    return sb.toString();
  }
}
