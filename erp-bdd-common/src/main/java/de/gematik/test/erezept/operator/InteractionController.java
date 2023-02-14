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

package de.gematik.test.erezept.operator;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.exceptions.TestcaseAbortedException;
import java.util.concurrent.Callable;

public class InteractionController<T> implements Callable<T>, InteractionListener<T> {

  private TestcaseAbortedException abort;
  private final transient InteractionView<T> view;
  private transient T returnValue;

  protected InteractionController(InteractionView<T> view) {
    this.view = view;
  }

  public final void receiveCancelEvent() {
    this.abort = new TestcaseAbortedException(format("Instruction: {0}", view.getInstruction()));
  }

  public final void receiveInteractionEvent(T event) {
    returnValue = event;
  }

  public T call() {
    view.register(this);
    // UI will block until an Event is triggered by the human operator, which will be forwarded back
    // to me
    view.start();
    return finishInteraction();
  }

  private T finishInteraction() {
    if (abort != null) {
      throw abort;
    } else {
      return returnValue;
    }
  }
}
