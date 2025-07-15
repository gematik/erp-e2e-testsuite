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
import lombok.RequiredArgsConstructor;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.openqa.selenium.By;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProfileSelectorElement implements PageElement {

  private final String userName;
  private final boolean fromSettingsMenu;

  @Override
  public String getElementName() {
    val from = fromSettingsMenu ? "settings menu" : "main screen";
    return format("Profile Selector for ''{0}'' from {1}", userName, from);
  }

  @Override
  public Supplier<By> getAndroidLocator() {
    return () -> null;
  }

  @Override
  public Supplier<By> getIosLocator() {
    if (fromSettingsMenu) {
      return () ->
          AppiumBy.iOSNsPredicateString(
              format("type == \"XCUIElementTypeButton\" AND label CONTAINS \"{0}\"", userName));
    } else {
      return () ->
          AppiumBy.iOSNsPredicateString(
              format(
                  "type == \"XCUIElementTypeButton\" AND name == \"pro_btn_selection_profile_row\""
                      + " AND label == \"{0}\"",
                  userName));
    }
  }

  public static Builder forActor(Actor actor) {
    return forUser(actor.getName());
  }

  public static Builder forUser(String userName) {
    return new Builder(userName);
  }

  @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
  public static class Builder {
    private final String userName;

    public ProfileSelectorElement fromSettingsMenu() {
      return new ProfileSelectorElement(userName, true);
    }

    public ProfileSelectorElement fromMainScreen() {
      return new ProfileSelectorElement(userName, false);
    }
  }
}
