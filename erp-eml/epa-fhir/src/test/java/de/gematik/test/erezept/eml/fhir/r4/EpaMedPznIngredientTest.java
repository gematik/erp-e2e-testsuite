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

package de.gematik.test.erezept.eml.fhir.r4;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.testutil.EpaFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;

class EpaMedPznIngredientTest extends EpaFhirParsingTest {

  @Test
  void getPznShouldWork() {
    val med = EpaMedPznIngredientBuilder.builder().withPzn(PZN.from("123"), "displayName").build();
    assertEquals("123", med.getPzn().orElseThrow().getValue());
  }

  @Test
  void getPznShouldFail() {
    val med = EpaMedPznIngredientBuilder.builder().withPzn(PZN.from("123"), "displayName").build();
    assertNotEquals("alternativeCode", med.getPzn().orElseThrow().getValue());
  }
}
