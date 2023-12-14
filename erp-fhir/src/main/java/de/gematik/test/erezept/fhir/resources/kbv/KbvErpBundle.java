/*
 * Copyright 2023 gematik GmbH
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
import de.gematik.test.erezept.fhir.parser.profiles.definitions.DeBasisStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.DeBasisNamingSystem;
import de.gematik.test.erezept.fhir.references.kbv.CoverageReference;
import de.gematik.test.erezept.fhir.references.kbv.KbvBundleReference;
import de.gematik.test.erezept.fhir.resources.ErpFhirResource;
import de.gematik.test.erezept.fhir.util.FhirEntryReplacer;
import de.gematik.test.erezept.fhir.util.IdentifierUtil;
import de.gematik.test.erezept.fhir.values.BSNR;
import de.gematik.test.erezept.fhir.values.BaseANR;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.Darreichungsform;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.PrescriptionFlowType;
import de.gematik.test.erezept.fhir.valuesets.StandardSize;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/** <a href="https://simplifier.net/erezept/kbvprerpbundle">KBV E-Rezept Bundle</a> */
@Slf4j
@Getter
@ResourceDef(name = "Bundle")
@SuppressWarnings({"java:S110"})
public class KbvErpBundle extends Bundle implements ErpFhirResource {

  public String getLogicalId() {
    return IdentifierUtil.getUnqualifiedId(this.id);
  }

