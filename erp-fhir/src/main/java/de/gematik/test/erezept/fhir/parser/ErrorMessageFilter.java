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

package de.gematik.test.erezept.fhir.parser;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.IValidationContext;
import ca.uhn.fhir.validation.IValidatorModule;
import ca.uhn.fhir.validation.SingleValidationMessage;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.instance.model.api.IBaseResource;

/**
 * This module filters error messages which we know of and which are required to be ignored due to
 * e.g. inconsistencies within the profiles.
 *
 * <p>Note: It might be necessary to review the list of {@link ErrorMessageFilter#IGNORED_MESSAGES}
 * regularly and check if we can achieve an empty list someday in the future.
 */
@Slf4j
public class ErrorMessageFilter implements IValidatorModule {

  private static final List<String> IGNORED_MESSAGES =
      List.of(
          "^Bundle entry missing fullUrl",
          "^Relative Reference appears inside Bundle whose entry is missing a fullUrl",
          "^docBundle-1: 'All referenced Resources must be contained in the Bundle'.*",
          "^Entry  isn't reachable by traversing from first Bundle entry",
          "^Can't find '\\w*?' in the bundle.*",
          "^Except for transactions and batches, each entry in a Bundle must have a fullUrl which is the identity of the resource in the entry.*",

          // The parser does not recognise JWS although sigFormat is given as application/jose
          "^The value .[(]snip[)]. is not a valid Base64 value",

          // Relative references are valid with ResourceProfile/id. Additional information with URL
          // parameter are not permitted
          "^Relative URLs must be of the format \\[ResourceName\\]\\/\\[id\\].*",

          // this error message is produced only on JSON, but accepted in XML: this most probably
          // roots from { and } chars
          "^UCUM Codes that contain human readable annotations like \\{.*\\} can be misleading.*",

          // dom-6 is just a Guideline: https://www.hl7.org/fhir/domainresource-definitions.html
          "^dom-6: Rule 'A resource should have narrative for robust management'.*",

          // this issue occurs only on round-trips (xml -> json): XML-comments <!-- ... --> are
          // converted to fhir_comments ["..."]
          "^Unrecognised property '@fhir_comments'.*",
          "^Nicht erkannte Property '@fhir_comments'");

  @Override
  public void validateResource(final IValidationContext<IBaseResource> iValidationContext) {
    val messages = iValidationContext.getMessages();
    messages.removeIf(ErrorMessageFilter::ignoreMessage);
  }

  private static boolean ignoreMessage(final SingleValidationMessage validationMessage) {
    val message = validationMessage.getMessage();
    val ignore = IGNORED_MESSAGES.stream().anyMatch(message::matches);
    if (ignore) {
      log.trace(format("Ignoring parser error message: {0}", validationMessage));
    }

    return ignore;
  }
}
