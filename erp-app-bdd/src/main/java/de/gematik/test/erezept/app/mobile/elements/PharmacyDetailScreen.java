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
public enum PharmacyDetailScreen implements PageElement {
  PHARMACY_PICKUP_BUTTON(
      "Choose pickup option", () -> AppiumBy.accessibilityId("pha_detail_btn_pickup")),
  REDEEM_ORDER_BUTTON(
      "Redeem a selected order for a prescription",
      () -> AppiumBy.accessibilityId("pha_redeem_btn_redeem")),
  SUCCESSFULLY_REDEEMED_LABEL(
      "Successfully redeemed",
      () -> AppiumBy.iOSNsPredicateString("label == \"Erfolgreich eingelöst\"")),
  BACK_TO_HOME("Back to home", () -> AppiumBy.accessibilityId("pha_redeem_btn_redeem"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }
}
