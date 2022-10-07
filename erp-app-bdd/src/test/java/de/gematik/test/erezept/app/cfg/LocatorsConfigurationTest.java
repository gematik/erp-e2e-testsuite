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

package de.gematik.test.erezept.app.cfg;

import static java.text.MessageFormat.format;
import static org.junit.Assert.*;

import de.gematik.test.erezept.app.exceptions.InvalidLocatorException;
import de.gematik.test.erezept.app.mobile.locators.GenericLocator;
import de.gematik.test.erezept.app.mobile.locators.LocatorDictionary;
import de.gematik.test.erezept.app.mobile.locators.LocatorStrategy;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class LocatorsConfigurationTest {

  private LocatorDictionary locatorsDict;

  @Before
  public void setup() {
    locatorsDict = LocatorDictionary.getInstance();
  }

  @Test
  public void readLocators() {

    assertTrue(locatorsDict.getLocators().size() > 0);

    val locators = locatorsDict.getLocators();
    locators.forEach(
        l -> {
          assertTrue(l.getSemanticName().length() > 0);
          val ios = l.getIos();
          assertEquals(ios, l.getSpecificLocator(PlatformType.IOS));
          if (ios.getStrategy() != LocatorStrategy.NOT_AVAILABLE) {
            // if a locator strategy is available, make sure the ID is not empty
            assertTrue(ios.getLocatorId().length() > 0);
          }

          val android = l.getAndroid();
          assertEquals(android, l.getSpecificLocator(PlatformType.ANDROID));
          if (android.getStrategy() != LocatorStrategy.NOT_AVAILABLE) {
            assertTrue(android.getLocatorId().length() > 0);
          }
        });
  }

  @Test
  public void shouldProvideBySemanticName() {
    val nextBtnAndroid =
        locatorsDict.getBySemanticName("Onboarding Next").forPlatform(PlatformType.ANDROID);
    assertEquals("By.tagName: onboarding/next", nextBtnAndroid.toString());

    val nextBtnIos =
        locatorsDict.getBySemanticName("Onboarding Next").forPlatform(PlatformType.IOS);
    assertEquals("AppiumBy.accessibilityId: onb_btn_next", nextBtnIos.toString());
  }

  @Test
  public void shouldProvideByIdentifier() {
    val genericPwLocator = locatorsDict.getByIdentifier("onboarding/password_confirm");
    assertEquals("Password Confirmation", genericPwLocator.getSemanticName());

    val androidBy = genericPwLocator.forPlatform(PlatformType.ANDROID);
    assertEquals("By.tagName: onboarding/secure_text_input_2", androidBy.toString());

    val iosBy = genericPwLocator.forPlatform(PlatformType.IOS);
    assertEquals("AppiumBy.accessibilityId: onb_auth_inp_passwordB", iosBy.toString());
  }

  /**
   * The Locators Dictionary locators.json shall not contain duplicate Semantic Names as these are
   * used to clearly identify a locator.
   */
  @Test
  public void ensureNoDuplicateLocatorNames() {
    //    printLocatorsForDebug();
    val distinctNames =
        locatorsDict.getLocators().stream().map(GenericLocator::getSemanticName).distinct().count();
    assertEquals(locatorsDict.getLocators().size(), distinctNames);
  }

  /**
   * The Locators Dictionary locators.json shall not contain duplicate Identifiers as these are used
   * to clearly identify a locator.
   */
  @Test
  public void ensureNoDuplicateLocatorIdentifiers() {
    //    printLocatorsForDebug();
    val distinctIdentifiers =
        locatorsDict.getLocators().stream().map(GenericLocator::getIdentifier).distinct().count();
    assertEquals(locatorsDict.getLocators().size(), distinctIdentifiers);
  }

  @Test
  public void shouldThrowOnInvalidSemanticLocator() {
    assertThrows(InvalidLocatorException.class, () -> locatorsDict.getBySemanticName("ABCD"));
  }

  @Test
  public void shouldReturnEmptyOptionalOnInvalidSemanticLocator() {
    val locator = locatorsDict.getOptionallyBySemanticName("ABCD");
    assertFalse(locator.isPresent());
  }

  @Test
  public void shouldThrowOnInvalidLocatorId() {
    assertThrows(InvalidLocatorException.class, () -> locatorsDict.getByIdentifier("ABCD"));
  }

  /**
   * Note: you cann use this method to print out all locators and check for duplicate Semantic Names
   * and/or identifiers
   */
  private void printLocatorsForDebug() {
    locatorsDict
        .getLocators()
        .forEach(
            l ->
                System.out.println(
                    format("{0} \t\t->\t {1}", l.getSemanticName(), l.getIdentifier())));
  }
}
