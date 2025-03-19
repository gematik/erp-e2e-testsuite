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

package de.gematik.test.fuzzing.fhirfuzz;

import de.gematik.test.fuzzing.fhirfuzz.utils.FuzzerContext;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.val;

public interface BaseFuzzer<T> {
  /**
   * Method to fuzz direct a Value with concrete implementations of specified Fuzzer
   *
   * @param value
   * @return fuzzedValue
   */
  T fuzz(T value);

  /**
   * Method to fuzz with specific Getter and Setter a conditional chance behaves for getting a
   * Random Object
   *
   * @param getter
   * @param setter
   */
  default void fuzz(Supplier<T> getter, Consumer<T> setter) {
    if (getContext().conditionalChance()) {
      setter.accept(this.generateRandom());
    } else {
      val a = getter.get();
      val fuzzedOne = this.fuzz(a);
      setter.accept(fuzzedOne);
    }
  }

  /**
   * Method to fuzz with specific Getter and Setter or generate a Random and concrete Object if no
   * one is set
   *
   * @param checker
   * @param getter
   * @param setter
   */
  default void fuzz(BooleanSupplier checker, Supplier<T> getter, Consumer<T> setter) {
    if (!checker.getAsBoolean()) {
      setter.accept(this.generateRandom());
    } else {
      fuzz(getter, setter);
    }
  }

  default String getMapContent(String key) {
    Map<?, ?> map;
    if (getContext().getFuzzConfig().getDetailSetup() != null
        && getContext().getFuzzConfig().getDetailSetup().get(key) != null) {
      map = getContext().getFuzzConfig().getDetailSetup();
    } else {
      return "false";
    }
    val erg = map.get(key);
    return erg.toString();
  }

  T generateRandom();

  FuzzerContext getContext();
}
