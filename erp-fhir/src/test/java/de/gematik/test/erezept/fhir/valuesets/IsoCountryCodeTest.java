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

import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

class IsoCountryCodeTest {

  @Test
  void testSomeEnumValues() {
    assertNotNull(IsoCountryCode.fromCode("DE"));
    assertNotNull(IsoCountryCode.fromCode("FR"));
    assertNotNull(IsoCountryCode.fromCode("IT"));
    assertNotNull(IsoCountryCode.fromCode("ES"));
    assertNotNull(IsoCountryCode.fromCode("AT"));
  }

  @Test
  void codeSystemShouldWork() {
    // Test, ob das Code-System korrekt ist
    assertEquals(CommonCodeSystem.ISO_3166, IsoCountryCode.DE.getCodeSystem());
  }

  @Test
  void codeAndDisplayShouldWorkCorrect() {
    IsoCountryCode germany = IsoCountryCode.DE;
    assertEquals("DE", germany.getCode());
    assertEquals("Germany", germany.getDisplay());
  }

  @Test
  void shouldBuildAsExtCorrect() {
    IsoCountryCode germany = IsoCountryCode.DE;
    val isoCCExtension = germany.asExtension();
    assertEquals(germany.getCode(), ((Coding) isoCCExtension.getValue()).getCode());
    assertEquals(germany.getCodeSystem().getCanonicalUrl(), isoCCExtension.getUrl());
  }
}
