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
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.BaseMedicationType;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationPZNBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationPZNBuilder> {

  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();

  private BaseMedicationType baseMedicationType = BaseMedicationType.MEDICAL_PRODUCT;
  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private StandardSize normgroesse = StandardSize.NB;
  private Darreichungsform darreichungsform = Darreichungsform.TAB;

  private final List<Extension> extensions = new LinkedList<>();

  private PZN pzn;
  private String medicationName;

  private String packagingSize;
  private long amountNumerator;
  private String amountNumeratorUnit;

  public static KbvErpMedicationPZNBuilder builder() {
    return new KbvErpMedicationPZNBuilder();
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvErpMedicationPZNBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return this;
  }

  public KbvErpMedicationPZNBuilder type(BaseMedicationType type) {
    this.baseMedicationType = type;
    return this;
  }

  public KbvErpMedicationPZNBuilder category(MedicationCategory category) {
    this.category = category;
    return this;
  }

  public KbvErpMedicationPZNBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return this;
  }

  public KbvErpMedicationPZNBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return this;
  }

  public KbvErpMedicationPZNBuilder darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
    return this;
  }

  public KbvErpMedicationPZNBuilder pzn(String pzn, String medicationName) {
    return pzn(PZN.from(pzn), medicationName);
  }

  public KbvErpMedicationPZNBuilder pzn(PZN pzn, String medicationName) {
    this.pzn = pzn;
    this.medicationName = medicationName;
    return this;
  }

  public KbvErpMedicationPZNBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public KbvErpMedicationPZNBuilder amount(long numerator, String unit) {
    this.amountNumerator = numerator;
    this.amountNumeratorUnit = unit;
    return this;
  }

  public KbvErpMedicationPZNBuilder packagingSize(String packagingSize) {
    return packagingSize(packagingSize, "ml");
  }

  public KbvErpMedicationPZNBuilder packagingSize(String packagingSize, String unit) {
    this.packagingSize = packagingSize;
    this.amountNumeratorUnit = unit;
    return this;
  }

  @Override
  public KbvErpMedication build() {
    val medication =
        this.createResource(
            KbvErpMedication::new, KbvItaErpStructDef.MEDICATION_PZN, kbvItaErpVersion);
    this.defaultAmount();

    val amount = new Ratio();
    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      amount.getNumerator().setValue(amountNumerator);
    } else {
      extensions.add(baseMedicationType.asExtension());

      val numerator = amount.getNumerator();
      val psizeValue =
          Optional.ofNullable(this.packagingSize).orElse(String.valueOf(amountNumerator));
      numerator.addExtension(KbvItaErpStructDef.PACKAGING_SIZE.asStringExtension(psizeValue));
    }
    amount.getNumerator().setUnit(amountNumeratorUnit);
    amount.getDenominator().setValue(1); // always 1 defined by the Profile (??)

    // handle default values
    extensions.add(category.asExtension());
    extensions.add(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));
    extensions.add(normgroesse.asExtension());
    medication.setExtension(extensions);

    medication
        .setCode(pzn.asNamedCodeable(medicationName))
        .setForm(darreichungsform.asCodeableConcept())
        .setAmount(amount);

    return medication;
  }

  /**
   * The amount is quite tricky: - If not given by the user, make it a default with 10,1 - Use the
   * unit from darreichungsform - set a default code to Stk for now
   *
   * <p>See also the comment in this.amount(..)
   */
  private void defaultAmount() {
    if (amountNumerator <= 0) {
      this.amountNumerator = 1;
      this.amountNumeratorUnit = "Stk";
    }
  }
}
