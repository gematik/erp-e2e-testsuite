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

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.MED_INGREDIENT_DOSAGE_FORM_EXT;
import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedPznIngredientBuilder;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Ratio;
import org.hl7.fhir.r4.model.Reference;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GemErpMedicationPZNBuilderORIGINAL_BUILDER
    extends ResourceBuilder<GemErpMedication, GemErpMedicationPZNBuilderORIGINAL_BUILDER> {

  private boolean isMedicationIngredient = false;
  private boolean isMedicationCompounding = false;

  @Nullable String compInstruction;
  @Nullable String formText;
  private ErpWorkflowVersion version = ErpWorkflowVersion.getDefaultVersion();
  private EpaDrugCategory drugCategory;
  private PZN pzn;
  private String medicationName;
  private Boolean isVaccine;
  private StandardSize normgroesse;
  private Darreichungsform darreichungsform;
  private Long amountNumerator;
  private String amountNumeratorUnit;
  private String batchLotNumber;
  private String freeText;
  @Nullable private CodeableConcept ingredCodeableConcept;
  @Nullable private Ratio ingredientStrength;
  @Nullable private String packaging;

  public static GemErpMedicationPZNBuilderORIGINAL_BUILDER builder() {
    return new GemErpMedicationPZNBuilderORIGINAL_BUILDER();
  }

  public static GemErpMedicationPZNBuilderORIGINAL_BUILDER from(KbvErpMedication kbvMedication) {
    val builder = builder();
    builder.category(EpaDrugCategory.fromCode(kbvMedication.getCategoryFirstRep().getCode()));
    kbvMedication
        .getPznOptional()
        .ifPresent(pzn -> builder.pzn(PZN.from(pzn.getValue()), kbvMedication.getMedicationName()));

    kbvMedication.getDarreichungsform().stream().findFirst().ifPresent(builder::darreichungsform);
    kbvMedication
        .getPackagingUnit()
        .ifPresentOrElse(
            unit -> builder.amount(kbvMedication.getPackagingSizeOrEmpty(), unit),
            () -> builder.amount(kbvMedication.getPackagingSizeOrEmpty()));
    kbvMedication.getFreeTextOptional().ifPresent(builder::freeText);

    kbvMedication.getTextInFormOptional().ifPresent(builder::formText);

    // for MedicationIngredient
    if (KbvItaErpStructDef.MEDICATION_INGREDIENT.matches(kbvMedication)) {
      builder.isIngredient();
      kbvMedication
          .getIngredient()
          .forEach(ing -> builder.ingredientItemCodeConcept(ing.getItemCodeableConcept()));
      kbvMedication.getIngredientStrengthRatio().ifPresent(builder::ingredientStrength);
    }

    // for medicationCompounding
    if (KbvItaErpStructDef.MEDICATION_COMPOUNDING.matches(kbvMedication)) {
      builder.isCompounding();
      kbvMedication
          .getIngredient()
          .forEach(ing -> builder.ingredientItemCodeConcept(ing.getItemCodeableConcept()));

      kbvMedication.getManufactoringInstrOptional().ifPresent(builder::manufacturingInstruction);
      kbvMedication.getPackagingOptional().ifPresent(builder::packaging);
      kbvMedication.getIngredientStrengthRatio().ifPresent(builder::ingredientStrength);
    }
    return builder
        .normgroesse(kbvMedication.getStandardSize())
        .isVaccine(kbvMedication.isVaccine());
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER category(EpaDrugCategory category) {
    this.drugCategory = category;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER manufacturingInstruction(String instruction) {
    this.compInstruction = instruction;
    return self();
  }

  private GemErpMedicationPZNBuilderORIGINAL_BUILDER ingredientItemCodeConcept(
      CodeableConcept codeableConcept) {
    this.ingredCodeableConcept = codeableConcept;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER packaging(String packaging) {
    this.packaging = packaging;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER isCompounding() {
    this.isMedicationCompounding = true;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER isIngredient() {
    this.isMedicationIngredient = true;
    return this;
  }

  private void ingredientStrength(Ratio ratio) {
    this.ingredientStrength = ratio;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER pzn(String pzn) {
    return pzn(PZN.from(pzn));
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER pzn(PZN pzn) {
    this.pzn = pzn;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER pzn(String pzn, String medicationName) {
    return pzn(PZN.from(pzn), medicationName);
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER pzn(PZN pzn, String medicationName) {
    this.medicationName = medicationName;
    return this.pzn(pzn);
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER freeText(String freeText) {
    this.freeText = freeText;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER normgroesse(StandardSize size) {
    this.normgroesse = size;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER formText(String formText) {
    this.formText = formText;
    return self();
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return this;
  }

  public GemErpMedicationPZNBuilderORIGINAL_BUILDER lotNumber(String lotNumber) {
    this.batchLotNumber = lotNumber;
    return this;
  }

  @Override
  public GemErpMedication build() {
    checkRequired();
    val medication =
        this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION, version);

    Optional.ofNullable(this.drugCategory)
        .ifPresent(dc -> medication.addExtension(dc.asExtension()));
    Optional.ofNullable(this.isVaccine)
        .ifPresent(
            vaccine ->
                medication.addExtension(
                    EpaMedicationStructDef.VACCINE_EXT.asBooleanExtension(vaccine)));
    Optional.ofNullable(this.normgroesse)
        .ifPresent(size -> medication.addExtension(size.asExtension()));
    Optional.ofNullable(this.darreichungsform)
        .ifPresent(form -> medication.setForm(form.asCodeableConcept()));
    if (!isMedicationCompounding) {
      Optional.ofNullable(this.pzn)
          .ifPresent(
              presentPzn -> medication.setCode(presentPzn.asNamedCodeable(this.medicationName)));
    }
    Optional.ofNullable(this.freeText)
        .ifPresent(presentFreeText -> medication.getCode().setText(presentFreeText));

    Optional.ofNullable(this.amountNumerator)
        .ifPresent(
            numerator -> {
              val amountRatio = medication.getAmount();

              amountRatio.getNumerator().setValue(numerator).setUnit(this.amountNumeratorUnit);
              amountRatio
                  .getNumerator()
                  .addExtension(
                      EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT.asStringExtension(
                          String.valueOf(numerator)));
              amountRatio.getDenominator().setValue(1);
            });
    Optional.ofNullable(this.batchLotNumber)
        .ifPresent(lotNumber -> medication.getBatch().setLotNumber(lotNumber));

    Optional.ofNullable(this.packaging)
        .ifPresent(
            p ->
                medication.addExtension(
                    EpaMedicationStructDef.PACKAGING_EXTENSION.asStringExtension(p)));

    Optional.ofNullable(this.compInstruction)
        .ifPresent(
            compInstr ->
                medication.addExtension(
                    EpaMedicationStructDef.MANUFACTURING_INSTRUCTION.asStringExtension(
                        compInstruction)));

    Optional.ofNullable(this.ingredCodeableConcept)
        .ifPresent(ingredientCod -> medication.getIngredientFirstRep().setItem(ingredientCod));

    if (isMedicationIngredient) {
      fillMissingInIngredientStrength(ingredientStrength, medication);
    }
    if (isMedicationCompounding) {
      if (medication.getIngredientFirstRep().getItemCodeableConcept().getCoding().stream()
          .anyMatch(DeBasisProfilCodeSystem.PZN::matches)) {

        // can cause issues when default version is still 1.3.0
        val epaPznIngreMed =
            EpaMedPznIngredientBuilder.builder()
                .withPzn(medication.getIngredientFirstRep().getItemCodeableConcept())
                .build();

        medication.getContained().add(epaPznIngreMed);
        medication.getIngredientFirstRep().setItem(new Reference("#" + epaPznIngreMed.getId()));
      }
      // in case of MedicationCompounding no other code in an Ingredient component is allowed
      fillMissingInIngredientStrength(ingredientStrength, medication);
    }
    return medication;
  }

  private void fillMissingInIngredientStrength(
      Ratio ingredientStrength, GemErpMedication medication) {

    val absentExt = HL7StructDef.DATA_ABSENT_REASON.asCodeExtension("unknown");

    List.of(ingredientStrength.getNumerator(), ingredientStrength.getDenominator())
        .forEach(
            quantity -> {
              if (!quantity.hasSystem()) {
                quantity.getSystemElement().addExtension(absentExt);
              }
              if (!quantity.hasCode()) {
                quantity.getCodeElement().addExtension(absentExt);
              }
              if (!quantity.hasValue()) {
                quantity.getValueElement().addExtension(absentExt);
              }
            });

    medication
        .getIngredientFirstRep()
        .setStrength(ingredientStrength)
        .addExtension(MED_INGREDIENT_DOSAGE_FORM_EXT.asStringExtension(formText));
  }

  private void checkRequired() {
    if (isMedicationCompounding && isMedicationIngredient) {
      val prefixedErrorMsg =
          format("The medication can only be an Ingredient or Compounding Medication, not both");
      throw new BuilderException(prefixedErrorMsg);
    }
  }
}
