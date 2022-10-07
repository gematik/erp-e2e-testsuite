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

package de.gematik.test.erezept.fhir.values;

import static org.junit.jupiter.api.Assertions.*;

import lombok.val;
import org.junit.jupiter.api.Test;

class AccessCodeTest {

  private static final String STRONG_ACCESS_CODE_VALUE =
      "c3830cacdc32d8b521dbfc02e5f7102f879f53206b5fca1d80fee3bda969ce4d";

  @Test
  void shouldFailOnInvalidAccessCode() {
    val accessCode = new AccessCode("ffae");
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
}
