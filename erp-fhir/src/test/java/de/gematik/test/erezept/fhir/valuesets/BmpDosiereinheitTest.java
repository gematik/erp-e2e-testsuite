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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class BmpDosiereinheitTest {

  @Test
  void testSomeEnumValues() {
    assertNotNull(BmpDosiereinheit.fromCode("m"));
    assertNotNull(BmpDosiereinheit.fromCode("5"));
    assertNotNull(BmpDosiereinheit.fromCode("#"));
  }

  @Test
  void codeSystemShouldWork() {
    assertNotEquals(BmpDosiereinheit.APPLIKATORFUELLUNG.getCode(), BmpDosiereinheit.MG.getCode());
  }

  @Test
  void codeAndDisplayShouldWorkCorrect() {
    de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit einheit =
        de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit.MG;
    assertEquals("v", einheit.getCode());
    assertEquals("mg", einheit.getDisplay());
  }

  @Test
  void shouldBuildAsExtCorrect() {
    de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit einheit =
        de.gematik.test.erezept.eml.fhir.valuesets.BmpDosiereinheit.MG;
    val ext = einheit.asExtension();
    assertEquals(einheit.getCode(), ((Coding) ext.getValue()).getCode());
    assertEquals(einheit.getCodeSystem().getCanonicalUrl(), ext.getUrl());
  }

  @ParameterizedTest
  @CsvSource({"'Stück', '1'", "Messbecher, 0", "Messlöffel, #", "cm, q", "Likörglas, i"})
  void shouldGetDisplay(String display, String code) {
    assertEquals(code, BmpDosiereinheit.fromDisplay(display).getCode());
  }
}
