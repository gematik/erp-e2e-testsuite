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

package de.gematik.test.erezept.cli.indexmap;

import ca.uhn.fhir.validation.ValidationResult;
import java.util.List;
import lombok.Data;
import lombok.val;

@Data
public class HapiValidationError {

  private String errorMessage;
  private String errorPath;

  public static List<HapiValidationError> from(ValidationResult result) {
    return result.getMessages().stream()
        .map(r -> from(r.getMessage(), r.getLocationString()))
        .toList();
  }

  public static HapiValidationError from(String message, String path) {
    val hve = new HapiValidationError();
    hve.setErrorMessage(message);
    hve.setErrorPath(path);
    return hve;
  }
}
