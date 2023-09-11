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
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

import java.util.function.Supplier;

@Getter
@RequiredArgsConstructor
public enum Utility implements PageElement {
  TOOLTIPS("Tooltip", null, () -> AppiumBy.accessibilityId("ctl_tooltip_container")),
  //  ACCEPT_LOGIN(
  //      "Accept digital reception of ePrescriptions",
  //      () -> null,
  //      () -> AppiumBy.accessibilityId("wlcd_btn_login")),
  DECLINE_LOGIN("Decline Cardwall Login", null, () -> AppiumBy.accessibilityId("wlcd_btn_decline")),

  SAVE_PASSWORD_TO_KEYCHAIN(
          "Save Password to Keychain", () -> null, () -> AppiumBy.accessibilityId("Passwort sichern")),
  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}