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

package de.gematik.test.erezept.fhir.builder.eu;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.version.EuVersion;
import de.gematik.test.erezept.fhir.r4.eu.EuMedication;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import jakarta.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Coding;

public class EuMedicationBuilder extends ResourceBuilder<EuMedication, EuMedicationBuilder> {

  private EuVersion version = EuVersion.getDefaultVersion();

  private final List<Coding> codes = new LinkedList<>();
  @Nullable private String codeText;

  private String manufacInstruction;

  // extentions
  private Boolean isVaccine = false;
  private EpaDrugCategory drugCategory;

  @Nullable private String totalQuantity;
  private String packaging;
  private String packagingSize;
  @Nullable private StandardSize normSizeCode;

  private Darreichungsform kbvDdarreichungsform;
  private String formText;

  private Long amountNumerator;
  private String amountNumeratorUnit;
  private Long amountDenominator;

  private String batchLotNumber;

  public static EuMedicationBuilder builder() {
    return new EuMedicationBuilder();
  }

  public EuMedicationBuilder pzn(PZN pzn, String name) {
    this.codes.add(pzn.asCoding().setDisplay(name));
    return this;
  }

  public EuMedicationBuilder pzn(String pzn) {
    return pzn(PZN.from(pzn));
  }

  public EuMedicationBuilder pzn(PZN pzn) {
    return pzn(pzn, "unknown");
  }

  public EuMedicationBuilder totalQuantity(String totalQuantity) {
    this.totalQuantity = totalQuantity;
    return this;
  }

  public EuMedicationBuilder normgroesse(StandardSize normSizeCode) {
    this.normSizeCode = normSizeCode;
    return this;
  }

  public EuMedicationBuilder packaging(String packagingInExtensions) {
    this.packaging = packagingInExtensions;
    return this;
  }

  public EuMedicationBuilder packagingSize(String packagingSize) {
    this.packagingSize = packagingSize;
    return this;
  }

  public EuMedicationBuilder darreichungsform(Darreichungsform form) {
    this.kbvDdarreichungsform = form;
    return this;
  }

  public EuMedicationBuilder formText(String formText) {
    this.formText = formText;
    return this;
  }

  public EuMedicationBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public EuMedicationBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return this;
  }

  public EuMedicationBuilder amountDenominator(long denominator) {
    this.amountDenominator = denominator;
    return this;
  }

  public EuMedicationBuilder codeText(String codeText) {
    this.codeText = codeText;
    return this;
  }

  public EuMedicationBuilder manufacturingInstruction(String instruction) {
    this.manufacInstruction = instruction;
    return this;
  }

  public EuMedicationBuilder version(EuVersion version) {
    this.version = version;
    return this;
  }

  public EuMedicationBuilder ask(ASK ask) {
    this.codes.add(ask.asCoding());
    return this;
  }

  public EuMedicationBuilder snomed(String snomedCode) {
    this.codes.add(CommonCodeSystem.SNOMED_SCT.asCoding(snomedCode));
    return this;
  }

  public EuMedicationBuilder atc(ATC atc) {
    this.codes.add(atc.asCoding().setVersion("1.0"));
    return this;
  }

  public EuMedicationBuilder category(EpaDrugCategory category) {
    this.drugCategory = category;
    return this;
  }

  public EuMedicationBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return this;
  }

  public EuMedicationBuilder lotNumber(String lotNumber) {
    this.batchLotNumber = lotNumber;
    return this;
  }

  @Override
  public EuMedication build() {
    val medication =
        this.createResource(EuMedication::new, GemErpEuStructDef.EU_MEDICATION, version);

    Optional.ofNullable(this.drugCategory)
        .ifPresent(dc -> medication.addExtension(dc.asExtension()));

    Optional.ofNullable(this.isVaccine)
        .ifPresent(
            vaccine ->
                medication.addExtension(
                    EpaMedicationStructDef.VACCINE_EXT.asBooleanExtension(vaccine)));

    Optional.ofNullable(this.manufacInstruction)
        .ifPresent(
            compInstr ->
                medication.addExtension(
                    EpaMedicationStructDef.MANUFACTURING_INSTRUCTION.asStringExtension(
                        manufacInstruction)));

    Optional.ofNullable(this.codeText).ifPresent(cT -> medication.getCode().setText(cT));
    Optional.ofNullable(this.normSizeCode)
        .ifPresent(size -> medication.addExtension(size.asExtension()));

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
    return medication;
  }
}
