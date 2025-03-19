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

package de.gematik.test.erezept.fhir.r4.kbv;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.r4.ErpFhirResource;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import java.util.Date;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public abstract class KbvBaseBundle extends Bundle implements ErpFhirResource {

  /**
   * Get the <a href="https://simplifier.net/erezept/kbvprerpcomposition">KBV E-Rezept
   * Composition</a> from Bundle
   *
   * @return the {@link Composition}
   */
  public Composition getComposition() {
    return this.entry.stream()
        .map(BundleEntryComponent::getResource)
        .filter(resource -> resource.getClass().equals(Composition.class))
        .map(Composition.class::cast)
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Composition"));
  }

  /**
   * Convenience method for getting the prescription ID of the bundle. Will yield the same result
   * from KbvErpBundle.getIdentifier().getValue()
   *
   * @return the prescription ID of this bundle
   */
  public PrescriptionId getPrescriptionId() {
    return Optional.of(this.getIdentifier())
        .filter(PrescriptionId::isPrescriptionId)
        .map(PrescriptionId::from)
        .orElseThrow(
            () ->
                new MissingFieldException(
                    KbvErpBundle.class,
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID,
                    ErpWorkflowNamingSystem.PRESCRIPTION_ID_121));
  }

  public void setPrescriptionId(PrescriptionId prescriptionId) {
    val pidIdentifier =
        new Identifier()
            .setSystem(prescriptionId.getSystemUrl())
            .setValue(prescriptionId.getValue());

    this.setIdentifier(pidIdentifier);
  }

  public PrescriptionFlowType getFlowType() {
    return PrescriptionFlowType.fromPrescriptionId(this.getPrescriptionId());
  }

  public KbvPatient getPatient() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Patient))
        .map(entry -> FhirEntryReplacer.cast(KbvPatient.class, entry, KbvPatient::fromPatient))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Patient));
  }

  public KbvCoverage getCoverage() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Coverage))
        .map(entry -> FhirEntryReplacer.cast(KbvCoverage.class, entry, KbvCoverage::fromCoverage))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Coverage));
  }

  public KbvPractitioner getPractitioner() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Practitioner))
        .filter(
            practitionerEntry ->
                ((Practitioner) practitionerEntry.getResource())
                    .getIdentifier().stream()
                        .map(identifier -> identifier.getType().getCodingFirstRep())
                        .anyMatch(BaseANR::isPractitioner))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvPractitioner.class, entry, KbvPractitioner::fromPractitioner))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Practitioner));
  }

  public KbvMedicalOrganization getMedicalOrganization() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Organization))
        .filter(KbvItaForStructDef.ORGANIZATION::matches)
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvMedicalOrganization.class, entry, KbvMedicalOrganization::fromOrganization))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Medical Organization"));
  }

  public void setAllDates() {
    val now = new Date();
    this.setAllDates(now);
  }

  public void setAllDates(Date date) {
    this.setAuthoredOnDate(date);
    this.setCompositionDate(date);
    this.setTimestamp(date);
    this.getMeta().setLastUpdated(date);
  }

  public abstract void setAuthoredOnDate(Date authoredOn);

  public abstract Date getAuthoredOn();

  public Date getCompositionDate() {
    return this.getComposition().getDate();
  }

  public void setCompositionDate(Date date) {
    this.getComposition().setDate(date);
  }
}
