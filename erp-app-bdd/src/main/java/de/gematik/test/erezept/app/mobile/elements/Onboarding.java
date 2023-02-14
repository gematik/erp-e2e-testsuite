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
public enum Onboarding implements PageElement {
  SKIP_BUTTON("Skip", () -> By.tagName("Onboarding.SkipOnboardingButton"), null),
  NEXT_BUTTON(
      "Next",
      () -> By.tagName("Onboarding.NextButton"),
      () -> AppiumBy.accessibilityId("onb_btn_next")),
  @Deprecated
  USER_PROFILE_FIELD( // TODO should be deprecated; No profile creation in onboarding
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
  CONFIRM_TERMS_AND_PRIVACY_SELECTION_BUTTON(
      "Confirm Legal Button", null, () -> AppiumBy.accessibilityId("onb_btn_accept")),
  ;

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
