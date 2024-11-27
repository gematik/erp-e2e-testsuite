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

package de.gematik.test.erezept.primsys.mapping;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.Getter;
import lombok.val;

@Getter
public abstract class BaseMapper<D> {

  protected final D dto;

  protected BaseMapper(D dto) {
    this.dto = dto;
    complete();
  }

  protected abstract void complete();

  protected final boolean isNullOrEmpty(String value) {
    return value == null || value.isEmpty();
  }

  protected final <T> boolean isNullOrEmpty(T value) {
    if (value == null) {
      return true;
    } else if (value instanceof String strValue) {
      return isNullOrEmpty(strValue);
    } else {
      return false;
    }
  }

  protected final <T> T getOrDefault(T value, Supplier<T> defaultSupplier) {
    if (value == null || (value instanceof String strValue && isNullOrEmpty(strValue)))
      return defaultSupplier.get();
    return value;
  }

  public String getOrDefault(String value, String defaultValue) {
    if (value == null || value.isEmpty()) return defaultValue;
    return value;
  }

  protected final <T> void ensure(
      Supplier<T> getter, Consumer<T> setter, Supplier<T> defaultSupplier) {
    if (isNullOrEmpty(getter.get())) {
      setter.accept(defaultSupplier.get());
    }
  }

  protected final <T> void ensure(
      BooleanSupplier checker, Consumer<T> setter, Supplier<T> defaultSupplier) {
    if (checker.getAsBoolean()) {
      setter.accept(defaultSupplier.get());
    }
  }

  protected final <T> void setIfPresent(Supplier<T> getter, Consumer<T> setter) {
    val value = getter.get();
    if (!isNullOrEmpty(value)) {
      setter.accept(value);
    }
  }
}
