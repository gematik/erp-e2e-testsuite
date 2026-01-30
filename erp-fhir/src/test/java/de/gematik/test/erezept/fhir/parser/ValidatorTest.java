/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.fhir.parser;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.io.File;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

@Slf4j
class ValidatorTest extends ErpFhirParsingTest {

  @ParameterizedTest
  @MethodSource
  void shouldPassValidResources(File file) {
    ValidatorUtil.validateFile(parser, file, Assertions::assertTrue, true);
  }

  static Stream<Arguments> shouldPassValidResources() {
    return ResourceLoader.getResourceDirectoryStructure("fhir/valid/", true).stream()
        .filter(File::isFile)
        .filter(
            f ->
                !f.getAbsolutePath()
                    .contains("epa")) // TODO: why do we need validating epa resources?
        .map(Arguments::of);
  }

  @ParameterizedTest
  @MethodSource
  void shouldDetectInvalidResources(File invalidResource) {
    ValidatorUtil.validateFile(parser, invalidResource, Assertions::assertFalse, false);
  }

  static Stream<Arguments> shouldDetectInvalidResources() {
    return ResourceLoader.getResourceDirectoryStructure("fhir/invalid", true).stream()
        .filter(File::isFile)
        .map(Arguments::arguments);
  }

  @ParameterizedTest(name = "Validate Mixed Version Bundles with {0}")
  @EnumSource(
      value = ValidatorMode.class,
      names = {"PEDANTIC", "STRICT"})
  @ClearSystemProperty(key = ValidatorMode.SYS_PROP_TOGGLE)
  @Disabled("Validator-Mode not yet implemented")
  void shouldValidateMixedVersionBundlesWithStrictPedanticValidation(ValidatorMode mode) {
    System.setProperty(ValidatorMode.SYS_PROP_TOGGLE, mode.name());
    val pedanticFhir = new FhirParser();
    val mixedBundleResources =
        ResourceLoader.getResourceDirectoryStructure("fhir/valid/mixed_bundles", true);
    ValidatorUtil.validateFiles(pedanticFhir, mixedBundleResources, Assertions::assertTrue, false);
  }

  @ParameterizedTest(name = "Validate Mixed Version Bundle {0} with {1}")
  @MethodSource
  void shouldValidateMixedVersionBundlesWithEachValidator(File input, ValidatorType validatorType) {
    val fhir = new FhirParser(validatorType);
    ValidatorUtil.validateFile(fhir, input, Assertions::assertTrue, true);
  }

  static Stream<Arguments> shouldValidateMixedVersionBundlesWithEachValidator() {
    val files =
        ResourceLoader.getResourceDirectoryStructure("fhir/valid/collection_bundles", true).stream()
            .toList();
    return Stream.of(ValidatorType.values())
        .filter(vt -> vt != ValidatorType.REF_VAL)
        .flatMap(vt -> files.stream().map(f -> Arguments.of(f, vt)));
  }

  @ParameterizedTest
  @EnumSource(ValidatorType.class)
  void shouldSmokeTestEachValidator(ValidatorType validatorType) {
    val content = ResourceLoader.getFileFromResource("fhir/valid/kbv/1.3.1/Beispiel_1_PZN.xml");
    val validator = assertDoesNotThrow(() -> new FhirParser(validatorType));
    ValidatorUtil.validateFile(validator, content, Assertions::assertTrue, true);
  }
}
