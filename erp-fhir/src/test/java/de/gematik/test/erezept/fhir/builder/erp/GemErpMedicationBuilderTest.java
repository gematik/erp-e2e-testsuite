/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.EpaMedicationStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.jupiter.api.Test;

class GemErpMedicationBuilderTest extends ParsingTest {

  @Test
  void shouldBuildGemErpMedicationWithFixedValues() {
    // https://github.com/gematik/eRezept-Examples/blob/main/Standalone-Examples/E-Rezept-Workflow_gematik/1.4.3/Medication-SumatripanMedication.xml
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .category(EpaDrugCategory.C_00)
            .pzn("06313728", "Sumatriptan-1a Pharma 100 mg Tabletten")
            .isVaccine(false)
            .darreichungsform(Darreichungsform.TAB)
            .normgroesse(StandardSize.N1)
            .amount(20) // will use St as default unit
            .lotNumber("1234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());

    assertFalse(medication.isVaccine());

    assertTrue(medication.getPzn().isPresent());
    assertEquals("06313728", medication.getPzn().get().getValue());
    assertTrue(medication.getName().isPresent());
    assertEquals("Sumatriptan-1a Pharma 100 mg Tabletten", medication.getName().get());

    assertTrue(medication.getCategory().isPresent());
    assertEquals(EpaDrugCategory.C_00, medication.getCategory().get());

    assertTrue(medication.getDarreichungsform().isPresent());
    assertEquals(Darreichungsform.TAB, medication.getDarreichungsform().get());

    assertTrue(medication.getStandardSize().isPresent());
    assertEquals(StandardSize.N1, medication.getStandardSize().get());

    assertTrue(medication.getBatchLotNumber().isPresent());
    assertEquals("1234567890", medication.getBatchLotNumber().get());
    // this one is not null-safe!!
    assertEquals("1234567890", medication.getBatch().getLotNumber());

    assertTrue(medication.getAmountNumeratorUnit().isPresent());
    assertEquals("Stk", medication.getAmountNumeratorUnit().get());
    assertTrue(medication.getAmountNumerator().isPresent());
    assertEquals(20, medication.getAmountNumerator().get());
  }

  @Test
  void shouldBuildSimpleMedication() {
    // https://github.com/gematik/eRezept-Examples/blob/main/Standalone-Examples/E-Rezept-Workflow_gematik/1.4.3/Medication-SimpleMedication.xml
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .pzn("06313728")
            .lotNumber("1234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildSimplestMedication() {
    val medication =
        GemErpMedicationBuilder.builder()
            .version(ErpWorkflowVersion.V1_4_0)
            .pzn("06313728")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
    assertTrue(medication.getBatchLotNumber().isEmpty());
  }

  @Test
  void shouldBuildFromKbvPznMedication() {
    val kbvMedication =
        KbvErpMedicationPZNFaker.builder()
            .withSupplyForm(
                GemFaker.fakerValueSet(
                    Darreichungsform.class,
                    Darreichungsform.KPG)) // mapping Kombipackung not possible yet
            .withAmount(100, "Packung")
            .fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedication)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
    assertFalse(gemMedication.getBatchLotNumber().isEmpty());
    assertEquals("123123", gemMedication.getBatchLotNumber().get());

    assertEquals(kbvMedication.getPznOptional(), gemMedication.getPzn());
    assertEquals(kbvMedication.getMedicationName(), gemMedication.getName().orElse(null));
    assertEquals(kbvMedication.getStandardSize(), gemMedication.getStandardSize().orElse(null));
    assertEquals(kbvMedication.getDarreichungsformFirstRep(), gemMedication.getDarreichungsform());
    assertEquals(
        kbvMedication.getMedicationAmount(),
        gemMedication.getAmountNumerator().orElse(Integer.MIN_VALUE));
    assertEquals(kbvMedication.isVaccine(), gemMedication.isVaccine());

    // if KbvErpMedication does not have a unit, GemErpMedication will use "Stk" as default
    kbvMedication
        .getPackagingUnit()
        .ifPresentOrElse(
            unit -> assertEquals(unit, gemMedication.getAmountNumeratorUnit().orElse(null)),
            () -> assertEquals("Stk", gemMedication.getAmountNumeratorUnit().orElse(null)));
  }

  @Test
  void shouldBuildFromKbvPznMedicationWithDefaultAmountUnit() {
    val kbvMedication =
        KbvErpMedicationPZNFaker.builder()
            .withSupplyForm(
                GemFaker.fakerValueSet(
                    Darreichungsform.class,
                    Darreichungsform.KPG)) // mapping Kombipackung not possible yet
            .withAmount(3, null)
            .fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedication)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());

