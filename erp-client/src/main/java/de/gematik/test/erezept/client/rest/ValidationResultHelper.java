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

package de.gematik.test.erezept.client.rest;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.*;
import de.gematik.test.erezept.client.exceptions.*;
import java.util.stream.*;
import lombok.*;
import org.hl7.fhir.r4.model.*;

public class ValidationResultHelper {

  private ValidationResultHelper() {
    throw new IllegalStateException("Utility class");
  }

  public static void throwOnInvalidValidationResult(ValidationResult vr) {
    throwOnInvalidValidationResult(Resource.class, vr);
  }

  public static void throwOnInvalidValidationResult(
      Class<? extends Resource> resourceType, ValidationResult vr) {
    if (!vr.isSuccessful()) {
      val validationSummary =
          vr.getMessages().stream()
              .map(
                  m ->
                      format(
                          "[{0} at {1}]: {2}",
                          m.getSeverity(), m.getLocationString(), m.getMessage()))
              .collect(Collectors.joining("\n"));
      val errors =
          vr.getMessages().stream()
              .filter(m -> m.getSeverity().ordinal() >= ResultSeverityEnum.ERROR.ordinal())
              .count();
      val warnings =
          vr.getMessages().stream()
              .filter(m -> m.getSeverity().ordinal() == ResultSeverityEnum.WARNING.ordinal())
              .count();
      val errorMessage =
          format(
              """
                  FHIR Content of {0} is invalid with {1} errors and {2} warnings:
                  ----------
                  {3}
                  ----------""",
              resourceType, errors, warnings, validationSummary);
      throw new FhirValidationException(errorMessage);
    }
  }
}
