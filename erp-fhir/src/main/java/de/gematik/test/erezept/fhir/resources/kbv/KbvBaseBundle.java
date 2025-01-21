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

package de.gematik.test.erezept.fhir.resources.kbv;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaForStructDef;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public abstract class KbvBaseBundle extends Bundle implements ErpFhirResource {

  /**
   * Convenience method for getting the prescription ID of the bundle. Will yield the same result
   * from KbvErpBundle.getIdentifier().getValue()
   *
   * @return the prescription ID of this bundle
   */
  public PrescriptionId getPrescriptionId() {
    if (!PrescriptionId.isPrescriptionId(this.getIdentifier())) {
      throw new MissingFieldException(KbvErpBundle.class, PrescriptionId.NAMING_SYSTEM);
    }
    return PrescriptionId.from(this.getIdentifier());
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

  public MedicalOrganization getMedicalOrganization() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Organization))
        .filter(entry -> KbvItaForStructDef.ORGANIZATION.match(entry.getResource().getMeta()))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    MedicalOrganization.class, entry, MedicalOrganization::fromOrganization))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Medical Organization"));
  }
}
