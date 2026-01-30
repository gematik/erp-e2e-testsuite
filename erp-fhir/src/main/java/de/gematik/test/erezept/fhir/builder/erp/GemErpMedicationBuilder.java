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

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.r4.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import jakarta.annotation.Nullable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
public abstract class GemErpMedicationBuilder<B extends GemErpMedicationBuilder<B>>
    extends ResourceBuilder<GemErpMedication, B> {

  protected final List<Coding> codes = new LinkedList<>();
  protected ErpWorkflowVersion version = ErpWorkflowVersion.getDefaultVersion();
  @Nullable protected String codeText;

  protected String manufacInstruction;
  // extensions
  protected Boolean isVaccine = false;
  protected EpaDrugCategory drugCategory;

  @Nullable protected String totalQuantity;
  protected String packaging;
  protected String packagingSize;
  @Nullable protected StandardSize normSizeCode;

  protected Darreichungsform kbvDdarreichungsform;
  protected String formText;

  protected Long amountNumerator;
  protected String amountNumeratorUnit;
  protected Long amountDenominator;

  protected List<Medication.MedicationIngredientComponent> ingredientComponentList =
      new LinkedList<>();

  protected String batchLotNumber;

  protected final List<Resource> containedResources = new ArrayList<>();

  public static GemErpMedicationCompoundingBuilder forCompounding() {
    return new GemErpMedicationCompoundingBuilder();
  }

  public static GemErpMedicationIngredientBuilder forIngredient() {
    return new GemErpMedicationIngredientBuilder();
  }

  public static GemErpMedicationKombiPkgBuilder forKombiPckg() {
    return new GemErpMedicationKombiPkgBuilder();
  }

  public static GemErpMedicationPZNBuilder forPZN() {
    return new GemErpMedicationPZNBuilder();
  }

  public static GemErpMedFreeTextBuilder forFreeText() {
    return new GemErpMedFreeTextBuilder();
  }

  public B manufacturingInstruction(String instruction) {
    this.manufacInstruction = instruction;
    return self();
  }

  public B version(ErpWorkflowVersion version) {
    this.version = version;
    return self();
  }

  public B ask(ASK ask) {
    this.codes.add(ask.asCoding());
    return self();
  }

  public B snomed(String snomedCode) {
    this.codes.add(CommonCodeSystem.SNOMED_SCT.asCoding(snomedCode));
    return self();
  }

  public B atc(ATC atc) {
    this.codes.add(atc.asCoding());
    return self();
  }

  public B category(EpaDrugCategory category) {
    this.drugCategory = category;
    return self();
  }

  public B isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }

  public B lotNumber(String lotNumber) {
    this.batchLotNumber = lotNumber;
    return self();
  }

  protected void applyCommonFields(GemErpMedication medication) {
    checkRequired();

    // from Versaion 1.6 a VersionId is mandatory
    if (version.isBiggerThan(ErpWorkflowVersion.V1_5)) medication.getMeta().setVersionId("1");

    medication.getContained().addAll(containedResources);

    Optional.ofNullable(drugCategory).ifPresent(dc -> medication.addExtension(dc.asExtension()));

    Optional.ofNullable(isVaccine)
        .ifPresent(
            vaccine ->
                medication.addExtension(
                    EpaMedicationStructDef.VACCINE_EXT.asBooleanExtension(vaccine)));

    Optional.ofNullable(manufacInstruction)
        .ifPresent(
            compInstr ->
                medication.addExtension(
                    EpaMedicationStructDef.MANUFACTURING_INSTRUCTION.asStringExtension(
                        manufacInstruction)));

    Optional.ofNullable(codeText).ifPresent(cT -> medication.getCode().setText(cT));
    Optional.ofNullable(normSizeCode)
        .ifPresent(size -> medication.addExtension(size.asExtension()));

    medication.getIngredient().addAll(this.ingredientComponentList);
    Optional.ofNullable(this.kbvDdarreichungsform)
        .ifPresent(form -> medication.getForm().addCoding(form.asCoding()));
    Optional.ofNullable(this.batchLotNumber)
        .ifPresent(lotNumber -> medication.getBatch().setLotNumber(lotNumber));
    Optional.ofNullable(this.formText).ifPresent(text -> medication.getForm().setText(text));
    Optional.ofNullable(this.packaging)
        .ifPresent(
            p ->
                medication
                    .getExtension()
                    .add(EpaMedicationStructDef.PACKAGING_EXTENSION.asStringExtension(p)));
    this.codes.forEach(code -> medication.getCode().addCoding(code));

    Optional.ofNullable(this.amountNumerator)
        .ifPresent(
            numerator -> {
              val amountRatio = medication.getAmount();

              amountRatio.getNumerator().setValue(numerator).setUnit(this.amountNumeratorUnit);
              Optional.ofNullable(totalQuantity)
                  .ifPresent(
                      tQ ->
                          amountRatio
                              .getNumerator()
                              .addExtension(
                                  EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT
                                      .asStringExtension((tQ))));
              Optional.ofNullable(this.packagingSize)
                  .ifPresent(
                      pSize ->
                          amountRatio
                              .getNumerator()
                              .addExtension(
                                  EpaMedicationStructDef.PACKAGING_SIZE_EXT.asStringExtension(
                                      pSize)));
              amountRatio.getDenominator().setValue(1);
            });

    // overwrite the default value of 1 of amountNumerator
    Optional.ofNullable(this.amountDenominator)
        .ifPresent(denom -> medication.getAmount().getDenominator().setValue(denom));
  }

  private void checkRequired() {
    validateCodesOrIngredients();
    validateAmountConsistency();
    validateNumeratorLength();
    validatePackagingSizeLength();
    validateTotalQuantityLength();
    validatePackagingLength();
    validatePackagingSizeWithAmount();
    validateTotalQuantityWithAmount();
  }

  private void validateCodesOrIngredients() {
    if (ingredientComponentList.isEmpty() && codes.isEmpty() && codeText == null) {
      throw new BuilderException(
          "epa-med-1: Medication codes, name, or ingredients must be specified codes.exists() or"
              + " ingredientList.exists()");
    }
  }

  private void validateAmountConsistency() {
    if (amountNumerator == null && amountDenominator != null) {
      throw new BuilderException(
          "Rule rat-1: 'Numerator and denominator SHALL both be present, or both are absent. If"
              + " both are absent, there SHALL be some extension present'");
    }
  }

  private void validateNumeratorLength() {
    if (amountNumerator != null && String.valueOf(amountNumerator).length() > 7) {
      throw new BuilderException("The amount numerator must be less than 7 digits");
    }
  }

  private void validatePackagingSizeLength() {
    if (packagingSize != null && packagingSize.length() > 7) {
      throw new BuilderException(
          format(
              "packagingSize is too long, max length is 7 characters, but was: {0} with value: {1}",
              packagingSize.length(), packagingSize));
    }
  }

  private void validateTotalQuantityLength() {
    if (totalQuantity != null && totalQuantity.length() > 7) {
      throw new BuilderException(
          format(
              "The totalQuantity is too long, max length is 7 characters, but was: {0} with value:"
                  + " {1}",
              totalQuantity.length(), totalQuantity));
    }
  }

  private void validatePackagingLength() {
    if (packaging != null && packaging.length() > 90) {
      throw new BuilderException(
          "The packaging value of"
              + " https://gematik.de/fhir/epa-medication/StructureDefinition/medication-formulation-packaging-extension"
              + " must be equal or less then 90 characters");
    }
  }

  private void validatePackagingSizeWithAmount() {
    if (packagingSize != null && amountNumerator == null) {
      throw new BuilderException(
          "PackagingSize can only be set if Amount and Numerator are set up, too");
    }
  }

  private void validateTotalQuantityWithAmount() {
    if (totalQuantity != null && amountNumerator == null) {
      throw new BuilderException(
          "TotalQuantity can only be set if Amount and Numerator are set up, too");
    }
  }
}
