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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.validation.ResultSeverityEnum;
import ca.uhn.fhir.validation.SingleValidationMessage;
import ca.uhn.fhir.validation.ValidationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.ValueInstantiationException;
import de.gematik.bbriccs.fhir.conf.exceptions.FhirConfigurationException;
import java.util.List;
import lombok.Data;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.ClearSystemProperty;

class ValidatorModeTest {

  private SingleValidationMessage createMessage(ResultSeverityEnum severity, String message) {
    val svm = new SingleValidationMessage();
    svm.setSeverity(severity);
    svm.setMessage(message);
    return svm;
  }

  private void assertAmountOf(ValidationResult result, ResultSeverityEnum severity, int num) {
    val filtered =
        result.getMessages().stream().filter(svm -> svm.getSeverity().equals(severity)).toList();
    assertEquals(num, filtered.size());
  }

  @Test
  void shouldDoNothingOnNormalMode() {
    val ctx = mock(FhirContext.class);
    val messages =
        List.of(
            createMessage(ResultSeverityEnum.INFORMATION, "info"),
            createMessage(ResultSeverityEnum.WARNING, "warning"),
            createMessage(ResultSeverityEnum.ERROR, "error"));
    val vr = new ValidationResult(ctx, messages);
    assertFalse(vr.isSuccessful());
    val adjusted = ValidatorMode.NORMAL.adjustResult(vr);
    assertEquals(vr, adjusted); // return the same object!
    assertAmountOf(adjusted, ResultSeverityEnum.INFORMATION, 1);
    assertAmountOf(adjusted, ResultSeverityEnum.WARNING, 1);
    assertAmountOf(adjusted, ResultSeverityEnum.ERROR, 1);
    assertAmountOf(adjusted, ResultSeverityEnum.FATAL, 0);
  }

  @Test
  void shouldLiftOnStrict() {
    val ctx = mock(FhirContext.class);
    val messages =
        List.of(
            createMessage(ResultSeverityEnum.INFORMATION, "info"),
            createMessage(ResultSeverityEnum.WARNING, "warning"),
            createMessage(ResultSeverityEnum.ERROR, "error"),
            createMessage(ResultSeverityEnum.FATAL, "fatal"));
    val vr = new ValidationResult(ctx, messages);
    assertFalse(vr.isSuccessful());
    val adjusted = ValidatorMode.STRICT.adjustResult(vr);
    assertNotEquals(vr, adjusted); // return a new object with adjusted messages
    assertFalse(adjusted.isSuccessful());

    assertAmountOf(adjusted, ResultSeverityEnum.INFORMATION, 0);
    assertAmountOf(adjusted, ResultSeverityEnum.WARNING, 1);
    assertAmountOf(adjusted, ResultSeverityEnum.ERROR, 2);
    assertAmountOf(adjusted, ResultSeverityEnum.FATAL, 1); // don't touch fatal results
  }

  @Test
  void shouldLiftOnPedantic() {
    val ctx = mock(FhirContext.class);
    val messages =
        List.of(
            createMessage(ResultSeverityEnum.INFORMATION, "info"),
            createMessage(ResultSeverityEnum.WARNING, "warning"),
            createMessage(ResultSeverityEnum.ERROR, "error"),
            createMessage(ResultSeverityEnum.FATAL, "fatal"));
    val vr = new ValidationResult(ctx, messages);
    assertFalse(vr.isSuccessful());
    val adjusted = ValidatorMode.PEDANTIC.adjustResult(vr);
    assertNotEquals(vr, adjusted); // return a new object with adjusted messages
    assertFalse(adjusted.isSuccessful());

    assertAmountOf(adjusted, ResultSeverityEnum.INFORMATION, 0);
    assertAmountOf(adjusted, ResultSeverityEnum.WARNING, 0);
    assertAmountOf(adjusted, ResultSeverityEnum.ERROR, 3);
    assertAmountOf(adjusted, ResultSeverityEnum.FATAL, 1); // don't touch fatal results
  }

  @Test
  @ClearSystemProperty(key = ValidatorMode.SYS_PROP_TOGGLE)
  void shouldThrowOnInvalidValidatorModeConfiguration() {
    System.setProperty(ValidatorMode.SYS_PROP_TOGGLE, "hello");
    assertThrows(FhirConfigurationException.class, ValidatorMode::getDefault);
  }

  @Test
  void shouldThrowOnInvalidNameValue() {
    val om = new ObjectMapper();
    val input = "{\"mode\": \"hello\"}";
    val ex =
        assertThrows(
            ValueInstantiationException.class,
            () -> om.readValue(input, MockValidatorConfig.class));
    assertEquals(FhirConfigurationException.class, ex.getCause().getClass());
  }

  @Test
  void shouldGetNormalModeOnDefault() {
    val mode = ValidatorMode.getDefault();
    assertEquals(ValidatorMode.NORMAL, mode);
  }

  @Test
  @ClearSystemProperty(key = ValidatorMode.SYS_PROP_TOGGLE)
  void shouldGetNormalModeOnEmptyConfiguration() {
    System.setProperty(ValidatorMode.SYS_PROP_TOGGLE, "");
    val mode = ValidatorMode.getDefault();
    assertEquals(ValidatorMode.NORMAL, mode);
  }

  @Data
  private static class MockValidatorConfig {
    private ValidatorMode mode;
  }
}
