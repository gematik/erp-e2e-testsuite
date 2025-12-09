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

package de.gematik.test.erezept.eml.fhir.r4;

import static de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef.PHARMACEUTICAL_PROD;
import static de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems.BFARM_CS_MED_REF;

import de.gematik.bbriccs.fhir.builder.ResourceBuilder;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationVersion;
import de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Medication;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class EpaPharmaceuticalProdBuilder
    extends ResourceBuilder<EpaPharmaceuticalProduct, EpaPharmaceuticalProdBuilder> {

  private EpaMedicationVersion version = EpaMedicationVersion.getDefaultVersion();
  private String textInCoding;
  private final CodeableConcept cc = new CodeableConcept();
  protected Medication.MedicationIngredientComponent ingredientComponent;
  private boolean withVersion = true;

  public static EpaPharmaceuticalProdBuilder builder() {
    return new EpaPharmaceuticalProdBuilder();
  }

  public EpaPharmaceuticalProdBuilder codingText(String textInCoding) {
    this.textInCoding = textInCoding;
    return this;
  }

  public EpaPharmaceuticalProdBuilder withoutVersion() {
    this.withVersion = false;
    return this;
  }

  public EpaPharmaceuticalProdBuilder version(EpaMedicationVersion version) {
    this.version = version;
    return this;
  }

  public EpaPharmaceuticalProdBuilder productKey(String code, String display) {
    cc.addCoding(BFARM_CS_MED_REF.asCoding(code).setDisplay(display));
    return this;
  }

  public EpaPharmaceuticalProdBuilder askCode(ASK ask) {
    cc.addCoding(ask.asCoding());
    return this;
  }

  public EpaPharmaceuticalProdBuilder atcCode(ATC atc) {
    cc.addCoding(atc.asCoding());
    return this;
  }

  public EpaPharmaceuticalProdBuilder snomedCode(String snomedCode) {
    cc.addCoding(UseFulCodeSystems.SNOMED_SCT.asCoding(snomedCode));
    return this;
  }

  /**
   * to build the ingredient component please Use IngredientCodeBuilder instead of putting single
   * values into the EpaMedPharmaceuticalProdBuilder
   *
   * @param ingredientComponent
   * @return EpaMedPharmaceuticalProdBuilder
   */
  public EpaPharmaceuticalProdBuilder ingredientComponent(
      Medication.MedicationIngredientComponent ingredientComponent) {
    this.ingredientComponent = ingredientComponent;
    return self();
  }

  @Override
  public EpaPharmaceuticalProduct build() {
    EpaPharmaceuticalProduct epaMed;
    if (withVersion) {
      epaMed = this.createResource(EpaPharmaceuticalProduct::new, PHARMACEUTICAL_PROD, version);
    } else {
      epaMed = this.createResource(EpaPharmaceuticalProduct::new, PHARMACEUTICAL_PROD);
    }
    Optional.ofNullable(cc).ifPresent(epaMed::setCode);
    Optional.ofNullable(this.textInCoding).ifPresent(text -> epaMed.getCode().setText(text));
    Optional.ofNullable(this.ingredientComponent).ifPresent(epaMed::addIngredient);

    return epaMed;
  }
}
