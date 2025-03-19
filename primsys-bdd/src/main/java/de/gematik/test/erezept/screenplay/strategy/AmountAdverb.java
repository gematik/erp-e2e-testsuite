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
 */

package de.gematik.test.erezept.screenplay.strategy;

import static java.text.MessageFormat.format;

import lombok.NonNull;
import org.apache.commons.lang3.NotImplementedException;

public enum AmountAdverb {
  AT_LEAST,
  AT_MOST,
  EXACTLY;

  public static AmountAdverb fromString(@NonNull final String value) {
    return switch (value.toLowerCase()) {
      case "mindestens" -> AmountAdverb.AT_LEAST;
      case "höchstens", "maximal" -> AmountAdverb.AT_MOST;
      case "genau", "exakt" -> AmountAdverb.EXACTLY;
      default -> throw new NotImplementedException(
          format(
              "{0} with Value {1} is not implemented", AmountAdverb.class.getSimpleName(), value));
    };
  }
}
