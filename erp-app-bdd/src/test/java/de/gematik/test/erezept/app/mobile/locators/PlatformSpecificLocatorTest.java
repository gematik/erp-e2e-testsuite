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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import de.gematik.test.erezept.app.exceptions.InvalidLocatorStrategyException;
import java.util.List;
import lombok.val;
import org.junit.Test;

public class PlatformSpecificLocatorTest {

  @Test
  public void shouldBuildValidLocatorStrategies() {
    val psl = new PlatformSpecificLocator();
    psl.setLocatorId("some_random_id");
    val validStrategies = List.of("espresso", "xcuitest", "accessibility_id", "xpath");
    validStrategies.forEach(
        strategy -> {
          psl.setStrategy(strategy);
          assertNotNull(
              psl.getLocator()); // if not null, it must be by definition an instance of By
        });
  }

  @Test
  public void shouldThrowOnInvalidLocatorStrategy() {
    val psl = new PlatformSpecificLocator();
    psl.setLocatorId("some_random_id");
    psl.setStrategy("not_available");
    assertThrows(InvalidLocatorStrategyException.class, psl::getLocator);
  }
}
