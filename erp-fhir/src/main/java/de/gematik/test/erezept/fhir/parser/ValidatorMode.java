package de.gematik.test.erezept.fhir.parser;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.annotation.JsonCreator;
import de.gematik.test.erezept.fhir.exceptions.FhirValidatorException;
import lombok.val;

import java.util.Arrays;

import static java.text.MessageFormat.format;

public enum ValidatorMode {
  NORMAL,
  STRICT,
  PEDANTIC;

  public static final String ENV_TOGGLE = "ERP_FHIR_VALIDATOR_MODE";
  public static final String SYS_PROP_TOGGLE = "erp.fhir.validator.mode";

  public ValidationResult adjustResult(ValidationResult result) {
    ValidationResult adjusted = result;
    if (this != NORMAL) {
      val strict =
          result.getMessages().stream()
              .map(this::liftSeverity)
              .toList();
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
