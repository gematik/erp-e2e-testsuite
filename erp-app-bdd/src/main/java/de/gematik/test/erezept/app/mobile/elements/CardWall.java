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

import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.openqa.selenium.By;

@Getter
@RequiredArgsConstructor
@SuppressWarnings({"java:S1192"}) // it's okay to duplicate string literals here
public enum CardWall implements PageElement {
  NEXT_BUTTON(
      "Next Button",
      () -> By.tagName("CardWall.ContinueButton"),
      () -> AppiumBy.accessibilityId("cdw_btn_intro_later")),
  ADD_HEALTH_CARD_BUTTON("Next Button", () -> null, () -> AppiumBy.name("cdw_btn_intro_advance")),
  CAN_INPUT_FIELD(
      "CAN Input Field",
      () -> By.tagName("CardWall.CAN.CANField"),
      () -> AppiumBy.accessibilityId("cdw_txt_can_input")),
  CAN_ACCEPT_BUTTON(
      "CAN Accept Button",
      () -> By.tagName("CardWall.ContinueButton"),
      () -> AppiumBy.accessibilityId("cdw_btn_can_done")),
  PIN_INPUT_FIELD(
      "PIN Input Field",
      () -> By.tagName("CardWall.PIN.PINField"),
      () -> AppiumBy.accessibilityId("cdw_edt_pin_input")),
  PIN_ACCEPT_BUTTON(
      "PIN Accept Button",
      () -> By.tagName("CardWall.ContinueButton"),
      () -> AppiumBy.name("cdw_btn_pin_no_pin")),
  DONT_SAVE_CREDENTIAL_BUTTON(
      "Dont save the credentials button",
      () -> null,
      () -> AppiumBy.accessibilityId("cdw_txt_loginOption_without_biometry")),
  CONTINUE_AFTER_BIOMETRY_CHECK_BUTTON(
      "Continue after the biometry decision button",
      () -> null,
      () -> AppiumBy.accessibilityId("cdw_btn_loginOption_continue")),

  START_NFC_READOUT_BUTTON(
      "Start the NFC readout Button",
      () -> null,
      () ->
          AppiumBy.iOSNsPredicateString(
              "type == \"XCUIElementTypeButton\" AND name == \"cdw_btn_rc_next\" AND label =="
                  + " \"Karte verbinden\"")),
  GKV_INSURED_BUTTON(
      "Select GKV insured", () -> null, () -> AppiumBy.accessibilityId("wlcd_btn_gkv_user"));

  private final String elementName;
  private final Supplier<By> androidLocator;
  private final Supplier<By> iosLocator;
}
