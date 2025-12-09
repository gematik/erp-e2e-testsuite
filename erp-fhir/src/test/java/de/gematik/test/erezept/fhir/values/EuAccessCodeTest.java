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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.profiles.systems.GemErpEuNamingSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class EuEuAccessCodeTest {

  private static final String STRONG_ACCESS_CODE_VALUE = "a1B2d3";

  @Test
  void shouldFailOnInvalidEuAccessCode() {
    val accessCode = EuAccessCode.from("ffae");
    assertFalse(accessCode.isValid());
  }

  @Test
  void shouldCheckValidEuAccessCode() {
    val accessCode = EuAccessCode.from(STRONG_ACCESS_CODE_VALUE);
    assertTrue(accessCode.isValid());
  }

  @Test
  void shouldGenerateValidRandomEuAccessCodes() {
    val accessCode = EuAccessCode.random();
    assertTrue(accessCode.isValid());
  }

  @ParameterizedTest
  @ValueSource(strings = {"toLongg", "short", "1", "12345", "1234567", "123 456", "**//§$"})
  void shouldThrowWilesSettingWrongAccessCode(String euAccessCode) {
    val testCode = EuAccessCode.from(euAccessCode);
    assertFalse(testCode.isValid());
  }

  @Test
  void shouldDetectEuAccessCodes() {
    val identifier =
        new Identifier()
            .setSystem(GemErpEuNamingSystem.ACCESS_CODE.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    assertTrue(EuAccessCode.isAccessCode(identifier));
  }

  @Test
  void shouldExtractFromIdentifier() {
    val identifier =
        new Identifier()
            .setSystem(GemErpEuNamingSystem.ACCESS_CODE.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    val ac = assertDoesNotThrow(() -> EuAccessCode.from(identifier));
    assertEquals(STRONG_ACCESS_CODE_VALUE, ac.getValue());
  }

  @Test
  void shouldThrowOnExtractingFromInvalidIdentifier() {
    val identifier =
        new Identifier()
            .setSystem(ErpWorkflowNamingSystem.SECRET.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    assertFalse(EuAccessCode.isAccessCode(identifier));
    assertThrows(BuilderException.class, () -> EuAccessCode.from(identifier));
  }
}
