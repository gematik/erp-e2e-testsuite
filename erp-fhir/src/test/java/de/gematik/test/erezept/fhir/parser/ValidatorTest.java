/*
 * Copyright (c) 2023 gematik GmbH
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
          assertTrue(vr.getMessages().size() > 0);
          assertThat(severity, anyOf(is(ResultSeverityEnum.ERROR), is(ResultSeverityEnum.FATAL)));
        });
  }

  @Test
  void shouldFailOnNullContent() {
    assertThrows(NullPointerException.class, () -> parser.validate(null)); // intentionally null
  }

  @Test
  void shouldPassValidErpResources() {
    val validResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/erp", true);
    ValidatorUtil.validateFiles(parser, validResources, Assertions::assertTrue, true);
  }

  @Test
  void shouldPassOfficialKbvBundleResources() {
    val kbvBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv", true);
    ValidatorUtil.validateFiles(parser, kbvBundleResources, Assertions::assertTrue, true);
  }

  @Test
  void shouldPassOfficialAbdaBundleResources() {
    val davBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/dav");
    ValidatorUtil.validateFiles(parser, davBundleResources, Assertions::assertTrue, true);
  }

  @Test
  void shouldFailInvalidErpResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/erp");
    ValidatorUtil.validateFiles(parser, invalidResources, Assertions::assertFalse, false);
  }

  @Test
  void shouldFailInvalidKbvResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/kbv");
    ValidatorUtil.validateFiles(parser, invalidResources, Assertions::assertFalse, false);
  }
}
