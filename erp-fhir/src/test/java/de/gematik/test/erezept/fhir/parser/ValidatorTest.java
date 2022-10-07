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

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import ca.uhn.fhir.validation.ResultSeverityEnum;
import de.gematik.test.erezept.fhir.util.ParsingTest;
import de.gematik.test.erezept.fhir.util.ResourceUtils;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    val vr = parser.validate(null);
    assertFalse(vr.isSuccessful());
    assertTrue(vr.getMessages().size() > 0);
    assertEquals(ResultSeverityEnum.ERROR, vr.getMessages().get(0).getSeverity());
  }

  @Test
  void shouldPassValidResources() {
    val validResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/erp");

    validResources.forEach(
        file -> {
          log.info("Validate: " + file.getName());
          val content = ResourceUtils.readFileFromResource(file);
          val vr = parser.validate(content);

          // give some help on debugging if still errors in "valid resources"
          if (!vr.isSuccessful()) {
            System.out.println("Errors: " + vr.getMessages().size() + " in File " + file.getName());
            vr.getMessages().forEach(System.out::println);
          }

          assertTrue(vr.isSuccessful());
        });
  }

  @Test
  void shouldPassOfficialKbvBundleResources() {
    val kbvBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv/bundle");

    kbvBundleResources.forEach(
        file -> {
          log.info("Validate: " + file.getName());
          val content = ResourceUtils.readFileFromResource(file);
          val vr = parser.validate(content);

          // give some help on debugging if still errors in "valid resources"
          if (!vr.isSuccessful()) {
            System.out.println("Errors: " + vr.getMessages().size() + " in File " + file.getName());
            vr.getMessages().forEach(System.out::println);
          }

          assertTrue(vr.isSuccessful());
        });
  }

  @Test
  void shouldPassOfficialAbdaBundleResources() {
    val kbvBundleResources = ResourceUtils.getResourceFilesInDirectory("fhir/valid/dav");

    kbvBundleResources.forEach(
        file -> {
          log.info("Validate: " + file.getName());
          val content = ResourceUtils.readFileFromResource(file);
          val vr = parser.validate(content);

          // give some help on debugging if still errors in "valid resources"
          if (!vr.isSuccessful()) {
            System.out.println("Errors: " + vr.getMessages().size() + " in File " + file.getName());
            vr.getMessages().forEach(System.out::println);
          }

          assertTrue(vr.isSuccessful());
        });
  }

  @Test
  void shouldPassOfficialKbvPatientResources() {
    val kbvPatients = ResourceUtils.getResourceFilesInDirectory("fhir/valid/kbv/patient");

    kbvPatients.stream()
        .map(ResourceUtils::readFileFromResource)
        .forEach(
            content -> {
              val vr = parser.validate(content);
              assertTrue(vr.isSuccessful());
            });
  }

  @Test
  void shouldFailInvalidErpResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/erp");

    invalidResources.stream()
        .map(ResourceUtils::readFileFromResource)
        .forEach(
            content -> {
              val vr = parser.validate(content);
              assertFalse(vr.isSuccessful());
            });
  }

  @Test
  void shouldFailInvalidKbvResources() {
    val invalidResources = ResourceUtils.getResourceFilesInDirectory("fhir/invalid/kbv");

    invalidResources.stream()
        .map(ResourceUtils::readFileFromResource)
        .forEach(
            content -> {
              val vr = parser.validate(content);
              assertFalse(vr.isSuccessful());
            });
  }
}
