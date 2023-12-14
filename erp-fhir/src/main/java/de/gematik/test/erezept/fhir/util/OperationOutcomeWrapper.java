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

package de.gematik.test.erezept.fhir.util;

import org.hl7.fhir.r4.model.OperationOutcome;

import java.util.stream.Collectors;

import static java.text.MessageFormat.format;

public class OperationOutcomeWrapper {

  private final OperationOutcome operationOutcome;

  private OperationOutcomeWrapper(OperationOutcome operationOutcome) {
    this.operationOutcome = operationOutcome;
  }

  public static OperationOutcomeWrapper from(OperationOutcome operationOutcome) {
    return new OperationOutcomeWrapper(operationOutcome);
  }

  public static String extractFrom(OperationOutcome operationOutcome) {
    return new OperationOutcomeWrapper(operationOutcome).toString();
  }

  @Override
  public String toString() {
    return this.operationOutcome.getIssue().stream()
        .map(
            issue ->
                format(
                    "[{0}] {1}: {2}",
                    issue.getSeverity(), issue.getDetails().getText(), issue.getDiagnostics()))
        .collect(Collectors.joining("\n"));
  }
}
