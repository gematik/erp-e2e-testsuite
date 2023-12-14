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

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum Debug implements PageElement {
  MENU_BUTTON(
      "Debug Menu Button",
      () -> By.tagName("Settings.DebugMenuButton"),
      () -> AppiumBy.accessibilityId("stg_btn_debug")),
  LEAVE_BUTTON(
      "Leave Debug Menu",
      () -> AppiumBy.accessibilityId("TopNavigation.BackButton"),
      () -> By.xpath("(//XCUIElementTypeButton[@name='Einstellungen'])[1]")),
  SET_VIRTUAL_EGK_FIELD(
      "Set virtual eGK Field",
      () -> By.tagName("DebugMenu.SetVirtualHealthCardButton"),
      () -> AppiumBy.accessibilityId("debug_enable_virtual_egk")),
  ENABLE_VIRTUAL_EGK_USAGE_BUTTON(
      "Activate virtual eGK Button",
      null,
      () ->
          AppiumBy.xpath(
              "//XCUIElementTypeSwitch[@name='debug_enable_virtual_egk']/XCUIElementTypeSwitch")),
  EGK_PRIVATE_KEY(
      "Input Private Key of eGK",
      () ->
          By.xpath(
              "/ComposeNode/ComposeNode/VerticalScrollAxisRange/ComposeNode[2]/EditableText[2]"),
      () -> AppiumBy.accessibilityId("debug_prk_ch_aut")),
  EGK_CERTIFICATE_CHAIN(
      "Certificate Chain of eGK",
      () ->
          By.xpath(
              "/ComposeNode/ComposeNode/VerticalScrollAxisRange/ComposeNode[2]/EditableText[1]"),
      () -> AppiumBy.accessibilityId("debug_c_ch_aut")),
  BEARER_TOKEN(
      "Bearer Token",
      () ->
          By.xpath("/ComposeNode/ComposeNode/VerticalScrollAxisRange/ComposeNode[3]/EditableText"),
      () ->
          By.xpath(
              "//XCUIElementTypeCell[@name='Current access token']/XCUIElementTypeOther[2]/XCUIElementTypeOther")),
  LOGIN("Login with virtual eGK", null, () -> By.xpath("//XCUIElementTypeButton[@name=\"Login\"]")),
  HIDE_INTRO(
      "Hide Intro",
      null,
      () ->
          By.xpath(
              "//XCUIElementTypeSwitch[@name=\"debug_tog_hide_intro\"]/XCUIElementTypeSwitch"));

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
