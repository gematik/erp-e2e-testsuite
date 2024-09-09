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

package de.gematik.test.erezept.client.exceptions;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.util.OperationOutcomeWrapper;
import javax.annotation.Nullable;
import lombok.val;
import org.hl7.fhir.r4.model.OperationOutcome;
import org.hl7.fhir.r4.model.Resource;

public class UnexpectedResponseResourceError extends AssertionError {
  public <T extends Resource> UnexpectedResponseResourceError(
      Class<? extends Resource> expected, @Nullable T actual) {
    super(createErrorMessage(expected, actual));
  }

  private static <T extends Resource> String createErrorMessage(
      Class<? extends Resource> expected, @Nullable T actual) {
    val genericPattern = "Request expected Response of type {0} but received {1}";
    String ret;
    if (actual != null) {
      val actualType = actual.getClass();
      if (OperationOutcome.class.equals(actualType)) {
        ret = createOperationOutcomeMessage(expected, (OperationOutcome) actual);
      } else {
        ret = format(genericPattern, expected.getSimpleName(), actual);
      }
    } else {
      ret = format(genericPattern, expected.getSimpleName(), "NULL");
    }

    return ret;
  }

  private static String createOperationOutcomeMessage(
      Class<? extends Resource> expected, OperationOutcome operationOutcome) {
    return format(
        "Request expected Response of type {0} but received OperationOutcome: {1}",
        expected.getSimpleName(), OperationOutcomeWrapper.from(operationOutcome));
  }
}
