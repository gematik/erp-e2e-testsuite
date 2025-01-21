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

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.test.erezept.fhir.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItvEvdgaStructDef;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import de.gematik.test.erezept.fhir.values.BaseANR;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.ResourceType;

@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class KbvEvdgaBundle extends KbvBaseBundle {

  public KbvHealthAppRequest getHealthAppRequest() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.DeviceRequest))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvHealthAppRequest.class, entry, KbvHealthAppRequest::fromDeviceRequest))
        .findFirst()
        .orElseThrow(
            () ->
                new MissingFieldException(
                    this.getClass(), KbvItvEvdgaStructDef.HEALTH_APP_REQUEST));
  }

  public List<KbvPractitioner> getAllPractitioners() {
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
        .toList();
  }

  public Optional<KbvPractitionerRole> getPractitionerRole() {
    return this.entry.stream()
        .filter(
            entry -> entry.getResource().getResourceType().equals(ResourceType.PractitionerRole))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvPractitionerRole.class,
                    entry,
                    resource -> {
                      val pr = new KbvPractitionerRole();
                      resource.copyValues(pr);
                      return pr;
                    }))
        .findFirst();
  }

  @Override
  public String getDescription() {
    val workflow = this.getFlowType();
    return format(
        "{0} (Workflow {1}) {2} für {3}",
        workflow.getDisplay(), workflow.getCode(), type, this.getPatient().getDescription());
  }

  @Override
  public void setAuthoredOnDate(Date authoredOn) {
    this.getHealthAppRequest().setAuthoredOn(authoredOn);
  }

  @Override
  public Date getAuthoredOn() {
    return this.getHealthAppRequest().getAuthoredOn();
  }
}
