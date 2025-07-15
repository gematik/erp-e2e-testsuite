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

package de.gematik.test.erezept.eml.fhir.valuesets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import lombok.val;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class EpaDrugCategoryTest {

  @ParameterizedTest
  @EnumSource(EpaDrugCategory.class)
  void shouldHaveCodeDisplayAndDef(EpaDrugCategory cat) {
    val code = assertDoesNotThrow(cat::getCode);
    assertFalse(code.isEmpty());

    val display = assertDoesNotThrow(cat::getDisplay);
    assertFalse(display.isEmpty());

    val system = assertDoesNotThrow(cat::getCodeSystem);
    assertNotNull(system);
  }

  @ParameterizedTest
  @EnumSource(EpaDrugCategory.class)
  void shouldDecodeFromCode(EpaDrugCategory cat) {
    val cat2 = assertDoesNotThrow(() -> EpaDrugCategory.fromCode(cat.getCode()));
    assertEquals(cat, cat2);
  }

  @ParameterizedTest
  @EnumSource(EpaDrugCategory.class)
  void shouldEncodeAsExtension(EpaDrugCategory cat) {
    val ext = assertDoesNotThrow(cat::asExtension);
    assertEquals(cat.getCode(), ext.getValue().castToCoding(ext.getValue()).getCode());
    assertEquals(EpaMedicationStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl(), ext.getUrl());
  }
}
