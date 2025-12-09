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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.WithSystem;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.definitions.ErpWorkflowStructDef;
import de.gematik.test.erezept.fhir.profiles.definitions.GemErpEuStructDef;
import de.gematik.test.erezept.fhir.r4.eu.EuMedicationDispense;
import de.gematik.test.erezept.fhir.r4.eu.EuOrganization;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitioner;
import de.gematik.test.erezept.fhir.r4.eu.EuPractitionerRole;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class ErxMedicationDispenseBundle extends Bundle {

  private static final String URN_UUID = "urn:uuid:";

  private static boolean isContainedKbvMedication(ErxMedicationDispense md, String reference) {
    return md.getContained().stream().anyMatch(m -> m.getId().contains(reference));
  }

  public List<ErxMedicationDispense> getMedicationDispenses() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.MedicationDispense))
        .filter(
            resource ->
                WithSystem.anyOf(
                        ErpWorkflowStructDef.MEDICATION_DISPENSE_12,
                        ErpWorkflowStructDef.MEDICATION_DISPENSE,
                        ErpWorkflowStructDef.MEDICATION_DISPENSE_DIGA)
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
              val medicationRef = Optional.ofNullable(md.getMedicationReference().getReference());
              val urnMedicationRef = medicationRef.orElse("").replace(URN_UUID, "");

              if (medicationRef.isEmpty() || isContainedKbvMedication(md, urnMedicationRef)) {
                // TODO: skip contained medications for now!
                return null;
              }

              val medication =
                  medications.stream()
                      .filter(m -> m.getId().contains(urnMedicationRef))
                      .findFirst()
                      .orElseThrow(
                          () ->
                              new MissingFieldException(
                                  this.getClass(),
                                  format(
                                      "Medication with reference {0} for {1}",
                                      urnMedicationRef, prescriptionId.getValue())));
              return Pair.of(md, medication);
            })
        .filter(Objects::nonNull)
        .toList();
  }

  /**
   * Get the pair of corresponding MedicationDispense and GemErpMedication by prescriptionId
   * <b>Attention:</b> this method will only work for FHIR profiles 1.4.0 onwards
   *
   * @param prescriptionId of the prescription for which the dispensation was performed
   * @return a list of pairs of MedicationDispense and GemErpMedication
   */
  public List<Pair<EuMedicationDispense, GemErpMedication>> getEuDispensePairBy(
      PrescriptionId prescriptionId) {
    val mds = this.getEuMedicationDispenses();
    val euMedications = this.getEuMedicationBy(prescriptionId);
    val gemMedications = this.getMedications();

    return mds.stream()
        .filter(md -> md.getPrescriptionId().getValue().equals(prescriptionId.getValue()))
        .map(
            md -> {
              val medicationRef = md.getMedicationReference().getReference().replace(URN_UUID, "");

              GemErpMedication medication =
                  euMedications.stream()
                      .filter(m -> m.getId().contains(medicationRef))
                      .findFirst()
                      .orElse(null);
              if (medication == null) {
                medication =
                    gemMedications.stream()
                        .filter(m -> m.getId().contains(medicationRef))
                        .findFirst()
                        .orElseThrow(
                            () ->
                                new MissingFieldException(
                                    this.getClass(),
                                    format(
                                        "Medication with reference {0} for {1}",
                                        medicationRef, prescriptionId.getValue())));
              }
              return Pair.of(md, medication);
            })
        .filter(Objects::nonNull)
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

  public List<EuMedicationDispense> getEuMedicationDispenses() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.MedicationDispense))
        .filter(GemErpEuStructDef.EU_DISPENSATION::matches)
        .map(EuMedicationDispense::fromMedicationDispense)
        .toList();
  }

  public List<EuMedicationDispense> getEuMedicationDispenseBy(PrescriptionId prescriptionId) {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.MedicationDispense))
        .filter(GemErpEuStructDef.EU_DISPENSATION::matches)
        .map(EuMedicationDispense::fromMedicationDispense)
        .filter(md -> md.getPrescriptionId().equals(prescriptionId))
        .toList();
  }

  public List<GemErpMedication> getEuMedicationBy(PrescriptionId prescriptionId) {
    val medDisp = this.getEuMedicationDispenseBy(prescriptionId);
    val refSet =
        medDisp.stream()
            .map(md -> md.getMedicationReference().getReference().replace(URN_UUID, ""))
            .distinct()
            .toList();
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Medication))
        .filter(
            r ->
                WithSystem.anyOf(GemErpEuStructDef.EU_MEDICATION, ErpWorkflowStructDef.MEDICATION)
                    .matches(r))
        .filter(res -> refSet.stream().anyMatch(mR -> res.getId().contains(mR)))
        .map(GemErpMedication::fromMedication)
        .toList();
  }

  public List<EuPractitioner> getEuPractitionerBy(PrescriptionId prescriptionId) {
    val medDisp = this.getEuMedicationDispenseBy(prescriptionId);
    // findPractitionerRole
    return medDisp.stream().map(this::getEuPractitionerBy).toList();
  }

  public EuPractitioner getEuPractitionerBy(EuMedicationDispense md) {
    val prRole = this.getEuPractitionerRoleTo(md);
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Practitioner))
        .filter(GemErpEuStructDef.EU_PRACTITIONER::matches)
        .filter(
            pr ->
                prRole.stream()
                    .flatMap(
                        it ->
                            Stream.of(
                                it.getPractitioner().getReference(),
                                it.getPractitioner().getResource().getIdElement().getValue()))
                    .anyMatch(it -> it.contains(pr.getId())))
        .map(EuPractitioner::fromPractitioner)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Practitioner"));
  }

  public Optional<EuPractitionerRole> getEuPractitionerRoleTo(EuMedicationDispense md) {
    val id =
        md.getPerformer().stream()
            .findFirst()
            .orElseThrow()
            .getActor()
            .getReference()
            .replace(URN_UUID, "")
            .replace("PractitionerRole/", "");
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.PractitionerRole))
        .filter(GemErpEuStructDef.EU_PRACTITIONER_ROLE::matches)
        .filter(pr -> pr.getId().contains(id))
        .map(EuPractitionerRole::fromPractitionerRole)
        .findFirst();
  }

  public List<EuOrganization> getEuOrganizations() {
    return this.getEntry().stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getResourceType().equals(ResourceType.Organization))
        .filter(GemErpEuStructDef.EU_ORGANIZATION::matches)
        .map(EuOrganization::fromOrganisation)
        .toList();
  }

  public Optional<LocalDate> getEuWhenHandedOver(PrescriptionId prescriptionId) {
    return this.getEuMedicationDispenseBy(prescriptionId).stream()
        .map(md -> md.getWhenHandedOver().toInstant().atZone(ZoneId.systemDefault()).toLocalDate())
        .findFirst();
  }
}
