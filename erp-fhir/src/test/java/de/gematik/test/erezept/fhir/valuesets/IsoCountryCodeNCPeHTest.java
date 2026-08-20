/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;

class IsoCountryCodeNCPeHTest {

  @Test
  void testSomeEnumValues() {
    assertNotNull(IsoCountryCodeNCPeH.fromCode("DE"));
    assertNotNull(IsoCountryCodeNCPeH.fromCode("FR"));
    assertNotNull(IsoCountryCodeNCPeH.fromCode("IT"));
    assertNotNull(IsoCountryCodeNCPeH.fromCode("ES"));
    assertNotNull(IsoCountryCodeNCPeH.fromCode("AT"));
  }

  @Test
  void codeSystemShouldWork() {
    // Test, ob das Code-System korrekt ist
    assertEquals(CommonCodeSystem.ISO_3166, IsoCountryCodeNCPeH.DE.getCodeSystem());
  }

  @Test
  void codeAndDisplayShouldWorkCorrect() {
    IsoCountryCodeNCPeH germany = IsoCountryCodeNCPeH.DE;
    assertEquals("DE", germany.getCode());
    assertEquals("Germany", germany.getDisplay());
  }

  @Test
  void shouldBuildAsExtCorrect() {
    IsoCountryCodeNCPeH germany = IsoCountryCodeNCPeH.DE;
    val isoCCExtension = germany.asExtension();
    assertEquals(germany.getCode(), ((Coding) isoCCExtension.getValue()).getCode());
    assertEquals(germany.getCodeSystem().getCanonicalUrl(), isoCCExtension.getUrl());
  }
}
