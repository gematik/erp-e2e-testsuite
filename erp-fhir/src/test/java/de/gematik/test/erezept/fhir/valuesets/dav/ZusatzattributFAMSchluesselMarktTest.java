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

package de.gematik.test.erezept.fhir.valuesets.dav;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import org.junit.jupiter.api.Test;

class ZusatzattributFAMSchluesselMarktTest {

  @Test
  void testFromCode() {
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.NICHT_BETROFFEN,
        ZusatzattributFAMSchluesselMarkt.fromCode("0"));
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.GENERIKA, ZusatzattributFAMSchluesselMarkt.fromCode("1"));
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.SOLITAER, ZusatzattributFAMSchluesselMarkt.fromCode("2"));
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.MEHRFACHVERTRIEB,
        ZusatzattributFAMSchluesselMarkt.fromCode("3"));
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.AUT_IDEM, ZusatzattributFAMSchluesselMarkt.fromCode("4"));
    assertEquals(
        ZusatzattributFAMSchluesselMarkt.SUBSTITUTIONS_AUSSCHLUSS,
        ZusatzattributFAMSchluesselMarkt.fromCode("5"));
  }

  @Test
  void testInvalidValueSetException() {
    assertThrows(
        InvalidValueSetException.class, () -> ZusatzattributFAMSchluesselMarkt.fromCode("6"));
  }
}
