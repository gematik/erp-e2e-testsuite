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

package de.gematik.test.erezept.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.gematik.bbriccs.fhir.coding.exceptions.InvalidValueSetException;
import lombok.val;
import org.junit.jupiter.api.Test;

class EuConsentTypeTest {

  @Test
  void shouldGetCodeSystemCorrect() {
    val expected = "https://gematik.de/fhir/erp-eu/CodeSystem/GEM_ERPEU_CS_ConsentType";
    assertEquals(expected, EuConsentType.EUDISPCONS.getCodeSystem().getCanonicalUrl());
  }

  @Test
  void shouldFailWileUsingFromCode() {
    val code = "1";
    assertThrows(InvalidValueSetException.class, () -> EuConsentType.fromCode(code));
  }

  @Test
  void shouldUsingFromCodeCorrect() {
    val code = EuConsentType.EUDISPCONS.getCode();
    assertEquals("EUDISPCONS", EuConsentType.fromCode(code).getCode());
  }
}
