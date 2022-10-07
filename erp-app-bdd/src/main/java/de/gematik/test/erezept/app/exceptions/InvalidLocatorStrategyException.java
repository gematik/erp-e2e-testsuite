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

package de.gematik.test.erezept.app.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.app.mobile.locators.LocatorStrategy;

public class InvalidLocatorStrategyException extends RuntimeException {
  public InvalidLocatorStrategyException(LocatorStrategy strategy) {
    super(format("LocatorStrategy {0} is not supported", strategy));
  }

  public InvalidLocatorStrategyException(String strategyName) {
    super(format("Given name for LocatorStrategy {0} is invalid", strategyName));
  }
}
