/*
 * Copyright 2023 gematik GmbH
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

package de.gematik.test.erezept.toggle;

import java.util.List;
import java.util.function.Function;
import lombok.Getter;

@Getter
public class BooleanToggle implements FeatureToggle<Boolean> {

  private static final List<String> BOOL_TRUE_VALUES = List.of("yes", "1");

  private final String key;
  private final boolean defaultValue;

  protected BooleanToggle(String key, boolean defaultValue) {
    this.key = key;
    this.defaultValue = defaultValue;
  }

  @Override
  public Function<String, Boolean> getConverter() {
    return this::mapBoolean;
  }

  @Override
  public Boolean getDefaultValue() {
    return defaultValue;
  }

  private boolean mapBoolean(String value) {
    if (BOOL_TRUE_VALUES.contains(value.toLowerCase())) {
      return true;
    } else {
      return Boolean.parseBoolean(value);
    }
  }

  public static BooleanToggle forKey(String key) {
    return forKey(key, false);
  }

  public static BooleanToggle forKey(String key, boolean defaultValue) {
    return new BooleanToggle(key, defaultValue);
  }
}
