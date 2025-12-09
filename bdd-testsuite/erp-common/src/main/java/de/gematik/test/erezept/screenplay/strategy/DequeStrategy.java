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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.screenplay.strategy;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.InvalidStrategyMappingException;
import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.Comparator;
import java.util.List;
import lombok.val;

/**
 * Define how to dequeue an element from a list.
 *
 * <ul>
 *   <li>LIFO (last in, first out) signals to dequeue the <b>youngest</b> element
 *   <li>FIFO (first in, first out) signals to dequeue the <b>oldest</b> element
 * </ul>
 */
public enum DequeStrategy {
  FIFO,
  LIFO,
  ;

  public <T> T chooseFrom(List<T> objects) {
    if (objects.isEmpty()) {
      throw new MissingPreconditionError(format("Cannot deque with {0} from empty list", this));
    }

    T ret;
    if (this.equals(LIFO)) {
      ret = objects.get(objects.size() - 1);
    } else {
      ret = objects.get(0);
    }
    return ret;
  }

  public <T> T chooseFrom(ManagedList<T> managedList) {
    return chooseFrom(managedList.getRawList());
  }

  public <T> T chooseFrom(List<T> objects, Comparator<? super T> comparator) {
    val sorted = objects.stream().sorted(comparator.reversed()).toList();
    return chooseFrom(sorted);
  }

  public <T> T consume(ManagedList<T> managedList) {
    return consume(managedList.getRawList());
  }

  public <T> T consume(List<T> objects) {
    val ret = chooseFrom(objects);
    objects.remove(ret);
    return ret;
  }

  public <T> void removeFrom(ManagedList<T> managedList) {
    val item = chooseFrom(managedList);
    managedList.getRawList().remove(item);
  }

  @Override
  public String toString() {
    return switch (this) {
      case LIFO -> "letzte(n)";
      case FIFO -> "erste(n)";
    };
  }

  public static DequeStrategy fromString(String value) {
    return switch (value.toLowerCase()) {
      case "letzte", "letzten", "letztes", "jüngstes" -> LIFO;
      case "erste", "ersten", "erstes", "ältestes" -> FIFO;
      default -> throw new InvalidStrategyMappingException(DequeStrategy.class, value);
    };
  }
}
