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

package de.gematik.test.erezept.screenplay.strategy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.NonNull;
import lombok.val;

public class DequeStrategy {
  private final DequeStrategyEnum dequeue;

  public DequeStrategy(DequeStrategyEnum dequeue) {
    this.dequeue = dequeue;
  }

  public <T> T choose(@NonNull List<T> objects, @NonNull Comparator<? super T> comparator) {
    val sorted = objects.stream().sorted(comparator.reversed()).collect(Collectors.toList());
    final T ret;
    if (dequeue == DequeStrategyEnum.FIFO) {
      ret = sorted.get(sorted.size() - 1);
    } else {
      ret = sorted.get(0);
    }
    return ret;
  }
}
