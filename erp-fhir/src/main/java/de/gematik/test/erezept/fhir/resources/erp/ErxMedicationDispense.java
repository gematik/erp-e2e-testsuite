/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.fhir.resources.erp;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

@Slf4j
@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispense extends MedicationDispense {

  public List<KbvErpMedication> getErpMedication() {
    return this.getContained().stream()
        .filter(c -> c.getResourceType().equals(ResourceType.Medication))
        .map(KbvErpMedication::fromMedication)
        .toList();
  }

  public PrescriptionId getPrescriptionId() {
    return new PrescriptionId(this.getIdentifierFirstRep().getValue());
  }

  public KbvErpMedication getErpMedicationFirstRep() {
    return this.getErpMedication().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpMedication.class, KbvItaErpStructDef.MEDICATION_PZN));
  }

  public KVNR getSubjectId() {
    return KVNR.from(this.getSubject().getIdentifier().getValue());
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

  public List<String> getDosageInstructionText() {
    return this.getDosageInstruction().stream().map(Dosage::getText).toList();
  }

  public String getDosageInstructionTextFirstRep() {
    return this.getDosageInstructionText().stream()
        .findFirst()
        .orElseThrow(
            () -> new MissingFieldException(ErxMedicationDispense.class, "DosageInstructionText"));
  }

  /**
   * This constructor translates a Task into a ErxTask. For example if you receive an
   * ErxPrescriptionBundle HAPI interprets the containing Task as plain HAPI-Task and not as a
   * ErxTask. This constructor allows mapping to ErxTask
   *
   * @param adaptee
   */
  public static ErxMedicationDispense fromMedicationDispense(MedicationDispense adaptee) {
    val erxMedicationDispense = new ErxMedicationDispense();
    adaptee.copyValues(erxMedicationDispense);
    return erxMedicationDispense;
  }

  public static ErxMedicationDispense fromMedicationDispense(Resource adaptee) {
    return fromMedicationDispense((MedicationDispense) adaptee);
  }
}
