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
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilCodeSystem;
import de.gematik.bbriccs.fhir.de.HL7StructDef;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.extensions.erp.DeepLink;
import de.gematik.test.erezept.fhir.extensions.erp.RedeemCode;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispenseDiGA extends ErxMedicationDispenseBase {

  public Optional<RedeemCode> getRedeemCode() {
    return this.getExtension().stream()
        .filter(ErpWorkflowStructDef.REDEEM_CODE::matches)
        .map(ex -> ex.getValue().castToString(ex.getValue()))
        .map(stringType -> RedeemCode.from(stringType.getValue()))
        .findFirst();
  }

  public Optional<DeepLink> getDeepLink() {
    return this.getExtension().stream()
        .filter(ErpWorkflowStructDef.DEEP_LINK::matches)
        .map(ex -> ex.getValue().castToUri(ex.getValue()))
        .map(uriType -> DeepLink.from(uriType.getValue()))
        .findFirst();
  }

  public PZN getPzn() {
    return Optional.ofNullable(this.medication)
        .map(m -> (Reference) m)
        .map(Reference::getIdentifier)
        .filter(DeBasisProfilCodeSystem.PZN::matches)
        .map(identifier -> PZN.from(identifier.getValue()))
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), "Medication Reference with PZN identifier"));
  }

  public String getDigaName() {
    return Optional.ofNullable(this.medication)
        .map(m -> (Reference) m)
        .map(Reference::getDisplay)
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), "Medication Reference with display name"));
  }

  public Boolean isDeclined() {
    return this.getMedication().getExtension().stream()
        .filter(ext -> HL7StructDef.DATA_ABSENT_REASON.getCanonicalUrl().matches(ext.getUrl()))
        .map(value -> value.getValueAsPrimitive().getValueAsString().contains("asked-declined"))
        .findFirst()
        .orElse(false);
  }
}
