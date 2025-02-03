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

package de.gematik.test.core.expectations.verifier.emlverifier;

import static de.gematik.test.core.Helper.findBySystem;
import static java.text.MessageFormat.format;

import com.google.common.base.Strings;
import de.gematik.test.core.expectations.requirements.EmlAfos;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.erezept.eml.fhir.parser.profiles.EpaMedStructDef;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpProvidePrescription;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.TelematikID;
import java.util.*;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;

@Slf4j
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaOpProvidePrescriptionVerifier {

  public static final String MEDICATION_AMOUNT_DENOMINATOR_VALUE =
      "Medication.amount.denominator.value";
  public static final String MEDICATION_AMOUNT_NUMERATOR_EXTENSION_GESAMTMENGE =
      "Medication.amount.numerator.extension:Gesamtmenge";
  public static final String MEDICATION_AMOUNT_NUMERATOR_UNIT = "Medication.amount.numerator.unit";
  public static final String INGREDIENT_CODEABLE_CONCEPT_SYSTEM =
      ".ingredient.item[1]:itemCodeableConcept.coding:pznCode.system";
  public static final String INGREDIENT_CODE_CON_CODE = "Ingredient.codeCon.code";
  public static final String INGREDIENT_CODE_CON_TEXT = "Ingredient.codeCon.text";
  public static final String NORMGROESSE = "Normgroesse";
  public static final String MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_UNIT =
      "Medication.ingredient.strength.numerator.unit";
  public static final String MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_VALUE =
      "Medication.ingredient.strength.numerator.value";
  public static final String MEDICATION_INGREDIENT_STRENGTH_DENOMINATOR_VALUE =
      "Medication.ingredient.strength.denominator.value";
  public static final String MEDICATION_CODE_TEXT = "Medication.code.text";
  public static final String MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE =
      "Medication.extension:Arzneimittelkategorie";
  public static final String MEDICATION_EXTENSION_IMPFSTOFF = "Medication.extension:Impfstoff";

  public static VerificationStep<EpaOpProvidePrescription> emlPrescriptionIdIsEqualTo(
      PrescriptionId prescriptionId) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription.getEpaPrescriptionId().getValue().equals(prescriptionId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25952.getRequirement(),
            format(
                "Die Dispensation muss die PrescriptionID: {0} haben, daher ist {1} nicht erfüllt",
                prescriptionId.getValue(), EmlAfos.A_25952))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlAuthoredOnIsEqualTo(Date date) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> prescription.getEpaAuthoredOn().equals(date);
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Das AuthoredOn Datum entspricht nicht dem erwarteten Datum")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlMedicationMapsTo(
      KbvErpMedication expectedMedication) {

    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> {
          val expectedCodings = expectedMedication.getCode().getCoding();
          val expectedValues = new HashMap<String, Optional<String>>();
          var isEqual = false;

          if (expectedCodings.size() > 1) {
            val epaCodings = prescription.getEpaMedication().getCode().getCoding();
            if (epaCodings.size() != expectedCodings.size()) return false;
            return minimalSystemValidation(expectedCodings, epaCodings);
          }

          if (isMedicationTypeOf(expectedMedication, "wirkstoff")) {
            val givenValues = getGivenIngredientValues(prescription);
            val expectedAttributes =
                specificExpectedIngredientAttributes(expectedMedication, expectedValues);
            isEqual = hasEqualValues(givenValues, expectedAttributes);
          }

          if (isMedicationTypeOf(expectedMedication, "freitext")) {
            val givenValues = getGivenFreeTextValues(prescription);
            val expectedAttributes =
                specificExpectedFreeTextAttributes(expectedMedication, expectedValues);
            isEqual = hasEqualValues(givenValues, expectedAttributes);
          }

          if (isMedicationTypeOf(expectedMedication, "rezeptur")) {
            val givenValues = getGivenCompoundingValues(prescription);
            var expectedAttributes =
                specificExpectedCompoundingAttributes(expectedMedication, expectedValues);
            isEqual = hasEqualValues(givenValues, expectedAttributes);
          }

          if (isPznMedication(expectedMedication)) {
            val givenValues = getGivenPZNValues(prescription);
            val expectedAttributes = specificExpectedPZNAttributes(expectedMedication);
            isEqual = hasEqualValues(givenValues, expectedAttributes);
          }

          return isEqual;
        };

    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Die enthaltenen Werte oder Coding/s (PZN / ASK / ATC) in der Epa Medication müssen mit"
                + " denen der KbvMedication übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  private static Map<String, Optional<String>> specificExpectedIngredientAttributes(
      KbvErpMedication expectedMedication, HashMap<String, Optional<String>> expectedValues) {

    expectedValues.putAll(generalExpectedAttributes(expectedMedication, expectedValues));
    expectedValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(expectedMedication.getAmount().getDenominator().getValue())));
    expectedValues.put(
        MEDICATION_AMOUNT_NUMERATOR_EXTENSION_GESAMTMENGE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication.getAmount().getNumerator().getExtensionFirstRep().getValue())));
    expectedValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(
            String.valueOf(expectedMedication.getAmount().getNumerator().getUnit())));

    expectedValues.put(
        INGREDIENT_CODEABLE_CONCEPT_SYSTEM,
        Optional.ofNullable(
            expectedMedication
                .getIngredientFirstRep()
                .getItemCodeableConcept()
                .getCoding()
                .get(0)
                .getSystem()));
    expectedValues.put(
        INGREDIENT_CODE_CON_CODE,
        Optional.ofNullable(
            expectedMedication
                .getIngredientFirstRep()
                .getItemCodeableConcept()
                .getCoding()
                .get(0)
                .getCode()));
    expectedValues.put(
        INGREDIENT_CODE_CON_TEXT,
        Optional.ofNullable(
            expectedMedication.getIngredientFirstRep().getItemCodeableConcept().getText()));
    expectedValues.put(
        NORMGROESSE, Optional.ofNullable(expectedMedication.getStandardSize().getCode()));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_UNIT,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getStrength()
                    .getNumerator()
                    .getUnit())));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getStrength()
                    .getNumerator()
                    .getValue())));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getStrength()
                    .getDenominator()
                    .getValue())));
    return expectedValues;
  }

  private static Map<String, Optional<String>> specificExpectedCompoundingAttributes(
      KbvErpMedication expectedMedication, HashMap<String, Optional<String>> expectedValues) {
    expectedValues.putAll(generalExpectedAttributes(expectedMedication, expectedValues));

    expectedValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(expectedMedication.getAmount().getDenominator().getValue())));
    expectedValues.put(
        MEDICATION_AMOUNT_NUMERATOR_EXTENSION_GESAMTMENGE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication.getAmount().getNumerator().getExtensionFirstRep().getValue())));
    expectedValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(
            String.valueOf(expectedMedication.getAmount().getNumerator().getUnit())));
    expectedValues.put(
        MEDICATION_CODE_TEXT, Optional.ofNullable(expectedMedication.getCode().getText()));

    expectedValues.put(
        INGREDIENT_CODEABLE_CONCEPT_SYSTEM,
        Optional.ofNullable(
            expectedMedication
                .getIngredientFirstRep()
                .getItemCodeableConcept()
                .getCoding()
                .get(0)
                .getSystem()));
    expectedValues.put(
        INGREDIENT_CODE_CON_CODE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getItemCodeableConcept()
                    .getCoding()
                    .get(0)
                    .getCode())));

    expectedValues.put(
        INGREDIENT_CODE_CON_TEXT,
        Optional.ofNullable(
            expectedMedication.getIngredientFirstRep().getItemCodeableConcept().getText()));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_UNIT,
        Optional.ofNullable(
            expectedMedication.getIngredientFirstRep().getStrength().getNumerator().getUnit()));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getStrength()
                    .getNumerator()
                    .getValue())));
    expectedValues.put(
        MEDICATION_INGREDIENT_STRENGTH_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                expectedMedication
                    .getIngredientFirstRep()
                    .getStrength()
                    .getDenominator()
                    .getValue())));
    return expectedValues;
  }

  private static Map<String, Optional<String>> specificExpectedFreeTextAttributes(
      KbvErpMedication expectedMedication, HashMap<String, Optional<String>> expectedValues) {

    expectedValues.putAll(generalExpectedAttributes(expectedMedication, expectedValues));
    expectedValues.put(MEDICATION_CODE_TEXT, Optional.ofNullable(expectedMedication.getFreeText()));
    return expectedValues;
  }

  private static Map<String, Optional<String>> generalExpectedAttributes(
      KbvErpMedication expectedMedication, HashMap<String, Optional<String>> expectedValues) {
    expectedValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        Optional.ofNullable(expectedMedication.getCatagory().get(0).getCode()));
    expectedValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        Optional.of(String.valueOf(expectedMedication.isVaccine())));

    expectedValues.put("Form", Optional.ofNullable(expectedMedication.getForm().getText()));
    return expectedValues;
  }

  private static Map<String, Optional<String>> getGivenCompoundingValues(
      EpaOpProvidePrescription compoundingPrescription) {
    val givenValues = new HashMap<String, Optional<String>>();
    givenValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        compoundingPrescription.getEpaMedication().getExtension().stream()
            .filter(ext -> EpaMedStructDef.VACCINE_EXT.matches(ext.getUrl()))
            .map(extension -> extension.getValue().primitiveValue())
            .findFirst());
    givenValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        compoundingPrescription
            .getEpaMedication()
            .getExtensionsByUrl(EpaMedStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl())
            .stream()
            .map(ext -> ext.getValue().castToCoding(ext.getValue()).getCode())
            .findFirst());

    val ingredientComp = compoundingPrescription.getEpaMedication().getIngredientFirstRep();

    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(ingredientComp.getStrength().getDenominator().getValue())));
    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_UNIT,
        Optional.ofNullable(ingredientComp.getStrength().getNumerator().getUnit()));
    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(ingredientComp.getStrength().getNumerator().getValue())));

    if (ingredientComp.hasItemCodeableConcept()) {
      givenValues.put(
          INGREDIENT_CODE_CON_CODE,
          ingredientComp.getItemCodeableConcept().getCoding().stream()
              .filter(co -> co.getSystem().equals(DeBasisCodeSystem.PZN.getCanonicalUrl()))
              .map(Coding::getCode)
              .findFirst());
      givenValues.put(
          INGREDIENT_CODE_CON_TEXT,
          Optional.ofNullable(ingredientComp.getItemCodeableConcept().getText()));
      givenValues.put(
          INGREDIENT_CODEABLE_CONCEPT_SYSTEM,
          ingredientComp.getItemCodeableConcept().getCoding().stream()
              .map(Coding::getSystem)
              .filter(system -> system.equals(DeBasisCodeSystem.PZN.getCanonicalUrl()))
              .findFirst());

      // in ErpWorkflowVersion.V1_4_0 ++ the PZN-Object is in a Contained Resource & not in the
      // Expected Ingredient -> coding
    }
    if (compoundingPrescription.getEpaMedication().hasContained()) {
      val medication =
          (Medication) compoundingPrescription.getEpaMedication().getContained().get(0);
      val containedPznCC = medication.getCode();
      givenValues.put(
          INGREDIENT_CODE_CON_CODE,
          containedPznCC.getCoding().stream()
              .filter(DeBasisCodeSystem.PZN::match)
              .map(Coding::getCode)
              .findFirst());
      givenValues.put(
          INGREDIENT_CODEABLE_CONCEPT_SYSTEM,
          containedPznCC.getCoding().stream()
              .map(Coding::getSystem)
              .filter(DeBasisCodeSystem.PZN::match)
              .findFirst());
      givenValues.put(INGREDIENT_CODE_CON_TEXT, Optional.ofNullable(containedPznCC.getText()));
    }
    givenValues.put(
        "Form",
        Optional.ofNullable(compoundingPrescription.getEpaMedication().getForm().getText()));
    givenValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                compoundingPrescription
                    .getEpaMedication()
                    .getAmount()
                    .getDenominator()
                    .getValue())));
    givenValues.put(
        MEDICATION_AMOUNT_NUMERATOR_EXTENSION_GESAMTMENGE,
        Optional.ofNullable(
            String.valueOf(
                compoundingPrescription
                    .getEpaMedication()
                    .getAmount()
                    .getNumerator()
                    .getExtensionFirstRep()
                    .getValue())));
    givenValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(
            compoundingPrescription.getEpaMedication().getAmount().getNumerator().getUnit()));
    givenValues.put(
        MEDICATION_CODE_TEXT,
        Optional.ofNullable(compoundingPrescription.getEpaMedication().getCode().getText()));
    return givenValues;
  }

  private static Map<String, Optional<String>> getGivenFreeTextValues(
      EpaOpProvidePrescription freeTextPrescription) {
    val givenValues = new HashMap<String, Optional<String>>();
    givenValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        freeTextPrescription.getEpaMedication().getExtension().stream()
            .filter(ex -> EpaMedStructDef.VACCINE_EXT.matches(ex.getUrl()))
            .map(ext -> ext.getValue().primitiveValue())
            .findFirst());
    givenValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        freeTextPrescription
            .getEpaMedication()
            .getExtensionsByUrl(EpaMedStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl())
            .stream()
            .map(ext -> ext.getValue().castToCoding(ext.getValue()).getCode())
            .findFirst());

    givenValues.put(
        MEDICATION_CODE_TEXT,
        Optional.ofNullable(freeTextPrescription.getEpaMedication().getCode().getText()));

    givenValues.put(
        "Form", Optional.ofNullable(freeTextPrescription.getEpaMedication().getForm().getText()));
    return givenValues;
  }

  private static boolean hasEqualValues(
      Map<String, Optional<String>> givenValues, Map<String, Optional<String>> expectedAttributes) {
    return expectedAttributes.entrySet().stream()
        .allMatch(
            entry -> {
              val res = givenValues.get(entry.getKey()).equals(entry.getValue());
              if (!res) {
                log.info(
                    "the given value {}: {} doesn´t match the expected value -> {}",
                    entry.getKey(),
                    entry.getValue(),
                    givenValues.get(entry.getKey()));
              }
              return res;
            });
  }

  private static Map<String, Optional<String>> getGivenIngredientValues(
      EpaOpProvidePrescription prescription) {
    val givenValues = new HashMap<String, Optional<String>>();
    val ingredientComp = prescription.getEpaMedication().getIngredientFirstRep();

    givenValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        prescription
            .getEpaMedication()
            .getExtensionsByUrl(EpaMedStructDef.VACCINE_EXT.getCanonicalUrl())
            .stream()
            .map(ex -> ex.getValue().primitiveValue())
            .findFirst());
    givenValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        prescription
            .getEpaMedication()
            .getExtensionsByUrl(EpaMedStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl())
            .stream()
            .map(ext -> ext.getValue().castToCoding(ext.getValue()).getCode())
            .findFirst());
    givenValues.put(
        NORMGROESSE,
        Optional.ofNullable(
            prescription
                .getEpaMedication()
                .getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse")
                .getValue()
                .primitiveValue()));
    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_DENOMINATOR_VALUE,
        Optional.ofNullable(ingredientComp.getStrength())
            .map(as -> String.valueOf(as.getDenominator().getValue())));
    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_UNIT,
        Optional.ofNullable(ingredientComp.getStrength().getNumerator().getUnit()));
    givenValues.put(
        MEDICATION_INGREDIENT_STRENGTH_NUMERATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(ingredientComp.getStrength().getNumerator().getValue())));
    givenValues.put(
        INGREDIENT_CODE_CON_TEXT,
        Optional.ofNullable(ingredientComp.getItemCodeableConcept().getText()));
    givenValues.put(
        INGREDIENT_CODEABLE_CONCEPT_SYSTEM,
        Optional.ofNullable(
            ingredientComp.getItemCodeableConcept().getCoding().get(0).getSystem()));
    givenValues.put(
        INGREDIENT_CODE_CON_CODE,
        Optional.ofNullable(ingredientComp.getItemCodeableConcept().getCoding().get(0).getCode()));
    givenValues.put(
        "Form", Optional.ofNullable(prescription.getEpaMedication().getForm().getText()));
    givenValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                prescription.getEpaMedication().getAmount().getDenominator().getValue())));
    givenValues.put(
        MEDICATION_AMOUNT_NUMERATOR_EXTENSION_GESAMTMENGE,
        Optional.ofNullable(
            String.valueOf(
                prescription
                    .getEpaMedication()
                    .getAmount()
                    .getNumerator()
                    .getExtensionFirstRep()
                    .getValue()
                    .primitiveValue())));
    givenValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(prescription.getEpaMedication().getAmount().getNumerator().getUnit()));
    return givenValues;
  }

  private static Map<String, Optional<String>> getGivenPZNValues(
      EpaOpProvidePrescription pznPrescription) {
    val givenValues = new HashMap<String, Optional<String>>();
    givenValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        Optional.ofNullable(
            pznPrescription
                .getEpaMedication()
                .getExtensionByUrl(EpaMedStructDef.VACCINE_EXT.getCanonicalUrl())
                .getValue()
                .primitiveValue()));
    givenValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        pznPrescription
            .getEpaMedication()
            .getExtensionsByUrl(EpaMedStructDef.DRUG_CATEGORY_EXT.getCanonicalUrl())
            .stream()
            .map(ext -> ext.getValue().castToCoding(ext.getValue()).getCode())
            .findFirst());
    givenValues.put(
        NORMGROESSE,
        Optional.ofNullable(
            pznPrescription
                .getEpaMedication()
                .getExtensionByUrl("http://fhir.de/StructureDefinition/normgroesse")
                .getValue()
                .primitiveValue()));

    givenValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(
            pznPrescription.getEpaMedication().getAmount().getNumerator().getUnit()));
    // Will remain empty for now, as no source information is available  ->
    // https://gematik.github.io/api-erp/erp_epa_mapping_details/KBV_PR_ERP_Medication_Compounding%7C1.1.0_KBV_PR_ERP_Medication_FreeText%7C1.1.0_KBV_PR_ERP_Medication_Ingredient%7C1.1.0_KBV_PR_ERP_Medication_PZN%7C1.1.0_to_EPAMedication%7C1.1.0.html
    givenValues.put(
        "Medication.amount.numerator.value",
        pznPrescription.getEpaMedication().getAmount().getNumerator().getExtension().stream()
            .filter(ex -> EpaMedStructDef.EXT_MED_PACKAGING_SIZE.matches(ex.getUrl()))
            .map(ext -> ext.getValue().primitiveValue())
            .findFirst());
    givenValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(
                pznPrescription.getEpaMedication().getAmount().getDenominator().getValue())));
    givenValues.put("Name", Optional.of(pznPrescription.getEpaMedication().getCode().getText()));
    givenValues.put(
        "System",
        pznPrescription.getEpaMedication().getCode().getCoding().stream()
            .map(Coding::getSystem)
            .filter(system -> !Strings.isNullOrEmpty(system))
            .findFirst());
    givenValues.put(
        "PZN",
        pznPrescription.getEpaMedication().getCode().getCoding().stream()
            .filter(DeBasisCodeSystem.PZN::match)
            .map(Coding::getCode)
            .findFirst());
    givenValues.put(
        "Medication.form.coding:kbvDarreichungsform.code",
        pznPrescription.getEpaMedication().getForm().getCoding().stream()
            .filter(KbvCodeSystem.DARREICHUNGSFORM::match)
            .map(Coding::getCode)
            .findFirst());
    return givenValues;
  }

  private static Map<String, Optional<String>> specificExpectedPZNAttributes(
      KbvErpMedication expectedMedication) {
    val expectedValues = new HashMap<String, Optional<String>>();
    expectedValues.put(
        MEDICATION_EXTENSION_IMPFSTOFF,
        Optional.of(String.valueOf(expectedMedication.isVaccine())));
    expectedValues.put(
        NORMGROESSE, Optional.ofNullable(expectedMedication.getStandardSize().getCode()));
    expectedValues.put(
        MEDICATION_EXTENSION_ARZNEIMITTELKATEGORIE,
        expectedMedication
            .getExtensionsByUrl(
                "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_Category")
            .stream()
            .map(ext -> ext.getValue().castToCoding(ext.getValue()).getCode())
            .findFirst());
    expectedValues.put("Name", Optional.ofNullable(expectedMedication.getCode().getText()));
    expectedValues.put(
        "System",
        expectedMedication.getCode().getCoding().stream()
            .filter(DeBasisCodeSystem.PZN::match)
            .map(Coding::getSystem)
            .findFirst());
    expectedValues.put("PZN", Optional.ofNullable(expectedMedication.getPzn().get(0)));
    expectedValues.put(
        "Medication.form.coding:kbvDarreichungsform.code",
        expectedMedication.getForm().getCoding().stream()
            .filter(KbvCodeSystem.DARREICHUNGSFORM::match)
            .map(Coding::getCode)
            .findFirst());
    expectedValues.put(
        MEDICATION_AMOUNT_DENOMINATOR_VALUE,
        Optional.ofNullable(
            String.valueOf(expectedMedication.getAmount().getDenominator().getValue())));
    // Will remain empty for now, as no source information is available  ->
    // https://gematik.github.io/api-erp/erp_epa_mapping_details/KBV_PR_ERP_Medication_Compounding%7C1.1.0_KBV_PR_ERP_Medication_FreeText%7C1.1.0_KBV_PR_ERP_Medication_Ingredient%7C1.1.0_KBV_PR_ERP_Medication_PZN%7C1.1.0_to_EPAMedication%7C1.1.0.html
    /*expectedValues.put(
    "Medication.amount.numerator.value",
    Optional.ofNullable(
        String.valueOf(
            expectedMedication
                .getAmount()
                .getNumerator()
                .getExtensionByUrl(
                    "https://fhir.kbv.de/StructureDefinition/KBV_EX_ERP_Medication_PackagingSize")
                .getValue())));*/
    expectedValues.put(
        MEDICATION_AMOUNT_NUMERATOR_UNIT,
        Optional.ofNullable(expectedMedication.getAmount().getNumerator().getUnit()));

    return expectedValues;
  }

  private static boolean isMedicationTypeOf(
      KbvErpMedication givenMedication, String kindOfMedication) {
    return givenMedication.getCode().getCoding().stream()
        .filter(
            code ->
                code.getSystem()
                    .equals("https://fhir.kbv.de/CodeSystem/KBV_CS_ERP_Medication_Type"))
        .anyMatch(code -> code.getCode().equalsIgnoreCase(kindOfMedication));
  }

  private static boolean isPznMedication(KbvErpMedication givenMedication) {
    return !givenMedication.getCode().getCoding().stream()
        .filter(DeBasisCodeSystem.PZN::match)
        .toList()
        .isEmpty();
  }

  private static boolean minimalSystemValidation(
      List<Coding> expectedCodings, List<Coding> epaCodings) {
    return epaCodings.stream()
        .map(
            epaCoding -> {
              val expectedCoding = findBySystem(epaCoding, expectedCodings);
              return Pair.of(epaCoding, expectedCoding);
            })
        .allMatch(
            pair ->
                pair.getRight()
                    .map(
                        expectedCoding -> expectedCoding.getCode().equals(pair.getLeft().getCode()))
                    .orElse(false));
  }

  public static VerificationStep<EpaOpProvidePrescription> emlMedicationRequestMapsTo(
      KbvErpMedicationRequest medicationRequest) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription -> {
          if (medicationRequest.getAuthoredOn().getTime()
              != prescription.getEpaMedicationRequest().getAuthoredOn().getTime()) return false;
          if (!medicationRequest
              .getStatus()
              .equals(prescription.getEpaMedicationRequest().getStatus())) return false;

          val medReqQuan = medicationRequest.getDispenseRequest().getQuantity();
          val prescrQuant =
              prescription.getEpaMedicationRequest().getDispenseRequest().getQuantity();
          return medReqQuan.getValue().equals(prescrQuant.getValue())
              && medReqQuan.getSystem().equals(prescrQuant.getSystem());
        };
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25946.getRequirement(),
            "Die Werte in MedicationRequest für AuthoredOn, DispenseRequestQuantity und Status"
                + " müssen übereinstimmen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlOrganisationHasSmcbTelematikId(
      TelematikID telematikId) {

    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription
                .getEpaOrganisation()
                .getTelematikId()
                .getValue()
                .equals(telematikId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25949.getRequirement(),
            format("Die EpaOrganisation muss die TelematikId {0} enthalten", telematikId))
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<EpaOpProvidePrescription> emlPractitionerHasHbaTelematikId(
      TelematikID hbaId) {
    Predicate<EpaOpProvidePrescription> predicate =
        prescription ->
            prescription.getEpaPractitioner().getTelematikId().getValue().equals(hbaId.getValue());
    return new VerificationStep.StepBuilder<EpaOpProvidePrescription>(
            EmlAfos.A_25949.getRequirement(),
            "Der EpaPractitioner muss die TelematikId: {0} besitzen")
        .predicate(predicate)
        .accept();
  }

  public static VerificationStep<List<EpaOpProvidePrescription>> emlDoesNotContainAnything() {
    Predicate<List<EpaOpProvidePrescription>> predicate = List::isEmpty;
    return new VerificationStep.StepBuilder<List<EpaOpProvidePrescription>>(
            EmlAfos.A_25951.getRequirement(), "Eml besitzt eine leere Liste")
        .predicate(predicate)
        .accept();
  }
}
