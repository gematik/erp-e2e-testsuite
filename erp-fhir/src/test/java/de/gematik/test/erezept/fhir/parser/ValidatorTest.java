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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junitpioneer.jupiter.ClearSystemProperty;

@Slf4j
class ValidatorTest extends ParsingTest {

  @Test
  void shouldFailOnValidateGarbage() {
    val invalidContents =
        List.of(
            "Garbage content is definitely no valid FHIR content",
            "<xml>invalid</xml>",
            "{content: \"invalid\"}",
            "{\"content}\": \"invalid\"}",
            "");

    invalidContents.forEach(
        content -> {
          val vr = parser.validate(content);
          val severity = vr.getMessages().get(0).getSeverity();
          assertFalse(vr.isSuccessful());
          assertFalse(vr.getMessages().isEmpty());
          assertThat(severity, anyOf(is(ResultSeverityEnum.ERROR), is(ResultSeverityEnum.FATAL)));
        });
  }

  @Test
  void shouldFailOnNullContent() {
    String content = null;
    assertThrows(NullPointerException.class, () -> parser.validate(content)); // intentionally null
  }

  @Test
  void shouldPassValidErpResources() {
    val validResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/erp", true);
    ValidatorUtil.validateFiles(parser, validResources, Assertions::assertTrue, true);
  }

  @Test
  void shouldPassOfficialKbvBundleResources() {
    val kbvBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv", true);
    ValidatorUtil.validateFiles(parser, kbvBundleResources, Assertions::assertTrue, false);
  }

  @Test
  void shouldPassOfficialDavBundleResources() {
    val davBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/dav", true);
    ValidatorUtil.validateFiles(parser, davBundleResources, Assertions::assertTrue, false);
  }

  @Test
  void shouldFailInvalidErpResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/erp", true);
    ValidatorUtil.validateFiles(parser, invalidResources, Assertions::assertFalse, false);
  }

  @Test
  void shouldFailInvalidKbvResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/kbv", true);
    ValidatorUtil.validateFiles(parser, invalidResources, Assertions::assertFalse, false);
  }

  @Test
  void shouldValidateMixedVersionBundles() {
    val mixedBundleResources =
        ResourceUtils.getResourceFilesInDirectory("fhir/valid/mixed_bundles", true);
    ValidatorUtil.validateFiles(parser, mixedBundleResources, Assertions::assertTrue, false);
  }

  @ParameterizedTest(name = "Validate Mixed Version Bundles with {0}")
  @EnumSource(value = ValidatorMode.class, names = {"PEDANTIC", "STRICT"})
  @ClearSystemProperty(key = ValidatorMode.SYS_PROP_TOGGLE)
  void shouldValidateMixedVersionBundlesWithStrictPedanticValidation(ValidatorMode mode) {
    System.setProperty(ValidatorMode.SYS_PROP_TOGGLE, mode.name());
    val pedanticFhir = new FhirParser();
    val mixedBundleResources =
            ResourceUtils.getResourceFilesInDirectory("fhir/valid/mixed_bundles", true);
    ValidatorUtil.validateFiles(pedanticFhir, mixedBundleResources, Assertions::assertTrue, true);
  }
}
