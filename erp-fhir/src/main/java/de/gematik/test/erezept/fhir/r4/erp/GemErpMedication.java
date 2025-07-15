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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.DeBasisProfilStructDef;
import de.gematik.bbriccs.fhir.de.value.ASK;
import de.gematik.bbriccs.fhir.de.value.ATC;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.eml.fhir.profile.EpaMedicationStructDef;
import de.gematik.test.erezept.eml.fhir.r4.EpaMedPznIngredient;
import de.gematik.test.erezept.eml.fhir.valuesets.EpaDrugCategory;
import de.gematik.test.erezept.fhir.profiles.systems.CommonCodeSystem;
import de.gematik.test.erezept.fhir.profiles.systems.KbvCodeSystem;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.BooleanType;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Resource;

@Slf4j
@ResourceDef(name = "Medication")
@SuppressWarnings({"java:S110"})
public class GemErpMedication extends Medication {

  public Optional<EpaDrugCategory> getCategory() {
    return this.getExtension().stream()
        .filter(EpaMedicationStructDef.DRUG_CATEGORY_EXT::matches)
        .map(Extension::getValue)
        .map(coding -> coding.castToCoding(coding))
        .map(coding -> EpaDrugCategory.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<PZN> getPzn() {
    return this.getCode().getCoding().stream()
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(Coding::getCode)
        .map(PZN::from)
        .findFirst();
  }

  public Optional<String> getNameFromCodeOreContainedRessource() {
    return this.getCode().hasText()
        ? Optional.ofNullable(this.getCode().getText())
        : this.getContained().stream()
            .filter(EpaMedicationStructDef.MEDICATION_PZN_INGREDIENT::matches)
            .map(EpaMedPznIngredient.class::cast)
            .flatMap(med -> med.getName().stream())
            .findFirst();
  }

  public Optional<Darreichungsform> getDarreichungsform() {
    return this.getForm().getCoding().stream()
        .filter(KbvCodeSystem.DARREICHUNGSFORM::matches)
        .map(coding -> Darreichungsform.fromCode(coding.getCode()))
        .findFirst();
  }

  public Optional<StandardSize> getStandardSize() {
    return this.getExtension().stream()
        .filter(DeBasisProfilStructDef.NORMGROESSE::matches)
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
        .filter(EpaMedicationStructDef.VACCINE_EXT::matches)
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

  public static GemErpMedication fromMedication(Medication adaptee) {
    if (adaptee instanceof GemErpMedication erpMedication) {
      return erpMedication;
    } else {
      val erpMedication = new GemErpMedication();
      adaptee.copyValues(erpMedication);
      return erpMedication;
    }
  }

  public Optional<String> getFreeText() {
    return Optional.ofNullable(this.getCode().getText());
  }

  public Optional<String> getManufacturingInstruction() {
    return this.getExtension().stream()
        .filter(EpaMedicationStructDef.MANUFACTURING_INSTRUCTION::matches)
        .map(Extension::getValue)
        .map(coding -> coding.castToString(coding))
        .map(PrimitiveType::getValue)
        .findFirst();
  }

  public static GemErpMedication fromMedication(Resource adaptee) {
    return fromMedication((Medication) adaptee);
  }

  public Optional<ATC> getAtc() {
    return this.getCode().getCoding().stream()
        .filter(coding -> coding.getSystem().matches(DeBasisProfilCodeSystem.ATC.getCanonicalUrl()))
        .map(Coding::getCode)
        .map(ATC::from)
        .findFirst();
  }

  public Optional<ASK> getAsk() {
    return this.getCode().getCoding().stream()
        .filter(coding -> coding.getSystem().matches(DeBasisProfilCodeSystem.ASK.getCanonicalUrl()))
        .map(Coding::getCode)
        .map(ASK::from)
        .findFirst();
  }

  public Optional<String> getSnomed() {
    return this.getCode().getCoding().stream()
        .filter(coding -> CommonCodeSystem.SNOMED_SCT.matches(coding.getSystem()))
        .map(Coding::getCode)
        .findFirst();
  }
}
