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

package de.gematik.test.erezept.cli.cmd.generate.param;

import java.util.function.*;

public interface BaseResourceParameter {

  @SuppressWarnings("unchecked")
  default <T> T getOrDefault(T value, Supplier<T> defaultSupplier) {
    if (value != null) {
      if (value instanceof String sv) {
        return (T) getOrDefaultString(sv, String.valueOf(defaultSupplier.get()));
      } else {
        return value;
      }
    } else {
      return defaultSupplier.get();
    }
  }

  private String getOrDefaultString(String value, String defaultValue) {
    if (value.isEmpty() || value.isBlank()) {
      return defaultValue;
    } else {
      return value;
    }
  }
}
