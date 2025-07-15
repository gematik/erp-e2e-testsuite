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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedication;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.val;
import org.apache.commons.lang3.RandomStringUtils;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Quantity;
import org.junit.jupiter.api.Test;

class GemErpMedCompoundingBuilderTest extends ErpFhirParsingTest {

  Medication.MedicationIngredientComponent ingredient =
      IngredientCodeBuilder.builder()
          .withAsk(ASK.from("askCode"))
          .ingredientStrength(new Quantity(3), new Quantity(1), "ml")
          .darreichungsform("im Einer")
          .textInCoding("falls kein Code zur Hand hier was passendes rein schreiben")
          .build();

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithWorkFlow1_4() {
    val code = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ask(ASK.from(code))
            .version(ErpWorkflowVersion.V1_4)
            .build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(code, gemMedCompounding.getCode().getCodingFirstRep().getCode());
    assertTrue(gemMedCompounding.getMeta().getProfile().get(0).getValue().endsWith("1.4"));
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWitPackagingSize() {
    val code = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ask(ASK.from(code))
            .amount(5)
            .packagingSize("pack")
            .build();
    val res = parser.validate(gemMedCompounding);
    assertTrue(res.isSuccessful());
    assertEquals(
        "pack",
        gemMedCompounding.getAmount().getNumerator().getExtension().stream()
            .filter(EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT::matches)
            .findFirst()
            .get()
            .getValueAsPrimitive()
            .getValueAsString());
  }

  @Test
  void shouldThrowWleSettingpackagingSizeWithoutNumerator() {
    val code = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ask(ASK.from(code)).packagingSize("pack");
    assertThrows(BuilderException.class, gemMedCompounding::build);
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithPackaging() {
    val code = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ask(ASK.from(code))
            .packaging("packaging")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(
        "packaging",
        gemMedCompounding.getExtension().stream()
            .filter(
                ex ->
                    EpaMedicationStructDef.PACKAGING_EXTENSION
                        .getCanonicalUrl()
                        .equals(ex.getUrl()))
            .findFirst()
            .get()
            .getValueAsPrimitive()
            .getValueAsString());
  }

  @Test
  void shouldThrowWileBuildMedCompoundingWithToLongPackaging() {
    val packaging = RandomStringUtils.random(91, true, true);
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ask(ASK.from("code")).packaging(packaging);
    assertThrows(BuilderException.class, gemMedCompounding::build);
  }

  @Test
  void shouldThrowWhilePackagingSizeIsToLong() {
    val toLongString = RandomStringUtils.random(91, true, true);
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ask(ASK.from("code"))
            .packagingSize(toLongString);
    assertThrows(BuilderException.class, gemMedCompounding::build);
  }

  @Test
  void shouldThrowWhileAmountNumeratorIsToLong() {
    val toLongInt = FakerBrick.getGerman().random().nextInt(10000000, 1000000000); // > 7 digits
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ask(ASK.from("code")).amount(toLongInt);
    assertThrows(BuilderException.class, gemMedCompounding::build);
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingAsASK() {
    val code = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ask(ASK.from(code)).build();
    val askCoding = ASK.from(code);
    assertTrue(parser.isValid(gemMedCompounding));
    assertTrue(
        gemMedCompounding
            .getCode()
            .getCodingFirstRep()
            .is(askCoding.getSystemUrl(), askCoding.getValue()));
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithSupplyForm() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(
        "askCode",
        gemMedCompounding
            .getIngredientFirstRep()
            .getItemCodeableConcept()
            .getCoding()
            .get(0)
            .getCode());
  }

  @Test
  void shouldThrowWithoutRequiredCodeOrPzn() {
    val gemMedCompounding = GemErpMedCompoundingBuilder.forCompounding();
    assertThrows(BuilderException.class, gemMedCompounding::build);
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingAsVaccine() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .isVaccine(true)
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertTrue(gemMedCompounding.isVaccine());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingAsNotVaccine() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertFalse(gemMedCompounding.isVaccine());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithVaccineExtension() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertFalse(gemMedCompounding.isVaccine());
    assertEquals(
        EpaMedicationStructDef.VACCINE_EXT.getCanonicalUrl(),
        gemMedCompounding.getExtension().get(0).getUrl());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithDarreichungsform() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(
        "im Einer",
        gemMedCompounding
            .getIngredientFirstRep()
            .getExtensionFirstRep()
            .getValueAsPrimitive()
            .getValueAsString());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithDrugCategory() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .category(EpaDrugCategory.C_00)
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(EpaDrugCategory.C_00, gemMedCompounding.getCategory().get());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithFormText() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .formText("testText")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals("testText", gemMedCompounding.getForm().getText());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWitAmount() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .amount(1)
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(1, gemMedCompounding.getAmountNumerator().get().longValue());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithAmount2() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .amount(2, "Stk")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals("Stk", gemMedCompounding.getAmount().getNumerator().getUnit());
    assertEquals(2, gemMedCompounding.getAmount().getNumerator().getValue().intValue());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithLotNum() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .lotNumber("lotNum")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals("lotNum", gemMedCompounding.getBatchLotNumber().get());
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithCodeText() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .codeText("codeText")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals("codeText", gemMedCompounding.getCode().getText());
  }

  @Test
  void shouldGetMedicationName() {
    val pzn = PZN.from("12345678");
    val name = "Test Medication Name";
    val pznIngredient = IngredientCodeBuilder.builder().withPzn(pzn, name).build();
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(pznIngredient).build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(name, gemMedCompounding.getNameFromCodeOreContainedRessource().orElseThrow());
  }

  @Test
  void shouldBuildBIGCorrectMedCompoundingPzn() {
    val askCode = "123456789";
    val askIngredient =
        IngredientCodeBuilder.builder()
            .withAsk(ASK.from("IngredientAskCode"))
            .withPzn(PZN.from("ingr-pzn-code"))
            .ingredientStrength(Quantity.fromUcum("3", "mg"), Quantity.fromUcum("1", "l"))
            .darreichungsform("Darreichungsform.VER")
            .build();
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ask(ASK.from(askCode))
            .version(ErpWorkflowVersion.V1_4)
            .isVaccine(true)
            .category(EpaDrugCategory.C_00)
            .ingredientComponent(askIngredient)
            .category(EpaDrugCategory.C_00)
            .formText("testText")
            .amount(1)
            .manufacturingInstruction("testInstruction")
            .amountDenominator(9)
            .lotNumber("lotNum")
            .packagingSize("Boxed")
            .build();

    assertTrue(gemMedCompounding.isVaccine());
    assertEquals(9, gemMedCompounding.getAmount().getDenominator().getValue().intValue());

    assertEquals(
        "testInstruction",
        gemMedCompounding.getExtension().stream()
            .filter(EpaMedicationStructDef.MANUFACTURING_INSTRUCTION::matches)
            .findFirst()
            .orElseThrow()
            .getValueAsPrimitive()
            .getValueAsString());
    assertEquals(askCode, gemMedCompounding.getCode().getCodingFirstRep().getCode());
    assertTrue(gemMedCompounding.getMeta().getProfile().get(0).getValue().endsWith("1.4"));
    assertEquals(EpaDrugCategory.C_00, gemMedCompounding.getCategory().get());
    assertEquals(askCode, gemMedCompounding.getCode().getCodingFirstRep().getCode());
    assertEquals(EpaDrugCategory.C_00, gemMedCompounding.getCategory().get());
    assertEquals("testText", gemMedCompounding.getForm().getText());
    assertEquals(1, gemMedCompounding.getAmountNumerator().get().longValue());
    assertEquals("lotNum", gemMedCompounding.getBatchLotNumber().get());
    val containedMed = (EpaMedication) gemMedCompounding.getContained().get(0);
    assertEquals("ingr-pzn-code", containedMed.getPzn().orElseThrow().getValue());
    assertEquals(
        "Boxed",
        gemMedCompounding.getAmount().getNumerator().getExtension().stream()
            .filter(
                ex ->
                    EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT
                        .getCanonicalUrl()
                        .equals(ex.getUrl()))
            .findFirst()
            .get()
            .getValueAsPrimitive()
            .getValueAsString());

    assertTrue(parser.isValid(gemMedCompounding));
  }

  @Test
  void shouldBuildMinimalCorrectMedCompoundingWithFreeText() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .formText("formTextText")
            .build();

    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals("formTextText", gemMedCompounding.getForm().getText());
  }

  @Test
  void shouldSetPackagingSizeCorrect() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .amount(3)
            .packagingSize("1234567")
            .build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(
        "1234567",
        gemMedCompounding
            .getAmount()
            .getNumerator()
            .getExtensionFirstRep()
            .getValueAsPrimitive()
            .getValueAsString());
  }

  @Test
  void shouldThrowCausedByForgotToSetUpAmountWhileSetPackaging() {
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .packagingSize("StandardSize.N1");
    assertThrows(BuilderException.class, () -> gemMedCompounding.build());
  }

  @Test
  void shouldSetIngredientStrengthCorrect() {
    val atcCode = "123456789";
    val atcIngredient =
        IngredientCodeBuilder.builder()
            .withAtc(ATC.from(atcCode))
            .ingredientStrength(new Quantity(5), new Quantity(1))
            .build();

    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(atcIngredient).build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(
        5,
        gemMedCompounding
            .getIngredientFirstRep()
            .getStrength()
            .getNumerator()
            .getValue()
            .intValue());
    assertEquals(
        1,
        gemMedCompounding
            .getIngredientFirstRep()
            .getStrength()
            .getDenominator()
            .getValue()
            .intValue());
  }

  @Test
  void shouldSetATCCorrect() {
    val atcCode = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding().atc(ATC.from(atcCode)).build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(atcCode, gemMedCompounding.getCode().getCodingFirstRep().getCode());
  }

  @Test
  void shouldBuildSnomedMedCorrect() {
    val snomedCode = "123456789";
    val gemMedCompounding = GemErpMedCompoundingBuilder.forCompounding().snomed(snomedCode).build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(snomedCode, gemMedCompounding.getCode().getCodingFirstRep().getCode());
    assertEquals(
        CommonCodeSystem.SNOMED_SCT.getCanonicalUrl(),
        gemMedCompounding.getCode().getCodingFirstRep().getSystem());
  }

  @Test
  void shouldSetLotNo() {
    val lotNo = "123456789";
    val gemMedCompounding =
        GemErpMedCompoundingBuilder.forCompounding()
            .ingredientComponent(ingredient)
            .lotNumber(lotNo)
            .build();
    assertTrue(parser.isValid(gemMedCompounding));
    assertEquals(lotNo, gemMedCompounding.getBatchLotNumber().get());
  }

  @Test
  void shouldMapPznCorrect() {
    val pzn = PZN.random();
    ingredient.getItemCodeableConcept().addCoding(pzn.asCoding());
    val medCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingredient).build();

    assertTrue(parser.isValid(medCompounding));
  }

  @Test
  void shouldDetectNoPZN() {
    val ingr =
        IngredientCodeBuilder.builder()
            .withAtc(ATC.from("atc-code"))
            .ingredientStrength(new Quantity(3), new Quantity(1))
            .build();
    val medCompounding =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingr).build();

    assertTrue(parser.isValid(medCompounding));
  }

  @Test
  void shouldThrowWhileDetectAbsentIngredientItem() {
    val ingr =
        IngredientCodeBuilder.builder()
            .ingredientStrength(new Quantity(3), new Quantity(1))
            .build();
    val medCompoundingBuilder =
        GemErpMedCompoundingBuilder.forCompounding().ingredientComponent(ingr);

    assertThrows(BuilderException.class, medCompoundingBuilder::build);
  }
}
