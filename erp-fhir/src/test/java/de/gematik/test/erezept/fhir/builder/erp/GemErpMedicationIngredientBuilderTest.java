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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.r4.componentbuilder.GemEpaIngredientComponentBuilder;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.math.BigDecimal;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;
import org.junit.jupiter.api.Test;

class GemErpMedicationIngredientBuilderTest extends ErpFhirParsingTest {

  Medication.MedicationIngredientComponent ingredient =
      GemEpaIngredientComponentBuilder.builder().atc(ATC.from("atcCode=123")).build();

  @Test
  void shouldSetFormFormTextCorrect() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .ingredientComponent(ingredient)
            .formText("Tablette")
            .build();
    assertNotNull(medIngredient);
    assertEquals("Tablette", medIngredient.getForm().getText());
    val result = parser.validate(medIngredient);
    assertTrue(result.isSuccessful());
  }

  //  ingredientComponentList

  @Test
  void shouldSetSpecificVersionCorrect() {
    val value = ErpWorkflowVersion.V1_4;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .ingredientComponent(ingredient)
            .version(value)
            .build();

    assertNotNull(medIngredient);
    assertTrue(medIngredient.getMeta().getProfile().get(0).getValue().contains("1.4"));
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetVaccineCorrect() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .isVaccine(true)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertTrue(medIngredient.isVaccine());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetNoVaccineCorrect() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .isVaccine(false)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertFalse(medIngredient.isVaccine());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetDrugCatCorrect() {
    val value = EpaDrugCategory.C_01;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .category(value)
            .ingredientComponent(ingredient)
            .build();

    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getCategory().orElseThrow());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetStandardSizeCorrect() {
    val value = StandardSize.N1;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .normgroesse(value)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getStandardSize().orElseThrow());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetPackagingCorrect() {
    val value = "Karton";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .version(ErpWorkflowVersion.V1_4)
            .packaging(value)
            .amount(5)
            .ingredientComponent(ingredient)
            .build();

    val totalQuantity =
        medIngredient.getAmount().getNumerator().getExtension().stream()
            .filter(EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT::matches)
            .map(it -> it.getValueAsPrimitive().getValueAsString())
            .findFirst()
            .orElseThrow(() -> new AssertionError("Total Quantity expected but not found"));

    assertNotNull(medIngredient);
    assertEquals(value, totalQuantity);
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetManufacturingInstructionCorrect() {
    val value = "rechts rum rühern, immer nur rechts rum";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .ingredientComponent(ingredient)
            .manufacturingInstruction(value)
            .build();
    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getManufacturingInstruction().orElseThrow());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldBuildMedIngredientAsAtcCorrect() {
    val code = "12345678";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient().atc(ATC.from(code)).build();

    val actCoding = ATC.from(code);
    assertNotNull(medIngredient);
    assertTrue(
        medIngredient
            .getCode()
            .getCodingFirstRep()
            .is(actCoding.getSystemUrl(), actCoding.getValue()));
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetSnomedCorrect() {
    val value = "SnomedCode";
    val medIngredient = GemErpMedicationIngredientBuilder.forIngredient().snomed(value).build();
    assertNotNull(medIngredient);
    assertTrue(
        medIngredient
            .getCode()
            .getCodingFirstRep()
            .is(CommonCodeSystem.SNOMED_SCT.getCanonicalUrl(), value));
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetAskCorrect() {
    val value = "ASK_Code";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient().ask(ASK.from(value)).build();
    val askCoding = ASK.from(value);
    assertNotNull(medIngredient);
    assertTrue(medIngredient.getCode().getCodingFirstRep().is(askCoding.getSystemUrl(), value));
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetFreeTextInCodingCorrect() {
    val value = "Name in Coding ";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .formText(value)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getForm().getText());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetAmountNumCorrect() {
    long value = 3L;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .amount(value)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getAmountNumerator().get().longValue());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetAmountDenomCorrect() {
    val value = 5L;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .amountDenominator(value)
            .amount(3)
            .ingredientComponent(ingredient)
            .build();
    assertNotNull(medIngredient);
    assertEquals(BigDecimal.valueOf(value), medIngredient.getAmount().getDenominator().getValue());

    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldThrowWhileSetDenomWithoutNumeratorInAmount() {
    val value = 5L;
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .amountDenominator(value)
            .ingredientComponent(ingredient);
    assertThrows(BuilderException.class, medIngredient::build);
  }

  @Test
  void shouldThrowWhileTooLongPackagingSize() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .amount(5)
            .packagingSize("12345678")
            .ingredientComponent(ingredient);
    assertThrows(BuilderException.class, medIngredient::build);
  }

  @Test
  void shouldThrowWhileSettingPackagingSizeWithoutNumerator() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .packagingSize("1234567")
            .ingredientComponent(ingredient);
    assertThrows(BuilderException.class, medIngredient::build);
  }

  @Test
  void shouldThrowWhilePznAndCodingIsMissing() {
    val value = 5L;
    val medIngredient = GemErpMedicationIngredientBuilder.forIngredient().amountDenominator(value);
    assertThrows(BuilderException.class, medIngredient::build);
  }

  @Test
  void shouldSetIngredientComponentCorrect() {
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient().ingredientComponent(ingredient).build();
    assertNotNull(medIngredient);
    assertEquals(ingredient, medIngredient.getIngredientFirstRep());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetBatchCorrect() {
    val value = "LotNumber";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .ingredientComponent(ingredient)
            .lotNumber(value)
            .build();
    assertNotNull(medIngredient);
    assertEquals(value, medIngredient.getBatch().getLotNumber());
    assertTrue(parser.isValid(medIngredient));
  }

  @Test
  void shouldSetPackagingSizeCorrect() {
    val value = "boxed";
    val medIngredient =
        GemErpMedicationIngredientBuilder.forIngredient()
            .ingredientComponent(ingredient)
            .packagingSize(value)
            .amount(1)
            .build();
    assertNotNull(medIngredient);
    assertEquals(
        value,
        String.valueOf(medIngredient.getAmount().getNumerator().getExtensionFirstRep().getValue()));
    assertTrue(parser.isValid(medIngredient));
  }
}
