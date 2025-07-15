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

package de.gematik.test.erezept.fhir.builder.erp;

import static de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest.ERP_FHIR_PROFILES_TOGGLE;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;
import org.mockito.Mockito;

@SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.5.0")
class GemErpMedFreeTextFakerTest extends ErpFhirParsingTest {
  @Test
  void shouldBuildMedicationWithCodeText() {
    String codeText = "Test Code Text";
    val gemErpMedFreeText =
        GemErpMedicationFaker.forFreeTextMedication().withCodeText(codeText).fake();
    assertNotNull(gemErpMedFreeText);
    assertEquals(codeText, gemErpMedFreeText.getCode().getText());
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldBuildMedicationWithAllValues() {
    val codeText = "Test Code Text";
    val formText = "Test Form Text";
    val lotNumber = "Test Lot Number";
    val category = EpaDrugCategory.C_00;
    val version = ErpWorkflowVersion.V1_4;

    val gemErpMedFreeText =
        GemErpMedicationFaker.forFreeTextMedication()
            .withCodeText(codeText)
            .withFormText(formText)
            .withLotNumber(lotNumber)
            .withDrugCategory(category)
            .withVersion(version)
            .withVaccineTrue(true)
            .fake();

    assertNotNull(gemErpMedFreeText);
    assertEquals(codeText, gemErpMedFreeText.getCode().getText());
    assertEquals(formText, gemErpMedFreeText.getForm().getText());
    assertEquals(lotNumber, gemErpMedFreeText.getBatch().getLotNumber());
    assertTrue(gemErpMedFreeText.isVaccine());
    assertEquals(category.getCode(), gemErpMedFreeText.getCategory().orElseThrow().getCode());

    assertTrue(gemErpMedFreeText.getMeta().getProfile().get(0).getValue().endsWith("1.4"));
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldBuildMedicationWithMinimalValues() {
    val codeText = "Minimal Code Text";

    val gemErpMedFreeText =
        GemErpMedicationFaker.forFreeTextMedication().withCodeText(codeText).fake();

    assertNotNull(gemErpMedFreeText);
    assertEquals(codeText, gemErpMedFreeText.getCode().getText());
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldBuildMedicationWitWithoutValues() {
    val gemErpMedFreeText = GemErpMedicationFaker.forFreeTextMedication().fake();

    assertNotNull(gemErpMedFreeText);
    assertNotNull(gemErpMedFreeText.getNameFromCodeOreContainedRessource());
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldNotThrowExceptionWhenCodeTextIsMissing() {
    val faker = GemErpMedicationFaker.forFreeTextMedication().withFormText("Test Form Text");
    assertDoesNotThrow(faker::fake);
  }

  @Test
  void shouldFakeValidWithAllValues() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(true);
      val gemErpMedFreeText = GemErpMedicationFaker.forFreeTextMedication().fake();

      assertNotNull(gemErpMedFreeText);
      assertTrue(parser.isValid(gemErpMedFreeText));
    }
  }

  @Test
  void shouldFakeValidWithMinimumValues() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val gemErpMedFreeText = GemErpMedicationFaker.forFreeTextMedication().fake();

      assertNotNull(gemErpMedFreeText);
      assertTrue(parser.isValid(gemErpMedFreeText));
    }
  }
}
