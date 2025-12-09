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
public enum EVDGAInsuranceDetails implements PageElement {
  RETURN_TO_EVDGA_DETAILS("Return to DIGA screen", () -> AppiumBy.accessibilityId("Zurück")),
  INSURANCE_SEARCH_BAR(
      "search insurance by name",
      () -> By.xpath("//XCUIElementTypeTextField[@name='ctl_txt_search_bar_field']"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  public static PageElement forInsuranceEntry(String insuranceName) {
    return new EVDGAInsuranceDetails.SearchResultEntry(insuranceName);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class SearchResultEntry implements PageElement {

    private final String insuranceName;

    @Override
    public String getElementName() {
      return format("Insurance Search Entry for {0}", insuranceName);
    }

    @Override
    public Supplier<By> getAndroidLocator() {
      return () -> null;
    }

    @Override
    public Supplier<By> getIosLocator() {
      return () ->
          AppiumBy.iOSNsPredicateString(
              format(
                  "name == \"{0}\" AND label == \"{0}\" AND type == \"XCUIElementTypeButton\"",
                  insuranceName));
    }
  }
}
