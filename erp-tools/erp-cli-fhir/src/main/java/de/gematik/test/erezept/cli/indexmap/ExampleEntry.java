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

package de.gematik.test.erezept.cli.indexmap;

import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import java.util.LinkedList;
import java.util.List;
import lombok.Data;

@Data
public class ExampleEntry {

  private String fileName;
  private EncodingType fileType;
  private String description;
  private boolean validationSuccessful;

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<HapiValidationResult> validationResults = new LinkedList<>();

  public void addResult(HapiValidationResult error) {
    this.validationResults.add(error);
  }

  public void addResults(List<HapiValidationResult> errors) {
    this.validationResults.addAll(errors);
  }

  public void setValidationResults(ValidationResult result) {
    this.setValidationSuccessful(result.isSuccessful());
    this.addResults(HapiValidationResult.from(result));
  }
}
