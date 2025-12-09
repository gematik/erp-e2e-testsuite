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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.test.erezept.eml.fhir.profile.UseFulCodeSystems;
import java.util.Optional;
import javax.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Medication;

@Slf4j
@ResourceDef(name = "Medication")
@SuppressWarnings({"java:S110"})
public class EpaPharmaceuticalProduct extends EpaMedication {

  public Optional<String> getProductKey() {
    return getCode().getCoding().stream()
        .filter(UseFulCodeSystems.BFARM_CS_MED_REF::matches)
        .map(Coding::getCode)
        .findFirst();
  }

  public Optional<String> getProductKeyDisplay() {
    return getCode().getCoding().stream()
        .filter(UseFulCodeSystems.BFARM_CS_MED_REF::matches)
        .map(Coding::getDisplay)
        .findFirst();
  }

  public Optional<ASK> getAskCode() {
    return getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ASK::matches)
        .findFirst()
        .map(ASK::from);
  }

  public Optional<ATC> getAtcCode() {
    return getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ATC::matches)
        .map(Coding::getCode)
        .findFirst()
        .map(ATC::from);
  }

  public Optional<String> getSnomedCode() {
    return getCode().getCoding().stream()
        .filter(UseFulCodeSystems.SNOMED_SCT::matches)
        .map(Coding::getCode)
        .findFirst();
  }

  public Optional<Medication.MedicationIngredientComponent> getFirstIngredient() {
    return getIngredient().stream().findFirst();
  }

  public Optional<String> getIngredientAtcCode() {
    return getFirstIngredient()
        .flatMap(
            ing ->
                ing.getItemCodeableConcept().getCoding().stream()
                    .filter(c -> c.getSystem().contains("atc"))
                    .map(Coding::getCode)
                    .findFirst());
  }

  public Optional<String> getIngredientAtcDisplay() {
    return getIngredient().stream()
        .flatMap(it -> it.getItemCodeableConcept().getCoding().stream())
        .filter(DeBasisProfilCodeSystem.ATC::matches)
        .map(Coding::getDisplay)
        .findFirst();
  }

  public Optional<Double> getIngredientStrengthNumeratorValue() {
    return getFirstIngredient()
        .map(ing -> ing.getStrength().getNumerator().getValue().doubleValue());
  }

  public Optional<String> getIngredientStrengthNumeratorUnit() {
    return getFirstIngredient().map(ing -> ing.getStrength().getNumerator().getUnit());
  }

  public Optional<Double> getIngredientStrengthDenominatorValue() {
    return getFirstIngredient()
        .map(ing -> ing.getStrength().getDenominator().getValue().doubleValue());
  }

  public Optional<String> getIngredientStrengthDenominatorUnit() {
    return getFirstIngredient().map(ing -> ing.getStrength().getDenominator().getUnit());
  }

  @Nullable
  public String getIngredientStrengthReadable() {
    return format(
        "{0} {1} pro {2} {3}",
        getIngredientStrengthNumeratorValue().orElse(0.0),
        getIngredientStrengthNumeratorUnit().orElse("--"),
        getIngredientStrengthDenominatorValue().orElse(0.0),
        getIngredientStrengthDenominatorUnit().orElse("--"));
  }
}
