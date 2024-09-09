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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.io.File;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

@Slf4j
class ValidatorTest extends ParsingTest {

  @ParameterizedTest
  @MethodSource
  void shouldFailOnValidateGarbage(String content) {
    val vr = parser.validate(content);
    val severity = vr.getMessages().get(0).getSeverity();
    assertFalse(vr.isSuccessful());
    assertFalse(vr.getMessages().isEmpty());
    assertThat(severity, anyOf(is(ResultSeverityEnum.ERROR), is(ResultSeverityEnum.FATAL)));
  }

  static Stream<Arguments> shouldFailOnValidateGarbage() {
    return Stream.of(
            "Garbage content is definitely no valid FHIR content",
            "<xml>invalid</xml>",
            "{content: \"invalid\"}",
            "{\"content}\": \"invalid\"}",
            "")
        .map(Arguments::of);
  }

  @Test
  void shouldFailOnNullContent() {
    String content = null;
    assertThrows(NullPointerException.class, () -> parser.validate(content)); // intentionally null
  }

  @ParameterizedTest
  @MethodSource
  void shouldPassValidResources(File file) {
    ValidatorUtil.validateFile(parser, file, Assertions::assertTrue, false);
  }

  static Stream<Arguments> shouldPassValidResources() {
    return ResourceLoader.getResourceDirectoryStructure("fhir/valid", true).stream()
        .filter(File::isFile)
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
        .filter(it -> !it.getName().equals("no_profile_value_bundle.xml")) // produces NPE in HAPI
        .map(Arguments::arguments);
  }

  @ParameterizedTest(name = "Validate Mixed Version Bundles with {0}")
  @EnumSource(
      value = ValidatorMode.class,
      names = {"PEDANTIC", "STRICT"})
  @ClearSystemProperty(key = ValidatorMode.SYS_PROP_TOGGLE)
  void shouldValidateMixedVersionBundlesWithStrictPedanticValidation(ValidatorMode mode) {
    System.setProperty(ValidatorMode.SYS_PROP_TOGGLE, mode.name());
    val pedanticFhir = new FhirParser();
    val mixedBundleResources =
        ResourceLoader.getResourceDirectoryStructure("fhir/valid/mixed_bundles", true);
    ValidatorUtil.validateFiles(pedanticFhir, mixedBundleResources, Assertions::assertTrue, false);
  }
}
