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

package de.gematik.test.erezept.fhir.builder.kbv;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.MedicationType;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import lombok.val;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Extension;

public class KbvErpMedicationFreeTextBuilder
    extends ResourceBuilder<KbvErpMedication, KbvErpMedicationFreeTextBuilder> {

  private final List<Extension> extensions = new LinkedList<>();
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private String darreichungsform;
  private boolean isVaccine = false;
  private MedicationCategory category = MedicationCategory.C_00;
  private String nameOreFreetext;

  public static KbvErpMedicationFreeTextBuilder builder() {
    return new KbvErpMedicationFreeTextBuilder();
  }

  public KbvErpMedicationFreeTextBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    return self();
  }

  /**
   * Angabe der Darreichungsform als Freitext maximum of 30 digits allowed
   *
   * @param darreichung as String
   * @return builder
   */
  public KbvErpMedicationFreeTextBuilder darreichung(String darreichung) {
    this.darreichungsform = darreichung;
    return self();
  }

  /**
   * FreeText in Coding as name or declaration max 500 digits allowed
   *
   * @param freeText
   * @return
   */
  public KbvErpMedicationFreeTextBuilder freeText(String freeText) {
    this.nameOreFreetext = freeText;
    return self();
  }

  public KbvErpMedicationFreeTextBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }

  public KbvErpMedicationFreeTextBuilder category(MedicationCategory category) {
    this.category = category;
    return self();
  }

  @Override
  public KbvErpMedication build() {
    checkRequired();
    val medication =
        this.createResource(
            KbvErpMedication::new, KbvItaErpStructDef.MEDICATION_FREETEXT, kbvItaErpVersion);
    medication.setCode(MedicationType.FREETEXT.asCodeableConcept());
    medication.getCode().setText(nameOreFreetext);
    Optional.ofNullable(darreichungsform)
        .ifPresent(df -> medication.setForm(new CodeableConcept().setText(df)));
    extensions.add(category.asExtension());
    extensions.add(KbvItaErpStructDef.MEDICATION_VACCINE.asBooleanExtension(isVaccine));
    medication.setExtension(extensions);
    return medication;
  }

  private void checkRequired() {
    this.checkRequired(
        nameOreFreetext, "Medication FreeText Ressource requires a Name or description");
  }
}
