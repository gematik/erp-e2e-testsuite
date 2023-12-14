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

package de.gematik.test.erezept.operator;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.operator.jframe.SwingImageInstructionUI;
import de.gematik.test.erezept.operator.jframe.SwingInstructionUI;
import de.gematik.test.erezept.operator.jframe.SwingQuestionUI;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.NotImplementedException;

@Slf4j
public class UIProvider {

  private UIProvider() {
    throw new AssertionError("Do not instantiate this utility-class");
  }

  /**
   * Depending on the GraphicsEnvironment this will return a specialized controller for the
   * Environment
   *
   * @param suppliers is a tuple of Suppliers which can generate an environment specific controller
   * @param <T> the return type of the concrete controller
   * @return a concrete controller
   */
  private static <T> InteractionController<T> getController(
      SupplierTuple<InteractionView<T>> suppliers) {
    if (GraphicsEnvironment.isHeadless()) {
      // TODO:
      throw new NotImplementedException("Headless UI not implemented yet");
    } else {
      val view = suppliers.swing.get();
      return new InteractionController<>(view);
    }
  }

  /**
   * Open a UI and show the instruction to the human operator. This method will block until the
   * human operator confirms the instruction or cancels the Testcase-Execution
   *
   * @param instruction to be shown to the human operator
   */
  public static void getInstructionResult(final String instruction) {
    log.trace("Instruct the human operator to " + instruction);
    val suppliers =
        new SupplierTuple<InteractionView<Void>>(null, () -> new SwingInstructionUI(instruction));
    val controller = getController(suppliers);
    controller.call();
    log.trace("Human operator confirmed Instruction");
  }

  public static void getInstructionResult(final BufferedImage image, final String instruction) {
    log.trace("Instruct the human operator to " + instruction);
    val suppliers =
        new SupplierTuple<InteractionView<Void>>(
            null, () -> new SwingImageInstructionUI(image, instruction));
    val controller = getController(suppliers);
    controller.call();
    log.trace("Human operator confirmed Instruction with image");
  }

  /**
   * Opens a UI and aks the human operator a question which the operator needs to answer. This
   * method will block until the human operator answers the question and confirms or cancels the
   * Testcase-Execution
   *
   * @param question the question to be shown to the human operator
   * @return the answer which the human operator provided
   */
  public static String getQuestionResult(final String question) {
    log.info("Ask the human operator the question: " + question);
    val suppliers =
        new SupplierTuple<InteractionView<String>>(null, () -> new SwingQuestionUI(question));
    val controller = getController(suppliers);
    val answer = controller.call();
    log.info(format("Human operator answered Question <{0}> with <{1}>", question, answer));
    return answer;
  }

  @Data
  @AllArgsConstructor
  static class SupplierTuple<T extends InteractionView<?>> {
    private Supplier<T> headless;
    private Supplier<T> swing;
  }
}
