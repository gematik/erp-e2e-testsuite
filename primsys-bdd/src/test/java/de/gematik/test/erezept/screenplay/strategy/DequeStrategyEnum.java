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

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.MissingPreconditionError;
import de.gematik.test.erezept.lei.exceptions.InvalidStrategyMappingException;
import de.gematik.test.erezept.screenplay.util.ManagedList;
import java.util.List;
import lombok.NonNull;
import lombok.val;

/**
 * Define how to dequeue an element from a list.
 *
 * <ul>
 *   <li>LIFO (last in, first out) signals to dequeue the <b>youngest</b> element
 *   <li>FIFO (first in, first out) signals to dequeue the <b>oldest</b> element
 * </ul>
 */
public enum DequeStrategyEnum {
  FIFO,
  LIFO,
  ;

  public <T> T chooseFrom(@NonNull List<T> objects) {
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

  public <T> T chooseFrom(@NonNull ManagedList<T> managedList) {
    return chooseFrom(managedList.getRawList());
  }

  public <T> void removeFrom(@NonNull ManagedList<T> managedList) {
    val item = chooseFrom(managedList);
    managedList.getRawList().remove(item);
  }

  public static DequeStrategyEnum fromString(String value) {
    DequeStrategyEnum ret;
    switch (value.toLowerCase()) {
      case "letzte":
      case "letzten":
      case "letztes":
      case "jüngstes":
        ret = LIFO;
        break;
      case "erste":
      case "ersten":
      case "erstes":
      case "ältestes":
        ret = FIFO;
        break;
      default:
        throw new InvalidStrategyMappingException(DequeStrategyEnum.class, value);
    }
    return ret;
  }
}
