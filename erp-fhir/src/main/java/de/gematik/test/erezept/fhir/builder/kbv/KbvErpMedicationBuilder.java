/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.builder.BuilderUtil;
import de.gematik.test.erezept.fhir.parser.profiles.ErpStructureDefinition;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.util.LinkedList;
import java.util.List;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Meta;
import org.hl7.fhir.r4.model.Ratio;

public class KbvErpMedicationBuilder extends AbstractResourceBuilder<KbvErpMedicationBuilder> {

  private MedicationCategory category = MedicationCategory.C_00;
  private boolean isVaccine = false;
  private StandardSize normgroesse = StandardSize.NB;
  private Darreichungsform darreichungsform = Darreichungsform.TAB;

  private final List<Extension> extensions = new LinkedList<>();

  private PZN pzn;
  private String medicationName;
  private Ratio amount;

  public static KbvErpMedicationBuilder builder() {
    return new KbvErpMedicationBuilder();
  }

  public static KbvErpMedicationBuilder faker() {
    return faker(fakerPzn());
  }

  public static KbvErpMedicationBuilder faker(String pzn) {
    return faker(pzn, fakerDrugName());
  }

  public static KbvErpMedicationBuilder faker(String pzn, String name) {
    return faker(pzn, name, fakerValueSet(MedicationCategory.class));
  }

  public static KbvErpMedicationBuilder faker(
      String pzn, String name, MedicationCategory category) {
    return builder()
        .category(category)
        .isVaccine(false)
        .normgroesse(fakerValueSet(StandardSize.class))
        .darreichungsform(fakerValueSet(Darreichungsform.class))
        .amount(fakerAmount(), "Stk")
        .pzn(pzn, name);
  }

  public KbvErpMedicationBuilder category(MedicationCategory category) {
    this.category = category;
    return self();
  }

  public KbvErpMedicationBuilder isVaccine(boolean isVaccine) {
    this.isVaccine = isVaccine;
    return self();
  }

  public KbvErpMedicationBuilder normgroesse(StandardSize size) {
    this.normgroesse = size;
    return self();
  }

  public KbvErpMedicationBuilder darreichungsform(Darreichungsform form) {
    this.darreichungsform = form;
    return self();
  }

  public KbvErpMedicationBuilder pzn(@NonNull String pzn, @NonNull String medicationName) {
    return pzn(PZN.from(pzn), medicationName);
  }

  public KbvErpMedicationBuilder pzn(@NonNull PZN pzn, @NonNull String medicationName) {
    this.pzn = pzn;
    this.medicationName = medicationName;
    return self();
  }

  public KbvErpMedicationBuilder amount(long numerator) {
    return this.amount(numerator, "Stk");
  }

  public KbvErpMedicationBuilder amount(long numerator, String unit) {
    // probably some library required for mapping Darreichungsform to ucum e.g. Eclipse UOMo
    // https://ucum.org/trac
    // NOTE: MedicationRequest has also a quantity: possible to re-use?
    this.amount = new Ratio();
    amount.getDenominator().setValue(1); // always 1 defined by the Profile (??)
    amount.getNumerator().setValue(numerator).setUnit(unit);
    return self();
  }

  public KbvErpMedication build() {
    val medication = new KbvErpMedication();

    val profile = ErpStructureDefinition.KBV_MEDICATION_PZN.asCanonicalType();
    val meta = new Meta().setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    medication.setId(this.getResourceId()).setMeta(meta);
    this.defaultAmount();

    // handle default values
    extensions.add(category.asExtension());
    extensions.add(BuilderUtil.vaccine(isVaccine));
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
    if (amount == null) {
      this.amount = new Ratio();
      amount.getDenominator().setValue(1);
      amount.getNumerator().setValue(1).setUnit("Stk");
    }
  }
}
