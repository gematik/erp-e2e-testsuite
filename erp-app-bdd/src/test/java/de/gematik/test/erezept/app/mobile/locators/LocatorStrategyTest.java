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

package de.gematik.test.erezept.app.mobile.locators;

import static org.junit.Assert.*;

import de.gematik.test.erezept.app.exceptions.InvalidLocatorStrategyException;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class LocatorStrategyTest {

  @Test
  public void shouldParseValidStrategies() {
    val validStrategies =
        List.of("espresso", "xcuitest", "accessibility_id", "xpath", "not_available");
    val expected =
        List.of(
            LocatorStrategy.ESPRESSO_TAGNAME,
            LocatorStrategy.ACCESSIBILITY_ID,
            LocatorStrategy.ACCESSIBILITY_ID,
            LocatorStrategy.XPATH,
            LocatorStrategy.NOT_AVAILABLE);

    assertEquals(validStrategies.size(), expected.size());
    for (var i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), LocatorStrategy.fromString(validStrategies.get(i)));
    }
  }

  @Test
  public void shouldThrowOnInvalidStrategies() {
    val invalidStrategies =
        List.of("expresso", "ycuitest", "accessibilityid", "ypath", "notavailable", "ABC", "");

    invalidStrategies.forEach(
        strategy ->
            assertThrows(
                InvalidLocatorStrategyException.class, () -> LocatorStrategy.fromString(strategy)));
  }
}
