/*
 * Copyright 2024 gematik GmbH
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
public enum Onboarding implements PageElement {
  SKIP_BUTTON("Skip", () -> By.tagName("Onboarding.SkipOnboardingButton"), null),
  NEXT_BUTTON(
      "Next",
      () -> By.tagName("Onboarding.NextButton"),
      () -> AppiumBy.accessibilityId("onb_btn_next")),
  @Deprecated
  USER_PROFILE_FIELD(
      "User Profile",
      () -> By.tagName("onboarding/profile_text_input"),
      () -> AppiumBy.accessibilityId("onb_prf_txt_field")),
  PASSWORD_INPUT_FIELD(
      "Password Input Field",
      () -> By.tagName("Onboarding.Credentials.PasswordFieldA"),
      () -> AppiumBy.accessibilityId("onb_auth_inp_passwordA")),
  PASSWORD_CONFIRMATION_FIELD(
      "Password Confirmation Field",
      () -> By.tagName("Onboarding.Credentials.PasswordFieldB"),
      () -> AppiumBy.accessibilityId("onb_auth_inp_passwordB")),
  ACCEPT_PRIVACY_BUTTON(
      "Accept Privacy Button",
      () -> By.tagName("Onboarding.DataTerms.AcceptDataTermsSwitch"),
      () -> AppiumBy.accessibilityId("onb_btn_accept_privacy")),
  ACCEPT_TERMS_OF_USE_BUTTON(
      "Accept Terms of Use Button",
      () -> By.tagName("Onboarding.DataTerms.AcceptDataTermsSwitch"),
      () -> AppiumBy.accessibilityId("onb_btn_accept_terms_of_use")),

  CHECK_PRIVACY_AND_TOU_BUTTON(
      "Radio button accept both privacy and Terms of Use Button",
      () -> null,
      () -> AppiumBy.accessibilityId("circle")),

  ACCEPT_PRIVACY_AND_TOU_BUTTON(
      "Accept Terms of Use Button", () -> null, () -> AppiumBy.accessibilityId("onb_btn_confirm")),

  ACCEPT_PASSWORD_BUTTON(
      "Accept the given password button",
      () -> null,
      () -> AppiumBy.accessibilityId("onb_auth_btn_password")),

  CONTINUE_ANALYTICS_SCREEN_BUTTON(
      "Continue the analytics screen button",
      () -> null,
      () -> AppiumBy.accessibilityId("onb_ana_btn_continue")),

  NOT_ACCEPT_ANALYTICS_BUTTON(
      "Not accept the analytics button", () -> null, () -> AppiumBy.name("Nicht erlauben")),

  HIDE_SUGGESTION_PIN_SELECTION_BUTTON(
      "the option to remind select a pin in the future not display",
      () -> null,
      () -> AppiumBy.name("sec_txt_system_pin_selection")),

  ACCEPT_SUGGESTION_PIN_SELECTION_BUTTON(
      "Accept the option to remind select a pin in the future not display",
      () -> null,
      () -> AppiumBy.name("sec_btn_system_pin_done")),

  CONFIRM_TERMS_AND_PRIVACY_SELECTION_BUTTON(
      "Confirm Legal Button", null, () -> AppiumBy.accessibilityId("onb_btn_accept")),

  ACCEPT_SECURITY_HINT_BUTTON(
      "Confirm Security Hint Button",
      null,
      () -> AppiumBy.accessibilityId("sec_btn_system_pin_done")),
  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
