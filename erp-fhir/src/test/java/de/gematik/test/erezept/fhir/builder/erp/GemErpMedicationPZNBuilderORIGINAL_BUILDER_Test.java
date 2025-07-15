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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedPznIngredient;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationCompoundingFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationFreeTextFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationIngredientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.junit.jupiter.api.Test;
import org.junitpioneer.jupiter.SetSystemProperty;

@SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.5.0")
class GemErpMedicationPZNBuilderORIGINAL_BUILDER_Test extends ErpFhirParsingTest {

  @Test
  void shouldBuildGemErpMedicationWithFixedValues() {
    // https://github.com/gematik/eRezept-Examples/blob/main/Standalone-Examples/E-Rezept-Workflow_gematik/1.4.3/Medication-SumatripanMedication.xml
    val medication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder()
            .version(ErpWorkflowVersion.V1_4)
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
    assertTrue(medication.getNameFromCodeOreContainedRessource().isPresent());
    assertEquals(
        "Sumatriptan-1a Pharma 100 mg Tabletten",
        medication.getNameFromCodeOreContainedRessource().get());

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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder()
            .version(ErpWorkflowVersion.V1_4)
            .pzn("06313728")
            .lotNumber("1234567890")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, medication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildSimplestMedication() {
    val medication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder()
            .version(ErpWorkflowVersion.V1_4)
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

    val kbvResult = ValidatorUtil.encodeAndValidate(parser, kbvMedication);
    assertTrue(kbvResult.isSuccessful());

    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedication)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
    assertFalse(gemMedication.getBatchLotNumber().isEmpty());
    assertEquals("123123", gemMedication.getBatchLotNumber().get());

    assertEquals(kbvMedication.getPznOptional(), gemMedication.getPzn());
    assertEquals(
        kbvMedication.getMedicationName(),
        gemMedication.getNameFromCodeOreContainedRessource().orElse(null));
    assertEquals(kbvMedication.getStandardSize(), gemMedication.getStandardSize().orElse(null));
    assertEquals(kbvMedication.getDarreichungsform(), gemMedication.getDarreichungsform());
    assertEquals(
        kbvMedication.getPackagingSizeOrEmpty(),
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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedication)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());

    assertEquals(
        kbvMedication.getPackagingSizeOrEmpty(),
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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicationFreeText)
            .version(ErpWorkflowVersion.V1_4)
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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4)
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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test()
  void shouldBuildFromMedicationCompounding() {
    val kbvMedicComp = KbvErpMedicationCompoundingFaker.builder().fake();
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
  }

  @Test()
  void shouldBuildFromMedicationCompoundingWithoutStrength() {
    val kbvMedicComp = KbvErpMedicationCompoundingFaker.builder().fake();
    kbvMedicComp.getIngredientFirstRep().setStrength(null);
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldBuildFromMedicationCompoundingWithMissingStrengthParams() {
    val kbvMedicComp =
        KbvErpMedicationCompoundingFaker.builder().withPackaging("im Karton eben").fake();
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  @Test()
  void shouldBuildFromMedicationCompoundingWithoutPzn() {
    val kbvMedicComp = KbvErpMedicationCompoundingFaker.builder().fake();
    kbvMedicComp.getIngredientFirstRep().setItem(null);
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertFalse(result.isSuccessful());
  }

  @Test
  void shouldBuildFromMedicationCompoundingWithSpecificValuesCorrect() {
    val pzn = PZN.random().getValue();
    val medName = "nicorette";
    val kbvMedicComp =
        KbvErpMedicationCompoundingFaker.builder()
            .withMedicationIngredient(pzn, medName)
            .withDosageForm(Darreichungsform.ATO)
            .withVaccine(true)
            .withPackaging("im Fass")
            .withCategory(MedicationCategory.C_00)
            .withAmount(2, 1, "wölkchen")
            .fake();
    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvMedicComp)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
    assertEquals(
        medName,
        gemMedication.getNameFromCodeOreContainedRessource().orElse("definitely a wrong name"));
    assertTrue(gemMedication.isVaccine());
    assertEquals(
        "im Fass",
        gemMedication.getExtension().stream()
            .filter(EpaMedicationStructDef.PACKAGING_EXTENSION::matches)
            .findFirst()
            .map(ext -> ext.getValue().castToString(ext.getValue()).getValue())
            .orElse("definitely non matching string"));
    assertEquals(
        Darreichungsform.ATO.getDisplay(),
        String.valueOf(gemMedication.getIngredient().get(0).getExtensionFirstRep().getValue()));
    assertEquals(
        pzn,
        ((EpaMedPznIngredient) gemMedication.getContained().get(0))
            .getPzn()
            .orElseThrow()
            .getValue());
    val medReference = gemMedication.getIngredientFirstRep().getItemReference().getReference();
    assertEquals(gemMedication.getContained().get(0).getId(), medReference);
    assertFalse(gemMedication.getIngredientFirstRep().hasItemCodeableConcept());
  }

  @Test
  void shouldFillMissingAttributesInIngredient() {
    val kbvErpMedicationIngredient = KbvErpMedicationIngredientFaker.builder().fake();
    kbvErpMedicationIngredient.getIngredientFirstRep().getExtensionFirstRep().setValue(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().setStrength(null);

    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4)
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
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication, false);
    assertTrue(result.isSuccessful());
  }

  /** works fine with new GemErpMedicationBuilder, fix this old builder is to inefficient */
  @SetSystemProperty(key = ERP_FHIR_PROFILES_TOGGLE, value = "1.4.0")
  @Test
  void shouldFillMissingAttributesInStrengthInCompounding() {
    val kbvErpMedicationIngredient = KbvErpMedicationCompoundingFaker.builder().fake();
    kbvErpMedicationIngredient.getIngredientFirstRep().getExtensionFirstRep().setValue(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().setStrength(null);

    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.getDefaultVersion())
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
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setCode(null);
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getNumerator().setValue(null);
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setSystem("http://hl7.org")
        .setValueElement(null);
    kbvErpMedicationIngredient
        .getIngredientFirstRep()
        .getStrength()
        .getDenominator()
        .setCode("null");
    kbvErpMedicationIngredient.getIngredientFirstRep().getStrength().getDenominator().setValue(0);

    val gemMedication =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.from(kbvErpMedicationIngredient)
            .version(ErpWorkflowVersion.V1_4)
            .lotNumber("123123")
            .build();

    val result = ValidatorUtil.encodeAndValidate(parser, gemMedication);
    assertTrue(result.isSuccessful());
  }

  @Test
  void shouldNotBuildAnIngredientCompoundingMedication() {
    val builder =
        GemErpMedicationPZNBuilderORIGINAL_BUILDER.builder()
            .isIngredient()
            .isCompounding()
            .isVaccine(true);
    assertThrows(BuilderException.class, builder::build);
  }
}
