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

package de.gematik.test.erezept.exceptions;

import static java.text.MessageFormat.format;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.Task;

public class MissingNegativeBehaviourError extends AssertionError {

  public MissingNegativeBehaviourError(Task decorated, Class<? extends Throwable> expected) {
    super(
        format(
            "Decorated Task {0} did not throw the expected Exception of Type {1}",
            decorated.getClass().getSimpleName(), expected.getSimpleName()));
  }

  public MissingNegativeBehaviourError(Question<?> decorated, Class<? extends Throwable> expected) {
    super(
        format(
            "Decorated Question {0} did not throw the expected Exception of Type {1}",
            decorated.getClass().getSimpleName(), expected.getSimpleName()));
  }
}
