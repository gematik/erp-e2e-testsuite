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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.Test;

class GemErpMedFreeTextBuilderTest extends ErpFhirParsingTest {

  Medication.MedicationIngredientComponent ingredient =
      IngredientCodeBuilder.builder()
          .withAsk(ASK.from("askCode"))
          .ingredientStrength(new Quantity(3), new Quantity(1), "ml")
          .darreichungsform("im Einer")
          .textInCoding("falls kein Code zur Hand hier was passendes rein schreiben")
          .build();

  @Test
  void shouldBuildMedicationWithCodeText() {
    String codeText = "Test Code Text";

    val builder = GemErpMedicationBuilder.forFreeText();
    val gemErpMedFreeText = builder.codeText(codeText).build();
    assertNotNull(gemErpMedFreeText);

    assertEquals(codeText, gemErpMedFreeText.getFreeText().orElseThrow());
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldBuildMedicationWithFormText() {
    String formText = "Test Form Text";

    val builder = GemErpMedicationBuilder.forFreeText();
    val gemErpMedFreeText = builder.formText(formText).codeText(GemFaker.fakerName()).build();

    assertNotNull(gemErpMedFreeText);
    assertEquals(formText, gemErpMedFreeText.getForm().getText());
    assertTrue(parser.isValid(gemErpMedFreeText));
  }

  @Test
  void shouldThrowExceptionWhenCodeTextIsMissing() {
    var builder = GemErpMedicationBuilder.forFreeText();

    assertThrows(BuilderException.class, builder::build);
  }

  @Test
  void shouldWhileCodeTextIsBlank() {
    val freeTextBuilder = GemErpMedCompoundingBuilder.forFreeText().codeText("");
    assertThrows(BuilderException.class, freeTextBuilder::build);
  }

  @Test
  void shouldWhileFormTextIsBlank() {
    val freeTextBuilder = GemErpMedCompoundingBuilder.forFreeText().formText("");
    assertThrows(BuilderException.class, freeTextBuilder::build);
  }

  @Test
  void shouldBuildMedicationWithAllAvailableValues() {
    val builder = GemErpMedicationBuilder.forFreeText();

    val version = ErpWorkflowVersion.V1_4;
    val category = EpaDrugCategory.C_00;
    val lotNumber = "TestLotNumber";
    val codeText = "TestCodeText";
    val formText = "TestFormText";

    val medication =
        builder
            .version(version)
            .category(category)
            .isVaccine(true)
            .lotNumber(lotNumber)
            .codeText(codeText)
            .formText(formText)
            .manufacturingInstruction("TestInstruction")
            .build();

    assertNotNull(medication);

    assertTrue(medication.getMeta().getProfile().get(0).getValue().endsWith("1.4"));
    assertEquals(category, medication.getCategory().orElseThrow());
    assertTrue(medication.isVaccine());
    assertEquals(lotNumber, medication.getBatch().getLotNumber());
    assertEquals(codeText, medication.getCode().getText());
    assertEquals(formText, medication.getForm().getText());
  }
}
