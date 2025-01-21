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

import static de.gematik.test.erezept.fhir.parser.profiles.definitions.EpaMedicationStructDef.MED_INGREDIENT_DOSAGE_FORM_EXT;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.EpaMedicationStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import java.util.List;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GemErpMedicationBuilder
    extends ResourceBuilder<GemErpMedication, GemErpMedicationBuilder> {

  private static boolean isMedicationIngredient = false;
  private static boolean isMedicationCompounding = false;

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

  public static GemErpMedicationBuilder builder() {
    return new GemErpMedicationBuilder();
  }

  public static GemErpMedicationBuilder from(KbvErpMedication kbvMedication) {
    val builder = builder();
    builder.category(EpaDrugCategory.fromCode(kbvMedication.getCategoryFirstRep().getCode()));
    kbvMedication
        .getPznOptional()
        .ifPresent(pzn -> builder.pzn(pzn, kbvMedication.getMedicationName()));
    kbvMedication.getDarreichungsform().ifPresent(builder::darreichungsform);
    kbvMedication
        .getPackagingUnit()
        .ifPresentOrElse(
            unit -> builder.amount(kbvMedication.getMedicationAmount(), unit),
            () -> builder.amount(kbvMedication.getMedicationAmount()));
    kbvMedication.getFreeTextOptional().ifPresent(builder::freeText);

    kbvMedication.getTextInFormOptional().ifPresent(builder::formText);

    // for MedicationIngredient
    if (kbvMedication.getMeta().getProfile().stream()
        .anyMatch(
            prof ->
                prof.getValue()
                    .contains(KbvItaErpStructDef.MEDICATION_INGREDIENT.getCanonicalUrl()))) {
      isMedicationIngredient = true;

      kbvMedication
          .getIngredient()
          .forEach(ing -> builder.ingredientItemCodeConcept(ing.getItemCodeableConcept()));
      kbvMedication.getIngredientStrengthRatio().ifPresent(builder::ingredientStrength);
    }
    // for medicationCompounding
    if (kbvMedication.getMeta().getProfile().stream()
        .anyMatch(
            prof ->
                prof.getValue()
                    .contains(KbvItaErpStructDef.MEDICATION_COMPOUNDING.getCanonicalUrl()))) {
      isMedicationCompounding = true;
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

  public GemErpMedicationBuilder version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public GemErpMedicationBuilder category(EpaDrugCategory category) {
    this.drugCategory = category;
    return self();
  }

  public GemErpMedicationBuilder manufacturingInstruction(String instruction) {
    this.compInstruction = instruction;
    return self();
  }

  private GemErpMedicationBuilder ingredientItemCodeConcept(CodeableConcept codeableConcept) {
    this.ingredCodeableConcept = codeableConcept;
    return self();
  }

  public GemErpMedicationBuilder packaging(String packaging) {
    this.packaging = packaging;
    return self();
  }

  private void ingredientStrength(Ratio ratio) {
    this.ingredientStrength = ratio;
  }

  public GemErpMedicationBuilder pzn(String pzn) {
    return pzn(PZN.from(pzn));
  }

  public GemErpMedicationBuilder pzn(PZN pzn) {
    this.pzn = pzn;
    return this;
  }

  public GemErpMedicationBuilder pzn(String pzn, String medicationName) {
    return pzn(PZN.from(pzn), medicationName);
  }

  public GemErpMedicationBuilder pzn(PZN pzn, String medicationName) {
    this.medicationName = medicationName;
    return this.pzn(pzn);
  }

  public GemErpMedicationBuilder freeText(String freeText) {
    this.freeText = freeText;
    return this;
  }

  public GemErpMedicationBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return self();
  }

  public GemErpMedicationBuilder darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
    return self();
  }

  public GemErpMedicationBuilder formText(String formText) {
    this.formText = formText;
    return self();
  }

  public GemErpMedicationBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return this;
  }

  public GemErpMedicationBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public GemErpMedicationBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return this;
  }

  public GemErpMedicationBuilder lotNumber(String lotNumber) {
    this.batchLotNumber = lotNumber;
    return this;
  }

  @Override
  public GemErpMedication build() {
    // TODO: will be available after final move to bricks builder
    // val medication = this.createResource(GemErpMedication::new, ErpWorkflowStructDef.MEDICATION,
    // version);
    val medication = new GemErpMedication();
    val profile = ErpWorkflowStructDef.MEDICATION.asCanonicalType(version, true);
    val meta = new Meta().setProfile(List.of(profile));
    medication.setId(this.getResourceId()).setMeta(meta);

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

    Optional.ofNullable(this.pzn)
        .ifPresent(
            presentPzn -> medication.setCode(presentPzn.asNamedCodeable(this.medicationName)));

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
                      EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT.getCanonicalUrl(),
                      new StringType(String.valueOf(numerator)));
              amountRatio.getDenominator().setValue(1);
            });
    Optional.ofNullable(this.batchLotNumber)
        .ifPresent(lotNumber -> medication.getBatch().setLotNumber(lotNumber));

    // forMedicationCompounding
    Optional.ofNullable(this.packaging)
        .ifPresent(
            p ->
                medication.addExtension(
                    EpaMedicationStructDef.PACKAGING_EXTENSION.getCanonicalUrl(),
                    new StringType(p)));
    Optional.ofNullable(this.compInstruction)
        .ifPresent(
            compInstr ->
                medication.addExtension(
                    EpaMedicationStructDef.MANUFACTURING_INSTRUCTION.getCanonicalUrl(),
                    new StringType(compInstruction)));

    Optional.ofNullable(this.ingredCodeableConcept)
        .ifPresent(ingredientCod -> medication.getIngredientFirstRep().setItem(ingredientCod));

    if (isMedicationIngredient && ingredientStrength != null) {
      return fillMissingInIngredStrength(ingredientStrength, medication);
    }
    if (isMedicationCompounding && ingredientStrength != null) {
      return fillMissingInIngredStrength(ingredientStrength, medication);
    }
    return medication;
  }

  private GemErpMedication fillMissingInIngredStrength(
      Ratio ingredientStrength, GemErpMedication medication) {

    val absentNotice = "http://hl7.org/fhir/StructureDefinition/data-absent-reason";
    val absenExt = new Extension();
    absenExt.setValue(new CodeType("unknown")).setUrl(absentNotice);
    if (ingredientStrength.getNumerator() != null
        && !ingredientStrength.getNumerator().hasSystem()) {
      ingredientStrength.getNumerator().getSystemElement().addExtension(absenExt);
    }
    if (ingredientStrength.getNumerator() != null && !ingredientStrength.getNumerator().hasCode()) {
      ingredientStrength.getNumerator().setCode("");
      ingredientStrength.getNumerator().getCodeElement().addExtension(absenExt);
    }
    if (ingredientStrength.getNumerator() != null
        && !ingredientStrength.getNumerator().hasValue()) {
      ingredientStrength.getNumerator().setValue(null);
      ingredientStrength.getNumerator().getValueElement().addExtension(absenExt);
    }
    if (ingredientStrength.getDenominator() != null
        && !ingredientStrength.getDenominator().hasSystem()) {
      ingredientStrength.getDenominator().setSystem("");
      ingredientStrength.getDenominator().getSystemElement().addExtension(absenExt);
    }
    if (ingredientStrength.getDenominator() != null
        && !ingredientStrength.getDenominator().hasCode()) {
      var code = new Coding();
      code.addExtension(absenExt);
      ingredientStrength.getDenominator().setCode("");
      ingredientStrength.getDenominator().getCodeElement().addExtension(absenExt);
    }
    if (ingredientStrength.getDenominator() != null
        && !ingredientStrength.getDenominator().hasValue()) {
      ingredientStrength.getDenominator().setValue(null);
      ingredientStrength.getDenominator().getValueElement().addExtension(absenExt);
    }
    medication
        .getIngredientFirstRep()
        .setStrength(ingredientStrength)
        .addExtension(
            new Extension()
                .setUrl(MED_INGREDIENT_DOSAGE_FORM_EXT.getCanonicalUrl())
                .setValue(new StringType(formText)));

    return medication;
  }
}