  public KbvBundleReference getReference() {
    return new KbvBundleReference(this.getLogicalId());
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

  public void setAuthoredOnDate(Date authoredOn) {
    this.getMedicationRequest().setAuthoredOn(authoredOn);
  }

  public Date getAuthoredOn() {
    return this.getMedicationRequest().getAuthoredOn();
  }

  public Date getCompositionDate() {
    return this.getComposition().getDate();
  }

  public void setCompositionDate(Date date) {
    this.getComposition().setDate(date);
  }

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

  public KbvErpBundle setPrescriptionId(PrescriptionId prescriptionId) {
    val pidIdentifier =
        new Identifier()
            .setSystem(prescriptionId.getSystemAsString())
            .setValue(prescriptionId.getValue());

    this.setIdentifier(pidIdentifier);
    return this;
  }

  public PrescriptionFlowType getFlowType() {
    return PrescriptionFlowType.fromPrescriptionId(this.getPrescriptionId());
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

  public boolean isMultiple() {
    return this.getMedicationRequest().isMultiple();
  }

  public boolean hasEmergencyServicesFee() {
    boolean ret = false;
    val medRequestOpt = this.getFirstMedicationRequest();
    if (medRequestOpt.isPresent()) {
      val medRequest = medRequestOpt.orElseThrow();
      val extension =
          medRequest.getExtensionByUrl(KbvItaErpStructDef.EMERGENCY_SERVICES_FEE.getCanonicalUrl());
      ret = extension.castToBoolean(extension.getValue()).booleanValue();
    }

    return ret;
  }

  public KbvPatient getPatient() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Patient))
        .map(entry -> FhirEntryReplacer.cast(KbvPatient.class, entry, KbvPatient::fromPatient))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Patient));
  }

  public KVNR getKvnr() {
    val patient = this.getPatient();
    return patient.getKvnr();
  }

  public String getFamilyName() {
    val patient = this.getPatient();
    return patient.getNameFirstRep().getFamily();
  }

  public String getPatientGivenName() {
    val patient = this.getPatient();
    val given = patient.getNameFirstRep().getGiven().stream().map(PrimitiveType::getValue).toList();
    return String.join(" ", given);
  }

  public String getPatientFamilyName() {
    val patient = this.getPatient();
    return patient.getNameFirstRep().getFamilyElement().getValue();
  }

  public Date getPatientBirthDate() {
    val patient = this.getPatient();
    return patient.getBirthDate();
  }

  public String getPatientAddressCity() {
    val patient = this.getPatient();
    return patient.getAddressFirstRep().getCity();
  }

  public String getPatientAddressPostalCode() {
    val patient = this.getPatient();
    return patient.getAddressFirstRep().getPostalCode();
  }

  public String getPatientAddressStreet() {
    val patient = this.getPatient();
    return patient.getAddressFirstRep().getLine().stream()
        .map(PrimitiveType::getValue)
        .collect(Collectors.joining(" "));
  }

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

  public KbvCoverage getCoverage() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Coverage))
        .map(entry -> FhirEntryReplacer.cast(KbvCoverage.class, entry, KbvCoverage::fromCoverage))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Coverage));
  }

  /**
   * In some rare cases it might be required to change the coverage. However, within the KBV Bundle
   * the coverage is linked by other entries like Composition or MedicationRequest. This method will
   * remove the old coverage (if one exists), replace with the given one and adapt the linked
   * references
   *
   * @param coverage which shall be set
   */
  public void changeCoverage(KbvCoverage coverage) {
    /* find the index of the coverage */
    val covIdx =
        IntStream.range(0, this.entry.size())
            .filter(
                idx ->
                    this.getEntry()
                        .get(idx)
                        .getResource()
                        .getResourceType()
                        .equals(ResourceType.Coverage))
            .findFirst()
            .orElseThrow(() -> new MissingFieldException(this.getClass(), ResourceType.Coverage));
    val oldEntry = this.getEntry().get(covIdx);
    val oldCoverage = this.getCoverage();
    val oldId = IdentifierUtil.getUnqualifiedId(oldCoverage.getId());
    val newId = IdentifierUtil.getUnqualifiedId(coverage.getId());
    val fullUrl = oldEntry.getFullUrl().replace(oldId, newId);

    /* get the beneficiary from old to new one */
    coverage.getBeneficiary().setReference(oldCoverage.getBeneficiary().getReference());

    /* remove the coverage with the identified index */
    this.entry.remove(covIdx);

    /* replace the coverage with the new one */
    val covEntry = new BundleEntryComponent().setResource(coverage);
    covEntry.setFullUrl(fullUrl);
    this.addEntry(covEntry);

    /* change the reference within the composition */
    val covSection =
        this.getComposition().getSection().stream()
            .filter(section -> section.getCode().getCodingFirstRep().getCode().equals("Coverage"))
            .findFirst()
            .orElseThrow(
                () -> new MissingFieldException(this.getClass(), "Coverage in Composition"));
    covSection.getEntryFirstRep().setReference(new CoverageReference(coverage).getReference());

    /* change insurance in medication request */
    val newCoverageReference = new CoverageReference(coverage);
    this.getMedicationRequest()
        .getInsuranceFirstRep()
        .setReference(newCoverageReference.getReference());
  }

  public String getCoverageIknr() {
    val coverage = this.getCoverage();
    return coverage.getPayorFirstRep().getIdentifier().getValue();
  }

  public String getCoverageName() {
    val coverage = this.getCoverage();
    return coverage.getPayorFirstRep().getDisplay();
  }

  public VersicherungsArtDeBasis getCoverageKind() {
    return getCoverageKindOptional()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), VersicherungsArtDeBasis.CODE_SYSTEM));
  }

  public boolean hasCoverageKind() {
    return getCoverageKindOptional().isPresent();
  }

  public Optional<VersicherungsArtDeBasis> getCoverageKindOptional() {
    val coverage = this.getCoverage();
    return coverage.getType().getCoding().stream()
        .filter(
            coding ->
                coding.getSystem().equals(VersicherungsArtDeBasis.CODE_SYSTEM.getCanonicalUrl()))
        .map(coding -> VersicherungsArtDeBasis.fromCode(coding.getCode()))
        .findFirst();
  }

  public PayorType getPayorType() {
    return getPayorTypeOptional()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), PayorType.CODE_SYSTEM));
  }

  public Optional<PayorType> getPayorTypeOptional() {
    return this.getCoverage().getPayorType();
  }

  public Optional<Wop> getCoverageWop() {
    return this.getCoverage().getWop();
  }

  public VersichertenStatus getCoverageState() {
    return this.getCoverage()
        .getInsurantState()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisStructDef.GKV_VERSICHERTENART));
  }

  public PersonGroup getCoveragePersonGroup() {
    return this.getCoverage()
        .getPersonGroup()
        .orElseThrow(
            () -> new MissingFieldException(this.getClass(), DeBasisStructDef.GKV_PERSON_GROUP));
  }

  public KbvPractitioner getPractitioner() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Practitioner))
        .filter(
            orgEntry ->
                ((Practitioner) orgEntry.getResource())
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

  /**
   * Returns the Organization which issued the prescription. This is usually a medical practice
   * which has a BSNR. In case of an GKV prescription this should be the only organization within
   * the KbvErpBundle. However, if this is a PKV prescription this KbvErpBundle will hold at least
   * to different organizations: one which issued the prescription and a second one for organization
   * which assigned the healthcare insurance number to patient who receives this prescription
   *
   * @return the KbvMedicalOrganization
   */
  public MedicalOrganization getMedicalOrganization() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Organization))
        .filter(
            orgEntry ->
                ((Organization) orgEntry.getResource())
                    .getIdentifier().stream()
                        .map(identifier -> identifier.getType().getCodingFirstRep())
                        .anyMatch(
                            coding ->
                                coding.getSystem().equals(BSNR.getCodeSystemUrl())
                                    && coding.getCode().equals(BSNR.getCode())))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    MedicalOrganization.class, entry, MedicalOrganization::fromOrganization))
        .findFirst()
        .orElseThrow(() -> new MissingFieldException(this.getClass(), "Medical Organization"));
  }

  public Optional<AssignerOrganization> getAssignerOrganization() {
    return this.entry.stream()
        .filter(entry -> entry.getResource().getResourceType().equals(ResourceType.Organization))
        .filter(
            orgEntry ->
                ((Organization) orgEntry.getResource())
                    .getIdentifier().stream()
                        .anyMatch(
                            identifier ->
                                identifier
                                    .getSystem()
                                    .equals(DeBasisNamingSystem.IKNR.getCanonicalUrl())))
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    AssignerOrganization.class, entry, AssignerOrganization::fromOrganization))
        .findFirst();
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

  public MedicationCategory getMedicationCategory() {
    val medication = this.getMedication();
    return medication.getCategoryFirstRep();
  }

  public StandardSize getMedicationStandardSize() {
    val medication = this.getMedication();
    return medication.getStandardSize();
  }

  public Optional<Darreichungsform> getMedicationSupplyForm() {
    val medication = this.getMedication();
    return medication.getDarreichungsformFirstRep();
  }

  public int getMedicationAmount() {
    val medication = this.getMedication();
    int ret = 0; // Note: return simply 0 if amount was given as this field is optional
    if (medication.getAmount().getNumerator().getValue() != null) {
      ret = medication.getAmount().getNumerator().getValue().intValue();
    }
    return ret;
  }

  public String getMedicationPzn() {
    val medication = this.getMedication();
    return medication.getPznFirstRep();
  }

  public String getMedicationName() {
    val medication = this.getMedication();
    return medication.getMedicationName();
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
        .map(
            entry ->
                FhirEntryReplacer.cast(
                    SupplyRequest.class,
                    entry,
                    resource -> {
                      val sr = new SupplyRequest();
                      sr.copyValues(resource);
                      return sr;
                    }))
        .findFirst();
  }

  public int getDispenseQuantity() {
    val request = this.getMedicationRequest();
    return request.getDispenseRequest().getQuantity().getValue().intValue();
  }

  public String getDosageInstruction() {
    val request = this.getMedicationRequest();
    return request.getDosageInstructionFirstRep().getText();
  }

  public boolean isSubstitutionAllowed() {
    val request = this.getMedicationRequest();
    val allowedType = request.getSubstitution().getAllowedBooleanType();
    return !allowedType.isEmpty() && allowedType.booleanValue();
  }

  /**
   * KBV does encode the version number within the profile e.g.
   * https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1" This method cuts of the
   * version and returns the plain profile as String
   *
   * @param profile the profile (canonical) as String which has the following shape [URL]|[VERSION]
   * @return only the URL part
   */
  private String splitToProfileUrl(String profile) {
    return profile.split("\\|")[0];
  }

  /**
   * KBV does encode the version number within the profile e.g.
   * https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_Bundle|1.0.1" This method cuts of the
   * profile URL and returns the plain Version as String
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
    val type = (this.isMultiple()) ? "MVO Verordnung" : "einfache Verordnung";
    val workflow = this.getFlowType();
    return format(
        "{0} (Workflow {1}) {2} für {3}",
        workflow.getDisplay(), workflow.getCode(), type, this.getPatient().getDescription());
  }
}