    assertEquals(
        kbvMedication.getMedicationAmount(),
        gemMedication.getAmountNumerator().orElse(Integer.MIN_VALUE));
    // assertEquals(kbvMedication.getPackagingUnit(), gemMedication.getAmountNumeratorUnit());
    assertEquals("Stk", gemMedication.getAmountNumeratorUnit().orElse(null));
  }

  @Test
  void shouldBuildFromFreetextMedication() {
    val freitext = "Dies ist der zu testende Freitext";
    val kbvMedicationFreeText =
        KbvErpMedicationFreeTextFaker.builder()
            .withFreeText(freitext)
            .withDosageForm(GemFaker.fakerValueSet(Darreichungsform.class).getDisplay())
            .fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedicationFreeText)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildFromMedicationIngredientWithSpecificParamsCorrect() {

    val kbvErpMedicationIngredient =
        KbvErpMedicationIngredientFaker.builder()
            .withStandardSize(StandardSize.N1)
            .withDosageForm(Darreichungsform.AMP.getDisplay())
            .withIngredientComponent(2, 1, "wölkchen")
            .withDrugName("MonsterPille")
            .withVaccine(false)
            .withCategory(MedicationCategory.C_00)
            .withAmount("4", 1, "schwaden")
            .fake();

    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
    assertEquals(Optional.empty(), gemMedication.getPzn());
    assertEquals(
        "MonsterPille",
        ((CodeableConcept) gemMedication.getIngredientFirstRep().getItem()).getText());
    assertEquals(
        String.valueOf(Darreichungsform.AMP.getDisplay()),
        String.valueOf(gemMedication.getIngredient().get(0).getExtensionFirstRep().getValue()));
    assertEquals(
        MedicationCategory.C_00.getDisplay(),
        gemMedication.getCategory().orElseThrow().getDisplay());
    assertFalse(gemMedication.isVaccine());
    assertEquals(1, gemMedication.getAmount().getDenominator().getValue().intValue());
    assertEquals(4, gemMedication.getAmountNumerator().orElseThrow());
    assertEquals("schwaden", gemMedication.getAmountNumeratorUnit().orElseThrow());
  }

  @Test
  void shouldBuildFromMedicationIngredientWithDefaultParamsCorrect() {
    val kbvErpMedicationIngredient = KbvErpMedicationIngredientFaker.builder().fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test()
  void shouldBuildFromMedicationCompounding() {
    val kbvMedicComp = KbvErpMedicationCompoundingFaker.builder().fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test()
  void shouldBuildFromMedicationCompoundingWithMissingStrenthParams() {
    val kbvMedicComp =
        KbvErpMedicationCompoundingFaker.builder().withPackaging("im Karton eben").fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildFromMedicationCompoundingWithSpecificValuesCorrect() {
    val kbvMedicComp =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(PZN.random().getValue(), "nicorette")
            .withDosageForm(Darreichungsform.ATO)
            .withVaccine(true)
            .withPackaging("im Fass")
            .withCategory(MedicationCategory.C_00)
            .withAmount(2, 1, "wölkchen")
            .fake();
    val gemMedication =
        GemErpMedicationBuilder.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
    assertEquals(
        "nicorette", gemMedication.getIngredientFirstRep().getItemCodeableConcept().getText());
    assertTrue(gemMedication.isVaccine());
    assertEquals(
        "im Fass",
        String.valueOf(
            gemMedication.getExtension().stream()
                .filter(
                    ex ->
                        ex.getUrl()
                            .contains(EpaMedicationStructDef.PACKAGING_EXTENSION.getCanonicalUrl()))
                .findFirst()
                .get()
                .getValue()));
    assertEquals(
        String.valueOf(Darreichungsform.ATO.getDisplay()),
        String.valueOf(gemMedication.getIngredient().get(0).getExtensionFirstRep().getValue()));
  }

  @Test
  void shouldFillMissingAttributesInIngredient() {
    val kbvErpMedicationIngredient = KbvErpMedicationIngredientFaker.builder().fake();
    kbvErpMedicationIngredient.getIngredientFirstRep().getExtensionFirstRep().setValue(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().setStrength(null);

    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFillPartsOfMissingAttributesInIngredient() {
    val kbvErpMedicationIngredient = KbvErpMedicationIngredientFaker.builder().fake();
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setSystem(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setCode(null);
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setSystem(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getDenominator().setCode(null);
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setValue(null);

    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFillMissingAttributesInStrengthInCompounding() {
    val kbvErpMedicationIngredient = KbvErpMedicationCompoundingFaker.builder().fake();
    kbvErpMedicationIngredient.getIngredientFirstRep().getExtensionFirstRep().setValue(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().setStrength(null);

    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldFillPartsOfMissingAttributesInStrengthInCompounding() {
    val kbvErpMedicationIngredient = KbvErpMedicationCompoundingFaker.builder().fake();
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getNumerator()
        .setSystem("http://hl7.org");
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setCode("null");
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setValue(0);
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setSystem("http://hl7.org");
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setCode("null");
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getDenominator().setValue(0);

    val gemMedication =
        GemErpMedicationBuilder.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4_0)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
  }
}
