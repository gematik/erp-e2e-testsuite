/*
 * Copyright 2023 gematik GmbH
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

import de.gematik.test.erezept.app.mobile.PlatformType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;

import java.util.function.Supplier;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class XpathPageElement implements PageElement {

  private final By xPath;

  public static XpathPageElement xPathPageElement(String xpath) {
    return new XpathPageElement(By.xpath(xpath));
  }

  @Override
  public String getPage() {
    return "dynamic element";
  }

  @Override
  public String getFullName() {
    return "dynamic xpath element";
  }

  @Override
  public By forPlatform(PlatformType platform) {
    return this.xPath;
  }

  @Override
  public String getElementName() {
    return "dynamic element";
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> this.xPath;
  }

  @Override
  public Supplier<By> getIosLocator() {
    return () -> this.xPath;
  }
}
