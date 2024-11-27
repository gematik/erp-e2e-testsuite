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

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.EpaMedicationStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.resources.erp.GemErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.StringType;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class GemErpMedicationBuilder
    extends ResourceBuilder<GemErpMedication, GemErpMedicationBuilder> {

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

  public static GemErpMedicationBuilder builder() {
    return new GemErpMedicationBuilder();
  }

  public GemErpMedicationBuilder version(ErpWorkflowVersion version) {
    this.version = version;
    return this;
  }

  public GemErpMedicationBuilder category(EpaDrugCategory category) {
    this.drugCategory = category;
    return self();
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

  public GemErpMedicationBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return self();
  }

  public GemErpMedicationBuilder darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
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

    Optional.ofNullable(this.amountNumerator)
        .ifPresent(
            numerator -> {
              val amountRatio = medication.getAmount();
              amountRatio.addExtension(
                  EpaMedicationStructDef.TOTAL_QUANTITY_FORMULATION_EXT.getCanonicalUrl(),
                  new StringType(String.valueOf(numerator)));
              amountRatio.getNumerator().setValue(numerator).setUnit(this.amountNumeratorUnit);
              amountRatio.getDenominator().setValue(1);
            });
    Optional.ofNullable(this.batchLotNumber)
        .ifPresent(lotNumber -> medication.getBatch().setLotNumber(lotNumber));

    return medication;
  }
}
