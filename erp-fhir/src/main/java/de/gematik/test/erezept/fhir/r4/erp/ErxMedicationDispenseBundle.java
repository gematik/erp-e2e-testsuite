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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispenseBundle extends Bundle {

  public List<ErxMedicationDispense> getMedicationDispenses() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.MedicationDispense))
        .filter(
            resource ->
                WithSystem.anyOf(
                        ErpWorkflowStructDef.MEDICATION_DISPENSE_12,
                        ErpWorkflowStructDef.MEDICATION_DISPENSE)
                    .matches(resource))
        .map(ErxMedicationDispense::fromMedicationDispense)
        .toList();
  }

  public List<GemErpMedication> getMedications() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Medication))
        .filter(ErpWorkflowStructDef.MEDICATION::matches)
        .map(GemErpMedication::fromMedication)
        .toList();
  }

  /**
   * Get the pair of corresponding MedicationDispense and GemErpMedication by prescriptionId
   * <b>Attention:</b> this method will only work for FHIR profiles 1.4.0 onwards
   *
   * @param prescriptionId of the prescription for which the dispensation was performed
   * @return a list of pairs of MedicationDispense and GemErpMedication
   */
  public List<Pair<ErxMedicationDispense, GemErpMedication>> getDispensePairBy(
      PrescriptionId prescriptionId) {
    val mds = this.getMedicationDispenses();
    val medications = this.getMedications();

    return mds.stream()
        .filter(md -> md.getPrescriptionId().getValue().equals(prescriptionId.getValue()))
        .map(
            md -> {
              val medicationRef =
                  md.getMedicationReference().getReference().replace("urn:uuid:", "");
              val medication =
                  medications.stream()
                      .filter(m -> m.getId().contains(medicationRef))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new MissingFieldException(
                                  this.getClass(),
                                  format(
                                      "Medication with reference {0} for {1}",
                                      medicationRef, prescriptionId.getValue())));
              return Pair.of(md, medication);
            })
        .toList();
  }

  /**
   * Get the pair of corresponding MedicationDispense and KbvErpMedication (which is contained
   * within the MedicationDispense) by prescriptionId <b>Attention:</b> this method will only work
   * up to FHIR profiles 1.3.0
   *
   * @param prescriptionId of the prescription for which the dispensation was performed
   * @return a list of pairs of MedicationDispense and KbvErpMedication
   */
  public List<Pair<ErxMedicationDispense, KbvErpMedication>> unpackDispensePairBy(
      PrescriptionId prescriptionId) {
    return this.getMedicationDispenses().stream()
        .filter(md -> md.getPrescriptionId().getValue().equals(prescriptionId.getValue()))
        .map(
            md -> {
              val containedMedication =
                  md.getContained().stream()
                      .filter(c -> c.getResourceType().equals(ResourceType.Medication))
                      .map(KbvErpMedication::fromMedication)
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new MissingFieldException(
                                  this.getClass(), "Contained KbvErpMedication"));
              return Pair.of(md, containedMedication);
            })
        .toList();
  }
}
