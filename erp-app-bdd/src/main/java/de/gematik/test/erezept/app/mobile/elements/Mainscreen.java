/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.app.mobile.elements;

import static java.text.MessageFormat.format;

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
public enum Mainscreen implements PageElement {
  REFRESH_BUTTON(
      "Refresh",
      () -> By.tagName("erx_btn_refresh"),
      () -> AppiumBy.accessibilityId("erx_btn_refresh")),
  NEW_USER_PROFILE_BUTTON(
      "Create a new Profile",
      () -> null,
      () -> AppiumBy.accessibilityId("pro_btn_selection_add_profile")),
  LOGIN_BUTTON(
      "Login into the Cardwall button",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_btn_login")),
  DELETE_BUTTON(
      "Delete Prescription",
      () -> By.xpath("/ComposeNode/ComposeNode/VerticalScrollAxisRange/Button"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  PRESCRIPTIONS_LIST(
      "List of Prescriptions",
      () ->
          By.xpath(
              "/ComposeNode/ComposeNode[1]/ComposeNode[1]/ComposeNode/VerticalScrollAxisRange/ComposeNode"),
      () -> AppiumBy.accessibilityId("erx_btn_show_settings")),
  PRESCRIPTION_LIST_ELEMENT_NAME(
      "List of Prescriptions",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_detailed_prescription_name")),
  PRESCRIPTION_LIST_ELEMENT_STATUS(
      "Status of the Prescription",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_detailed_status")),
  PRESCRIPTION_LIST_ELEMENT_VALIDITY_DATE(
      "Validity Date of the Prescription",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_detailed_prescription_validity")),
  PRESCRIPTION_ARCHIVE(
      "Prescription Archive",
      () -> null,
      () -> AppiumBy.accessibilityId("erx_btn_arc_prescription")),
  CLOSE_CHARGE_ITEM_CONSENT_DRAWER_BUTTON(
      "Close button of the charge item consent drawer",
      null,
      () -> AppiumBy.accessibilityId("erx_btn_consent_drawer_close"));

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;

  public static PageElement forProfile(String profileName) {
    return new ProfileButton(profileName);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class ProfileButton implements PageElement {

    private static final String ACCESSIBILITY_ID = "pro_btn_selection_profile_row";
    private final String profileName;

    @Override
    public String getElementName() {
      return format("Profile Button for {0}", profileName);
    }

    @Override
    public Supplier<By> getAndroidLocator() {
      return () -> null;
    }

    @Override
    public Supplier<By> getIosLocator() {
      return () ->
          AppiumBy.iOSNsPredicateString(
              format("name == \"{0}\" AND label == \"{1}\"", ACCESSIBILITY_ID, profileName));
    }
  }
}
