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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.mobile.PlatformType;
import io.appium.java_client.AppiumBy;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import org.openqa.selenium.By;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PrescriptionsViewElement implements PageElement {

  private static final String PRESCRIPTION_ELEMENT_LOCATOR = "erx_detailed_prescription_name";

  @Nullable private final String prescriptionName;

  @Override
  public String extractSourceLabel(PlatformType platform) {
    if (prescriptionName != null) return this.prescriptionName;
    else return PRESCRIPTION_ELEMENT_LOCATOR;
  }

  @Override
  public String getElementName() {
    if (prescriptionName != null) return format("Prescription {0}", prescriptionName);
    else return format("any Prescription");
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return null;
  }

  @Override
  public Supplier<By> getIosLocator() {
    if (prescriptionName != null)
      return () ->
          AppiumBy.iOSNsPredicateString(
              format(
                  "type == \"XCUIElementTypeButton\" AND label CONTAINS \"{0}\"",
                  prescriptionName));
    else
      return () ->
          AppiumBy.iOSNsPredicateString(
              format(
                  "type == \"XCUIElementTypeStaticText\" AND name == \"{0}\"",
                      PRESCRIPTION_ELEMENT_LOCATOR));
  }

  public static PrescriptionsViewElement named(String prescriptionName) {
    return new PrescriptionsViewElement(prescriptionName);
  }

  public static PrescriptionsViewElement withoutName() {
    return new PrescriptionsViewElement(null);
  }
}
