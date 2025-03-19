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

package de.gematik.test.erezept.eml.fhir.r4;

import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.values.RxPrescriptionId;
import java.util.Optional;
import java.util.function.Function;
import lombok.val;
import org.hl7.fhir.r4.model.Medication;

@SuppressWarnings("java:S110")
public class EpaMedication extends Medication {

  public Optional<PZN> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(PZN::from)
        .findFirst();
  }

  public Optional<ASK> getAsk() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ASK::matches)
        .map(ASK::from)
        .findFirst();
  }

  public Optional<ATC> getAtc() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.ATC::matches)
        .map(ATC::from)
        .findFirst();
  }

  public Optional<RxPrescriptionId> getRxPrescriptionId() {
    return this.getExtension().stream()
        .filter(EpaMedicationStructDef.RX_PRESCRIPTION_ID::matches)
        .map(ext -> RxPrescriptionId.from(ext.getValue().castToIdentifier(ext.getValue())))
        .findFirst();
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(ext -> EpaMedicationStructDef.VACCINE_EXT.matches(ext.getUrl()))
        .map(ext -> ext.getValue().castToBoolean(ext.getValue()).booleanValue())
        .findFirst()
        .orElse(false); // keine Extension also per default false
  }

  public Optional<String> getName() {
    val name =
        this.getCode().hasText()
            ? Optional.ofNullable(this.getCode().getText())
            : this.getIngredient().stream().map(getCCTextOrEmpty()).findFirst();

    val isEmptyName = name.map(String::isEmpty).orElse(true);

    return isEmptyName
        ? Optional.ofNullable(this.getCode().getCodingFirstRep().getDisplay())
        : name;
  }

  private static Function<MedicationIngredientComponent, String> getCCTextOrEmpty() {
    return mic ->
        mic.getItemCodeableConcept().hasText() ? mic.getItemCodeableConcept().getText() : "";
  }
}
