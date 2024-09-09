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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
import java.util.Arrays;
import lombok.val;

public enum ValidatorMode {
  NORMAL,
  STRICT,
  PEDANTIC;

  public static final String ENV_TOGGLE = "ERP_FHIR_VALIDATOR_MODE";
  public static final String SYS_PROP_TOGGLE = "erp.fhir.validator.mode";

  public ValidationResult adjustResult(ValidationResult result) {
    ValidationResult adjusted = result;
    if (this != NORMAL) {
      val strict = result.getMessages().stream().map(this::liftSeverity).toList();
      adjusted = new ValidationResult(result.getContext(), strict);
    }

    return adjusted;
  }

  private SingleValidationMessage liftSeverity(SingleValidationMessage message) {
    val original = message.getSeverity();
    val shouldLift = original.ordinal() < ResultSeverityEnum.ERROR.ordinal();
    if (shouldLift && this.equals(STRICT)) {
      val stricter = ResultSeverityEnum.values()[original.ordinal() + 1];
      message.setSeverity(stricter);
    } else if (shouldLift && this.equals(PEDANTIC)) {
      message.setSeverity(ResultSeverityEnum.ERROR);
    }
    return message;
  }

  @JsonCreator
  private static ValidatorMode fromName(String value) {
    return Arrays.stream(ValidatorMode.values())
        .filter(it -> it.name().equalsIgnoreCase(value))
        .findFirst()
        .orElseThrow(
            () ->
                new FhirValidatorException(
                    format("Unable to set FHIR Validator Mode {0} from configuration", value)));
  }

  public static ValidatorMode getDefault() {
    val conf = System.getProperty(SYS_PROP_TOGGLE, System.getenv(ENV_TOGGLE));
    if (conf != null && !conf.isEmpty()) {
      return Arrays.stream(ValidatorMode.values())
          .filter(mode -> mode.name().equalsIgnoreCase(conf))
          .findFirst()
          .orElseThrow(
              () ->
                  new FhirValidatorException(
                      format(
                          "Unable to set FHIR Validator Mode {0} from SystemProperty/Environment",
                          conf)));
    } else {
      return NORMAL;
    }
  }
}
