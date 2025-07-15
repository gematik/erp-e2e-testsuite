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
public enum PharmacySearch implements PageElement {
  SEARCH_FIELD("Searchfield", () -> AppiumBy.name("Suchen")),
  PHARMACY_SEARCH_BACK_BUTTON("Go back to Pharmacy Search", () -> AppiumBy.name("Apothekensuche")),
  CANCEL_SEARCH("Cancel Search", () -> AppiumBy.name("Abbrechen"));

  private final String elementName;
  private final Supplier<By> iosLocator;

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  public static PageElement forPharmacyEntry(String pharmacyName) {
    return new SearchResultEntry(pharmacyName);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class SearchResultEntry implements PageElement {

    private static final String ACCESSIBILITY_ID = "pha_search_txt_result_list_entry";
    private final String pharmacyName;

    @Override
    public String getElementName() {
      return format("Pharmacy Search Entry for {0}", pharmacyName);
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
                  "type == \"XCUIElementTypeButton\" AND name == \"{0}\" AND label CONTAINS"
                      + " \"{1}\"",
                  ACCESSIBILITY_ID, pharmacyName));
    }
  }
}
