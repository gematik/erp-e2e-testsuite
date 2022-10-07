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

package de.gematik.test.erezept.fhir.util;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.util.stream.Collectors;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

public class ValidatorUtil {

  private ValidatorUtil() {
    throw new AssertionError();
  }

  public static ValidationResult encodeAndValidate(FhirParser parser, Resource resource) {
    return encodeAndValidate(parser, resource, false);
  }

  public static ValidationResult encodeAndValidate(
      FhirParser parser, Resource resource, boolean printEncoded) {
    // encode and check if encoded content is valid
    val encoded = parser.encode(resource, EncodingType.XML, printEncoded);

    if (printEncoded) System.out.println("\n\n##########\n" + encoded + "\n##########");

    val result = parser.validate(encoded);
    printValidationResult(result);

    return result;
  }

  public static void printValidationResult(final ValidationResult result) {
    if (!result.isSuccessful()) {
      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .map(m -> "(" + m.getLocationString() + ") " + m.getMessage())
              .collect(Collectors.joining("\n"));
      System.out.println(format("Errors: {0}\n{1}", result.getMessages().size(), r));
    }
  }
}
