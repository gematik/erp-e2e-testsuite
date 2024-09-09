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

package de.gematik.test.erezept.fhir.testutil;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.parser.EncodingType;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public class ValidatorUtil {

  private ValidatorUtil() {
    throw new AssertionError();
  }

  public static void validateFiles(
      FhirParser parser,
      List<File> files,
      Consumer<Boolean> validationAssertion,
      boolean printResult) {
    files.forEach(file -> validateFile(parser, file, validationAssertion, printResult));
  }

  public static void validateFile(
      FhirParser parser, File file, Consumer<Boolean> validationAssertion, boolean printResult) {

    log.info(format("Validate {0}", file.getName()));
    val content = ResourceLoader.readString(file);
    val vr = parser.validate(content);
    if (printResult) {
      printValidationResult(vr);
    }
    validationAssertion.accept(vr.isSuccessful());
  }

  public static ValidationResult encodeAndValidate(FhirParser parser, Resource resource) {
    return encodeAndValidate(parser, resource, false);
  }

  public static ValidationResult encodeAndValidate(
      FhirParser parser, Resource resource, EncodingType encodingType) {
    return encodeAndValidate(parser, resource, encodingType, false);
  }

  public static ValidationResult encodeAndValidate(
      FhirParser parser, Resource resource, boolean printEncoded) {
    return encodeAndValidate(parser, resource, EncodingType.XML, printEncoded);
  }

  public static ValidationResult encodeAndValidate(
      FhirParser parser, Resource resource, EncodingType encodingType, boolean printEncoded) {
    return encodeAndValidate(parser, resource, encodingType, printEncoded, false);
  }

  public static ValidationResult encodeAndValidate(
      FhirParser parser,
      Resource resource,
      EncodingType encodingType,
      boolean printEncoded,
      boolean prettyPrint) {
    // encode and check if encoded content is valid
    val encoded = parser.encode(resource, encodingType, prettyPrint);

    if (printEncoded) log.info("\n\n##########\n" + encoded + "\n##########");

    val result = parser.validate(encoded);
    printValidationResult(result);

    return result;
  }

  public static void printValidationResult(final ValidationResult result) {
    printValidationResult(result, m -> !m.getSeverity().equals(ResultSeverityEnum.INFORMATION));
  }

  public static void printValidationResult(
      final ValidationResult result, Predicate<SingleValidationMessage> messageFilter) {
    if (!result.isSuccessful()) {
      // give me some hints if the encoded result is invalid
      val r =
          result.getMessages().stream()
              .filter(messageFilter)
              .map(
                  m ->
                      format(
                          "[{0} in Line {3} at {1}]: {2}",
                          m.getSeverity(),
                          m.getLocationString(),
                          m.getMessage(),
                          m.getLocationLine()))
              .collect(Collectors.joining("\n\t"));
      log.warn(
          format(
              "--- Found Validation Messages after validation: {0} ---\n\t{1}\n------",
              result.getMessages().stream().filter(messageFilter).count(), r));
    }
  }
}
