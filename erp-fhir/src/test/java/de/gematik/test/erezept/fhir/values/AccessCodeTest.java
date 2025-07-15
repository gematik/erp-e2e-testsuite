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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.profiles.systems.ErpWorkflowNamingSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;

class AccessCodeTest {

  private static final String STRONG_ACCESS_CODE_VALUE =
      "c3830cacdc32d8b521dbfc02e5f7102f879f53206b5fca1d80fee3bda969ce4d";

  @Test
  void shouldFailOnInvalidAccessCode() {
    val accessCode = AccessCode.from("ffae");
    assertFalse(accessCode.isValid());
  }

  @Test
  void shouldCheckValidAccessCode() {
    val accessCode = AccessCode.from(STRONG_ACCESS_CODE_VALUE);
    assertTrue(accessCode.isValid());
  }

  @Test
  void shouldGenerateValidRandomAccessCodes() {
    val accessCode = AccessCode.random();
    assertTrue(accessCode.isValid());
  }

  @Test
  void shouldDetectAccessCodes() {
    val identifier =
        new Identifier()
            .setSystem(ErpWorkflowNamingSystem.ACCESS_CODE.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    assertTrue(AccessCode.isAccessCode(identifier));
  }

  @Test
  void shouldExtractFromIdentifier() {
    val identifier =
        new Identifier()
            .setSystem(ErpWorkflowNamingSystem.ACCESS_CODE.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    val ac = assertDoesNotThrow(() -> AccessCode.from(identifier));
    assertEquals(STRONG_ACCESS_CODE_VALUE, ac.getValue());
  }

  @Test
  void shouldThrowOnExtractingFromInvalidIdentifier() {
    val identifier =
        new Identifier()
            .setSystem(ErpWorkflowNamingSystem.SECRET.getCanonicalUrl())
            .setValue(STRONG_ACCESS_CODE_VALUE);
    assertFalse(AccessCode.isAccessCode(identifier));
    assertThrows(BuilderException.class, () -> AccessCode.from(identifier));
  }
}
