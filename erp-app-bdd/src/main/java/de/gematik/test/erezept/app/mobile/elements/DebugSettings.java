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

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum DebugSettings implements PageElement {
  LEAVE_BUTTON(
      "Leave Debug Settings Menu Button",
      () -> null,
      () -> AppiumBy.xpath("//*[@label='Einstellungen']")),
  ENVIRONMENT_SELECTOR(
      "Environment Selector", () -> null, () -> AppiumBy.xpath("//*[@label='Environment']")),

  RU_ENVIRONMENT(
      "Environment Selector for the RU", () -> null, () -> AppiumBy.xpath("//*[@label='RU']")),
  RU_DEV_ENVIRONMENT(
      "Environment Selector for the RU-DEV",
      () -> null,
      () -> AppiumBy.xpath("//*[@label='RU-DEV']")),
  TU_ENVIRONMENT(
      "Environment Selector for the TU", () -> null, () -> AppiumBy.xpath("//*[@label='TU']")),
  ;
  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
