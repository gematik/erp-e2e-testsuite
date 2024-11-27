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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.EpaMedicationStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisCodeSystem;
import de.gematik.test.erezept.fhir.parser.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.epa.EpaDrugCategory;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;

@Slf4j
@ResourceDef(name = "Medication")
@SuppressWarnings({"java:S110"})
public class GemErpMedication extends Medication {

  public Optional<EpaDrugCategory> getCategory() {
    return this.getExtension().stream()
        .filter(EpaMedicationStructDef.DURG_CATEGORY_EXT::match)
        .map(Extension::getValue)
        .map(coding -> coding.castToCoding(coding))
        .map(coding -> EpaDrugCategory.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<PZN> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisCodeSystem.PZN::match)
        .map(Coding::getCode)
        .map(PZN::from)
        .findFirst();
  }

  public Optional<String> getName() {
    return Optional.ofNullable(this.getCode().getText());
  }

  public Optional<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .filter(KbvCodeSystem.DARREICHUNGSFORM::match)
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<StandardSize> getStandardSize() {
    return this.getExtension().stream()
        .filter(DeBasisStructDef.NORMGROESSE::match)
        .map(ext -> StandardSize.fromCode(ext.getValue().castToCoding(ext.getValue()).getCode()))
        .findFirst();
  }

  public Optional<String> getAmountNumeratorUnit() {
    return Optional.ofNullable(this.getAmount().getNumerator().getUnit());
  }

  public Optional<Integer> getAmountNumerator() {
    return Optional.ofNullable(this.getAmount().getNumerator().getValue())
        .map(BigDecimal::intValue);
  }

  public boolean isVaccine() {
    return this.getExtension().stream()
        .filter(EpaMedicationStructDef.VACCINE_EXT::match)
        .map(Extension::getValue)
        .map(coding -> coding.castToBoolean(coding))
        .map(BooleanType::booleanValue)
        .findFirst()
        .orElse(false);
  }

  public Optional<String> getBatchLotNumber() {
    return this.getBatch().getLotNumberElement().isEmpty()
        ? Optional.empty()
        : Optional.of(this.getBatch().getLotNumber());
  }
}
