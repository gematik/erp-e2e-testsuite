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

package de.gematik.test.erezept.app.mobile.elements;

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
@SuppressWarnings({"java:S1192"}) // it's okay to duplicate string literals here
public enum CardWall implements PageElement {
  NEXT(
      "Next",
      () -> By.tagName("cardWall/next"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  SIGN_IN(
      "Sign in",
      () -> By.tagName("cardWall/next"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  CAN_INPUT(
      "CAN Input",
      () -> By.tagName("cardWall/cardAccessNumberInputField"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  PIN_INPUT(
      "PIN Input",
      () -> By.tagName("cardWall/personalIdentificationNumberInputField"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
