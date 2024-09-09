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

import de.gematik.test.core.exceptions.NotInitializedException;
import de.gematik.test.erezept.apimeasure.ApiCallStopwatch;
import de.gematik.test.erezept.apimeasure.DumpingStopwatch;
import lombok.Getter;

public class StopwatchProvider {

  private static StopwatchProvider instance;

  @Getter private final ApiCallStopwatch stopwatch;

  private StopwatchProvider() {
    this.stopwatch = new DumpingStopwatch("prod_testsuite");
  }

  public static void init() {
    if (instance == null) {
      instance = new StopwatchProvider();
    }
  }

  public static StopwatchProvider getInstance() {
    if (instance == null) {
      throw new NotInitializedException(ApiCallStopwatch.class);
    }
    return instance;
  }

  public static void close() {
    getInstance().getStopwatch().close();
    instance = null;
  }
}
