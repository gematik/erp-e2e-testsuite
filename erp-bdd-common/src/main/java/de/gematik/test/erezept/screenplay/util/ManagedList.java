/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.util;

import de.gematik.test.erezept.exceptions.*;
import java.util.*;
import java.util.function.*;
import lombok.*;

public class ManagedList<T> {

  private final List<T> managed;
  private final Supplier<String> onErrorMessageSupplier;

  public ManagedList(Supplier<String> onError) {
    this.managed = new ArrayList<>();
    this.onErrorMessageSupplier = onError;
  }

  public boolean isEmpty() {
    return this.managed.isEmpty();
  }

  public List<T> getRawList() {
    return managed;
  }

  public void append(T element) {
    this.managed.add(element);
  }

  public void prepend(T element) {
    this.managed.add(0, element);
  }

  public T getFirst() {
    return get(0, false);
  }

  public T consumeFirst() {
    return get(0, true);
  }

  public T getLast() {
    return get(-1, false);
  }

  public T consumeLast() {
    return get(-1, true);
  }

  /**
   * Reusable method for consuming or simply fetching an element by its index
   *
   * @param index of the element where -1 will return the last element
   * @param consume if true the element will be consumed
   * @return the element at the given index
   * @throws MissingPreconditionError if the managed list is empty
   */
  private T get(int index, boolean consume) {
    if (this.managed.isEmpty()) {
      throw new MissingPreconditionError(onErrorMessageSupplier.get());
    }

    // get the correct index: where e.g. -1 leads to last element
    val idx = (index >= 0 && index < managed.size() - 1) ? index : managed.size() - 1;
    T ret;
    if (consume) {
      ret = managed.remove(idx);
    } else {
      ret = managed.get(idx);
    }
    return ret;
  }
}
