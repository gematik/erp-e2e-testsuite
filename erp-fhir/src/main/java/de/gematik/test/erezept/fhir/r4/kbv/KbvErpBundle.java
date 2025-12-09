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

package de.gematik.test.erezept.fhir.r4.kbv;

import static java.text.MessageFormat.format;

import ca.uhn.fhir.model.api.annotation.ResourceDef;
import de.gematik.bbriccs.fhir.coding.exceptions.MissingFieldException;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.PrimitiveType;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.r4.model.ResourceType;
import org.hl7.fhir.r4.model.SupplyRequest;

/** <a href="https://simplifier.net/erezept/kbvprerpbundle">KBV E-Rezept Bundle</a> */
@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class KbvErpBundle extends KbvBaseBundle {

  @Override
  public Reference asReference() {
    return asReferenceFromId(this.getLogicalId());
  }

  public String getFullMetaProfile() {
    val profileIndex = 0; // profile has cardinality 1..1
    return this.getMeta().getProfile().get(profileIndex).asStringValue();
  }

  /**
   * Get the plain profile value without version from Resource.meta.profile
   *
   * @return plain profile value without version
   */
  public String getMetaProfile() {
    return this.splitToProfileUrl(this.getFullMetaProfile());
  }

  /**
   * Get the plain profile version without the profile itself from Resource.meta.profile
   *
   * @return plain profile version without the profile itself
   */
  public String getMetaProfileVersion() {
    return this.splitToProfileVersion(this.getFullMetaProfile());
  }

  public List<KbvErpMedicationRequest> getMedicationRequests() {
    return this.entry.stream()
        .filter(
            entry -> entry.getResource().getResourceType().equals(ResourceType.MedicationRequest))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvErpMedicationRequest.class,
                    entry,
                    KbvErpMedicationRequest::fromMedicationRequest))
        .toList();
  }

  public Optional<KbvErpMedicationRequest> getFirstMedicationRequest() {
    val medRequests = this.getMedicationRequests();
    Optional<KbvErpMedicationRequest> ret;
    if (!medRequests.isEmpty()) {
      ret = Optional.of(medRequests.get(0));
    } else {
      ret = Optional.empty();
    }
    return ret;
  }

  public KbvErpMedication getMedication() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Medication))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvErpMedication.class, entry, KbvErpMedication::fromMedication))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Medication));
  }

  public KbvErpMedicationRequest getMedicationRequest() {
    return getMedicationRequestOptional()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), ResourceType.MedicationRequest));
  }

  public Optional<KbvErpMedicationRequest> getMedicationRequestOptional() {
    return this.entry.stream()
        .filter(
            entry -> entry.getResource().getResourceType().equals(ResourceType.MedicationRequest))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    KbvErpMedicationRequest.class,
                    entry,
                    KbvErpMedicationRequest::fromMedicationRequest))
        .findFirst();
  }

  public Optional<SupplyRequest> getSupplyRequest() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.SupplyRequest))
        .map(entry -> (SupplyRequest) entry.getResource())
        .findFirst();
  }

  /**
   * KBV does encode the version number within the profile e.g. <a
   * href="https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1">KBV_PR_ERP_Bundle</a>"
   * This method cuts of the version and returns the plain profile as String
   *
   * @param profile the profile (canonical) as String which has the following shape [URL]|[VERSION]
   * @return only the URL part
   */
  private String splitToProfileUrl(String profile) {
    return profile.split("\\|")[0];
  }

  /**
   * KBV does encode the version number within the profile e.g. <a
   * href="https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1">KBV_PR_ERP_Bundle</a>"
   * This method cuts of the profile URL and returns the plain Version as String
   *
   * @param profile the profile (canonical) as String which has the following shape [URL]|[VERSION]
   * @return only the VERSION part
   */
  private String splitToProfileVersion(String profile) {
    return profile.split("\\|")[1];
  }

  @Override
  public String toString() {
    val profile =
        this.getMeta().getProfile().stream()
            .map(PrimitiveType::asStringValue)
            .collect(Collectors.joining(", "));
    return format("{0} (Profile {1})", this.getClass().getSimpleName(), profile);
  }

  @Override
  public String getDescription() {
    val type =
        (this.getMedicationRequest().isMultiple()) ? "MVO Verordnung" : "einfache Verordnung";
    val workflow = this.getFlowType();
    return format(
        "{0} (Workflow {1}) {2} für {3}",
        workflow.getDisplay(), workflow.getCode(), type, this.getPatient().getDescription());
  }

  public static Reference asReferenceFromId(String id) {
    return new Reference()
        .setDisplay("E-Rezept")
        .setReference(id)
        .setType(KbvItaErpStructDef.BUNDLE.getCanonicalUrl());
  }

  @Override
  public void setAuthoredOnDate(Date authoredOn) {
    this.getMedicationRequest().setAuthoredOn(authoredOn);
  }

  @Override
  public Date getAuthoredOn() {
    return this.getMedicationRequest().getAuthoredOn();
  }
}
