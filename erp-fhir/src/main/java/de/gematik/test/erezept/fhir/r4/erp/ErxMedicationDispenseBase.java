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

package de.gematik.test.erezept.fhir.r4.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Reference;

@Slf4j
@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispenseBase extends MedicationDispense {

  public boolean isDiGA() {
    return ErpWorkflowStructDef.MEDICATION_DISPENSE_DIGA.matches(this.getMeta());
  }

  public PrescriptionId getPrescriptionId() {
    return PrescriptionId.from(this.getIdentifierFirstRep());
  }

  public KVNR getSubjectId() {
    return KVNR.from(this.getSubject().getIdentifier());
  }

  public List<String> getPerformerIds() {
    return this.getPerformer().stream()
        .map(MedicationDispensePerformerComponent::getActor)
        .map(Reference::getIdentifier)
        .map(Identifier::getValue)
        .toList();
  }

  public String getPerformerIdFirstRep() {
    return this.getPerformerIds().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    ErxMedicationDispense.class, ErpWorkflowNamingSystem.TELEMATIK_ID));
  }

  public ZonedDateTime getZonedWhenHandedOver() {
    return ZonedDateTime.ofInstant(this.getWhenHandedOver().toInstant(), ZoneId.systemDefault());
  }
}
