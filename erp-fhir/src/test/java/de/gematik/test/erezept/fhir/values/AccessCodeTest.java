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
 */

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Identifier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class AccessCodeTest {

  private static final String STRONG_ACCESS_CODE_VALUE =
      "c3830cacdc32d8b521dbfc02e5f7102f879f53206b5fca1d80fee3bda969ce4d";

  @Test
  void shouldFailOnInvalidAccessCode() {
    val accessCode = AccessCode.fromString("ffae");
    assertFalse(accessCode.isValid());
  }

  @Test
  void shouldCheckValidAccessCode() {
    val accessCode = new AccessCode(STRONG_ACCESS_CODE_VALUE);
    assertTrue(accessCode.isValid());
  }

  @Test
  void shouldGenerateValidRandomAccessCodes() {
    val accessCode = AccessCode.random();
    assertTrue(accessCode.isValid());
  }

  @ParameterizedTest
  @EnumSource(
      value = ErpWorkflowNamingSystem.class,
      names = {"ACCESS_CODE_121", "ACCESS_CODE"})
  void shouldDetectAccessCodes(ErpWorkflowNamingSystem ns) {
    val identifier = new Identifier();
    identifier.setSystem(ns.getCanonicalUrl()).setValue(STRONG_ACCESS_CODE_VALUE);
    assertTrue(AccessCode.isAccessCode(identifier));
  }
}
