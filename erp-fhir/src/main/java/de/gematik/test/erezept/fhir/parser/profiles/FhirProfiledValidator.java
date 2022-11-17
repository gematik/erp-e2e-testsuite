/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.parser.profiles;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport;
import ca.uhn.fhir.context.support.IValidationSupport;
import ca.uhn.fhir.parser.StrictErrorHandler;
import ca.uhn.fhir.validation.FhirValidator;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.ErrorMessageFilter;
import de.gematik.test.erezept.fhir.parser.profiles.cfg.ParserConfigurations;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.SnapshotGeneratingValidationSupport;
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain;
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator;

@Slf4j
public class FhirProfiledValidator {

  @Getter private final String name;
  private final FhirContext ctx;
  private final List<IValidationSupport> customProfileSupports;
  private final FhirValidator hapiValidator;

  public FhirProfiledValidator(
      ParserConfigurations.ProfileSettingConfig cfg,
      FhirContext ctx,
      List<IValidationSupport> customProfileSupports) {
    log.debug(
        format(
            "Instantiate Parser ''{0}'' with support for \n\t{1}",
            cfg.getName(),
            cfg.getProfiles().stream()
                .map(p -> p.getVersionedProfile().toString())
                .collect(Collectors.joining("\n\t"))));
    this.name = cfg.getName();
    this.ctx = ctx;
    ctx.setParserErrorHandler(new StrictErrorHandler());
    this.customProfileSupports = customProfileSupports;
    this.hapiValidator = ctx.newValidator();

    // create support chain for validation
    // create support validators for custom profiles
    val validationSupports = new ArrayList<>(customProfileSupports);
    validationSupports.add(ctx.getValidationSupport());
    validationSupports.add(new DefaultProfileValidationSupport(ctx));
    validationSupports.add(new InMemoryTerminologyServerValidationSupport(ctx));
    validationSupports.add(new SnapshotGeneratingValidationSupport(ctx));
    validationSupports.add(
        new IgnoreMissingValueSetValidationSupport(
            ctx,
            List.of(
                "http://fhir.de/CodeSystem/ask",
                "http://fhir.de/CodeSystem/ifa/pzn"))); // these CodeSystems need to be ignored and
    // should be configurable in the future!

    // configure the HAPI FhirParser
    val fiv = new FhirInstanceValidator(ctx);
    val validationSupportChain =
        new ValidationSupportChain(validationSupports.toArray(IValidationSupport[]::new));

    fiv.setValidationSupport(validationSupportChain);
    fiv.setErrorForUnknownProfiles(true);
    fiv.setNoExtensibleWarnings(true);
    fiv.setAnyExtensionsAllowed(false);

    hapiValidator.registerValidatorModule(fiv);
    hapiValidator.registerValidatorModule(new ErrorMessageFilter());
  }

  public boolean doesSupport(@NonNull String url) {
    var ret = false;

    for (val cps : this.customProfileSupports) {
      val sdef = cps.fetchStructureDefinition(url);
      if (sdef != null) {
        ret = true;
        break;
      }
    }

    return ret;
  }

  /**
   * Check beforehand if the given content is valid
   *
   * @param content to be validated
   * @return true if content represents a valid FHIR-Resource and false otherwise
   */
  public ValidationResult validate(final String content) {
    ValidationResult ret;
    try {
      synchronized (this.hapiValidator) {
        ret = this.hapiValidator.validateWithResult(content);
      }
    } catch (Exception e) {
      /*
      some sort of error led to an Exception: handle this case via ValidationResult=ERROR
       */
      log.error("Error while validating FHIR content");
      val svm = new SingleValidationMessage();
      svm.setMessage(e.getMessage());
      svm.setSeverity(ResultSeverityEnum.ERROR);
      ret = new ValidationResult(ctx, List.of(svm));
    }

    if (!ret.isSuccessful()) {
      log.trace(
          format(
              "Validation unsuccessful with parser ''{0}'' producing {1} ValidationMessages",
              this.getName(), ret.getMessages().size()));
    }

    return ret;
  }

  public boolean isValid(final String content) {
    return this.validate(content).isSuccessful();
  }
}
