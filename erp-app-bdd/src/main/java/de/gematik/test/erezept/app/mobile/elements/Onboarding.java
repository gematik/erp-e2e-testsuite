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
public enum Onboarding implements PageElement {
  SKIP("Skip", () -> By.tagName("Onboarding.SkipOnboardingButton"), null),
  NEXT("Next", () -> By.tagName("onboarding/next"), () -> AppiumBy.accessibilityId("onb_btn_next")),
  USER_PROFILE(
      "User Profile",
      () -> By.tagName("onboarding/profile_text_input"),
      () -> AppiumBy.accessibilityId("onb_prf_txt_field")),
  PASSWORD_INPUT(
      "Password Input",
      () -> By.tagName("onboarding/secure_text_input_1"),
      () -> AppiumBy.accessibilityId("onb_auth_inp_passwordA")),
  PASSWORD_CONFIRMATION(
      "Password Confirmation",
      () -> By.tagName("onboarding/secure_text_input_2"),
      () -> AppiumBy.accessibilityId("onb_auth_inp_passwordB")),
  ACCEPT_PRIVACY(
      "Accept Privacy",
      () -> By.tagName("onb_btn_accept_privacy"),
      () -> AppiumBy.accessibilityId("onb_btn_accept_privacy")),
  ACCEPT_TERMS_OF_USE(
      "Accept Terms of Use",
      () -> By.tagName("onb_btn_accept_terms_of_use"),
      () -> AppiumBy.accessibilityId("onb_btn_accept_terms_of_use")),
  CONFIRM_LEGAL(
      "Confirm Legal",
      () -> By.tagName("onboarding/next"),
      () -> AppiumBy.accessibilityId("onb_btn_accept")),
  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
