/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.core;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.exceptions.NotInitializedException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

@Slf4j
class StopwatchProviderTest {

  @Test
  void shouldProvideInitializedStopwatch() {
    assertDoesNotThrow(StopwatchProvider::init);
    assertEquals(StopwatchProvider.getInstance(), StopwatchProvider.getInstance());
    assertDoesNotThrow(StopwatchProvider::close);
  }

  @Test
  void shouldThrowOnUninitializedStopwatch() {
    try {
      // make sure the StopwatchProvider is closed in any case
      StopwatchProvider.close();
    } catch (NotInitializedException nie) {
      log.info("StopwatchProvider was already closed; proceed with testcase");
    }

    assertThrows(NotInitializedException.class, StopwatchProvider::getInstance);
    assertThrows(NotInitializedException.class, StopwatchProvider::close);
  }
}
