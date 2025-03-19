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
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Dosage;
import org.hl7.fhir.r4.model.MedicationDispense;
import org.hl7.fhir.r4.model.Resource;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "MedicationDispense")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispense extends ErxMedicationDispenseBase {

  /**
   * This method will return all contained Medication resources as KbvErpMedication
   *
   * @deprecated since 0.10.1 because from FHIR-Profiles 1.4.0 the MedicationDispense won't contain
   *     the Medication anymore
   * @return List of KbvErpMedication contained within this MedicationDispense
   */
  @Deprecated(since = "0.10.1", forRemoval = true)
  public List<KbvErpMedication> getContainedKbvMedication() {
    return this.getContained().stream()
        .filter(c -> c.getResourceType().equals(ResourceType.Medication))
        .map(KbvErpMedication::fromMedication)
        .toList();
  }

  /**
   * This method will return the first contained Medication resource as KbvErpMedication or throw an
   * exception if no Medication is contained
   *
   * @deprecated since 0.10.1 because from FHIR-Profiles 1.4.0 the MedicationDispense won't contain
   *     the Medication anymore
   * @return the first KbvErpMedication contained within this MedicationDispense
   */
  @Deprecated(since = "0.10.1", forRemoval = true)
  public KbvErpMedication getContainedKbvMedicationFirstRep() {
    return this.getContainedKbvMedication().stream()
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpMedication.class, KbvItaErpStructDef.MEDICATION_PZN));
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
   * ErxPrescriptionBundle HAPI interprets the containing MedicationDispense as plain
   * HAPI-MedicationDispense and not as a ErxMedicationDispense. This constructor allows mapping to
   * ErxMedicationDispense.
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
