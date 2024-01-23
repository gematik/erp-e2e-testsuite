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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.*;

import ca.uhn.fhir.validation.*;
import java.util.*;
import javax.annotation.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.hl7.fhir.instance.model.api.*;

/**
 * This module filters error messages which we know of and which are required to be ignored due to
 * e.g. inconsistencies within the profiles.
 *
 * <p>Note: It might be necessary to review the list of {@link
 * ErrorMessageFilter#DEFAULT_IGNORED_MESSAGES} regularly and check if we can achieve an empty list
 * someday in the future.
 */
@Slf4j
public class ErrorMessageFilter implements IValidatorModule {

  private static final List<String> DEFAULT_IGNORED_MESSAGES =
      /* Note: comments in json are detected as errors but should be technically valid
      for now we haven't seen @fhir_comments in the wild but only in some rare examples like
      ParserTest.roundtripAllKbvBundles() */
      List.of(
          "^Unrecognised property '@fhir_comments'.*",
          "^This module has no support for code system.*'");

  private final List<String> ignoreMessages;

  public ErrorMessageFilter(@Nullable List<String> ignoreMessages) {
    if (ignoreMessages == null) {
      this.ignoreMessages = DEFAULT_IGNORED_MESSAGES;
    } else {
      this.ignoreMessages = ignoreMessages;
      this.ignoreMessages.addAll(DEFAULT_IGNORED_MESSAGES);
    }
  }

  private boolean ignoreMessage(final SingleValidationMessage validationMessage) {
    val message = validationMessage.getMessage();
    val ignore = ignoreMessages.stream().anyMatch(message::matches);
    if (ignore) {
      log.trace(
          format(
              "Ignored validation message: ''{0}'' at {1}",
              message, validationMessage.getLocationString()));
    }

    return ignore;
  }

  @Override
  public void validateResource(final IValidationContext<IBaseResource> iValidationContext) {
    // simply remove all ignored error messages!!
    iValidationContext.getMessages().removeIf(this::ignoreMessage);
  }
}
