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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.references.kbv.*;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * This builder provides a convenient way to build <a
 * href="https://simplifier.net/erezept/kbvprerpbundle">KBV ERP Bundles</a>
 */
@Slf4j
public class KbvErpBundleBuilder extends AbstractResourceBuilder<KbvErpBundleBuilder> {

  /**
   * KBV Prüfnummer e.g. <a
   * href="https://update.kbv.de/ita-update/Service-Informationen/Zulassungsverzeichnisse/KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf">KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf</a>
   */
  @SuppressWarnings({"java:S6418"}) // this is not an AUTH token
  private static final String DEVICE_AUTHOR_ID = "GEMATIK/410/2109/36/123";

  private static final String BASE_URL = "https://pvs.gematik.de/fhir/";
  private final KbvCompositionBuilder compositionBuilder = KbvCompositionBuilder.builder();
  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();
  private PrescriptionId prescriptionId;
  private Bundle.BundleEntryComponent compositionEntry;
  private StatusKennzeichen statusKennzeichen = StatusKennzeichen.NONE;

  private KbvPatient patient;
  private KbvCoverage coverage;
  private MedicalOrganization medicalOrganization; // the organization issuing the prescription
  private InstitutionalOrganization assignerOrganization;
  private Practitioner practitioner;
  private KbvErpMedicationRequest medicationRequest;
  private SupplyRequest supplyRequest;
  private Medication medication;
  private Practitioner attester;

  private KbvErpBundleBuilder() {}

  /**
   * @deprecated (when KbvBundleBuilderTest.buildFakerWithGivenMedicationCategory() have been
   *     refactored
   */
  @Deprecated(since = "founded", forRemoval = false)
  public static KbvErpBundleBuilder builder() {
    return new KbvErpBundleBuilder();
  }

  public static KbvErpBundleBuilder forPrescription(@NonNull String prescriptionId) {
    return forPrescription(new PrescriptionId(prescriptionId));
  }

  public static KbvErpBundleBuilder forPrescription(@NonNull PrescriptionId prescriptionId) {
    return new KbvErpBundleBuilder().prescriptionId(prescriptionId);
  }

  /**
   * Faker produces a Bundle with Medication_PZN !!!
   *
   * @return KbvErpBundleBuilder
   */
  public static KbvErpBundleBuilder faker() {
    return faker(KVNR.random(), fakerPrescriptionId());
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr) {
    return faker(kvnr, new Date());
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr, Date authoredOn) {
    return faker(kvnr, authoredOn, PZN.random().getValue());
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr, String pzn) {
    return faker(kvnr, new Date(), pzn);
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr, Date authoredOn, String pzn) {
    return faker(kvnr, pzn, authoredOn, fakerPrescriptionId());
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr, PrescriptionId prescriptionId) {
    return faker(kvnr, new Date(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      KVNR kvnr, Date authoredOn, PrescriptionId prescriptionId) {
    return faker(kvnr, PZN.random().getValue(), authoredOn, prescriptionId);
  }

  public static KbvErpBundleBuilder faker(KVNR kvnr, String pzn, PrescriptionId prescriptionId) {
    return faker(kvnr, pzn, new Date(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      KVNR kvnr, String pzn, Date authoredOn, PrescriptionId prescriptionId) {
    return faker(kvnr, pzn, fakerDrugName(), authoredOn, fakerBool(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      KVNR kvnr,
      String pzn,
      String medicationName,
      boolean substitution,
      PrescriptionId prescriptionId) {
    return faker(kvnr, pzn, medicationName, new Date(), substitution, prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      KVNR kvnr,
      String pzn,
      String medicationName,
      Date authoredOn,
      boolean substitution,
      PrescriptionId prescriptionId) {
    val practitioner = PractitionerBuilder.faker().build();
    val medicalOrganization = MedicalOrganizationBuilder.faker().build();
    val assignerOrganization = AssignerOrganizationBuilder.faker().build();
    val patient =
        PatientBuilder.faker(kvnr, VersicherungsArtDeBasis.GKV)
            .assigner(assignerOrganization)
            .build();
    val insurance =
        KbvCoverageBuilder.faker(VersicherungsArtDeBasis.GKV).beneficiary(patient).build();
    val medication =
        KbvErpMedicationPZNBuilder.faker(pzn, medicationName, MedicationCategory.C_00).build();
    val medicationRequest =
        MedicationRequestBuilder.faker(patient, authoredOn, substitution)
            .insurance(insurance)
            .requester(practitioner)
            .medication(medication)
            .build();
    return KbvErpBundleBuilder.forPrescription(prescriptionId.getValue())
        .practitioner(practitioner)
        .custodian(medicalOrganization)
        .assigner(assignerOrganization)
        .patient(patient)
        .insurance(insurance)
        .medicationRequest(medicationRequest)
        .medication(medication);
  }

  /**
   * <b>Attention:</b> use with care as this setter might break automatic choice of the version.
   * This builder will set the default version automatically, so there should be no need to provide
   * an explicit version
   *
   * @param version to use for generation of this resource
   * @return Builder
   */
  public KbvErpBundleBuilder version(KbvItaErpVersion version) {
    this.kbvItaErpVersion = version;
    this.compositionBuilder.version(version);
    return this;
  }

  public KbvErpBundleBuilder prescriptionId(@NonNull PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return self();
  }

  public KbvErpBundleBuilder patient(@NonNull KbvPatient patient) {
    this.patient = patient;
    return self();
  }

  public KbvErpBundleBuilder insurance(@NonNull KbvCoverage coverage) {
    this.coverage = coverage;
    return self();
  }

  public KbvErpBundleBuilder practitioner(@NonNull Practitioner practitioner) {
    this.practitioner = practitioner;
    return self();
  }

  public KbvErpBundleBuilder attester(@NonNull Practitioner attester) {
    this.attester = attester;
    return self();
  }

  public KbvErpBundleBuilder custodian(@NonNull MedicalOrganization organization) {
    this.medicalOrganization = organization;
    return self();
  }

  public KbvErpBundleBuilder assigner(@NonNull InstitutionalOrganization organization) {
    this.assignerOrganization = organization;
    return self();
  }

  public KbvErpBundleBuilder medicationRequest(@NonNull KbvErpMedicationRequest medicationRequest) {
    this.medicationRequest = medicationRequest;
    return self();
  }

  public KbvErpBundleBuilder supplyRequest(@NonNull SupplyRequest supplyRequest) {
    this.supplyRequest = supplyRequest;
    return self();
  }

  public KbvErpBundleBuilder medication(@NonNull Medication medication) {
    this.medication = medication;
    return self();
  }

  public KbvErpBundleBuilder statusKennzeichen(@NonNull String code) {
    return statusKennzeichen(StatusKennzeichen.fromCode(code));
  }

  public KbvErpBundleBuilder statusKennzeichen(StatusKennzeichen statusKennzeichen) {
    this.statusKennzeichen = statusKennzeichen;
    return self();
  }

  private void addComposition(@NonNull Composition composition) {
    val fullUrl = BASE_URL + "Composition/" + composition.getId();
    this.compositionEntry =
        new Bundle.BundleEntryComponent().setResource(composition).setFullUrl(fullUrl);
  }

  public KbvErpBundle build() {
    checkRequired();
    val kbv = new KbvErpBundle();

    val profile = KbvItaErpStructDef.BUNDLE.asCanonicalType(kbvItaErpVersion);
    val meta = new Meta().setLastUpdated(new Date()).setProfile(List.of(profile));

    // set FHIR-specific values provided by HAPI
    kbv.setType(Bundle.BundleType.DOCUMENT);
    kbv.setTimestamp(new Date());
    kbv.setId(this.getResourceId()).setMeta(meta);

    val coverageReference = new CoverageReference(coverage.getId());
    val coverageEntry =
        createBundleEntry(
            coverage, coverageReference::asReference, compositionBuilder::coverageReference);

    val patientEntry =
        createBundleEntry(
            patient,
            () -> patient.getReference().asReference(),
            compositionBuilder::subjectReference);

    val practitionerEntry =
        createBundleEntry(
            practitioner,
            () -> new RequesterReference(practitioner.getId()).asReference(),
            compositionBuilder::requesterReference);

    val organizationEntry =
        createBundleEntry(
            medicalOrganization,
            () -> new OrganizationReference(medicalOrganization.getId()).asReference(),
            compositionBuilder::custodianReference);

    // Note: Medication does not require an entry within the composition
    val medicationFullUrl = BASE_URL + "Medication/" + medication.getId();
    val medicationEntry =
        new Bundle.BundleEntryComponent().setResource(medication).setFullUrl(medicationFullUrl);

    // assemble the KBV Bundle
    // build and add the child-builders for composition/medicationRequest etc.
    this.addComposition(compositionBuilder.addDeviceAuthor(DEVICE_AUTHOR_ID).build());

    // add all entries
    kbv.getEntry()
        .addAll(
            List.of(
                compositionEntry, medicationEntry, patientEntry, organizationEntry, coverageEntry));

    kbv.getEntry().add(practitionerEntry);

    if (this.attester != null) {
      kbv.getEntry()
          .add(
              createBundleEntry(
                  attester,
                  () -> new RequesterReference(attester.getId()).asReference(),
                  compositionBuilder::attesterReference));
    }
    // note: MedicationRequestEntry is valid without SupplyRequestEntry
    if (medicationRequest != null) {
      val medicationRequestEntry =
          createBundleEntry(
              medicationRequest,
              () -> new MedicationRequestReference(medicationRequest.getId()).asReference(),
              compositionBuilder::medicationRequestReference);
      kbv.getEntry().add(medicationRequestEntry);
    }

    // note: SupplyRequestEntry is valid without MedicationRequestEntry
    if (supplyRequest != null) {
      val supplyRequestEntry =
          createBundleEntry(
              supplyRequest,
              () -> new SupplyRequestReference(supplyRequest.getId()).asReference(),
              compositionBuilder::supplyRequestReference);
      kbv.getEntry().add(supplyRequestEntry);
    }

    val isOldProfile = kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0;
    val isPkvCoverage =
        coverage
            .getInsuranceKindOptional()
            .map(insuranceKind -> insuranceKind.equals(VersicherungsArtDeBasis.PKV))
            .orElse(false);
    if (isPkvCoverage && isOldProfile) {
      val assignerFullUrl = BASE_URL + "Organization/" + assignerOrganization.getId();
      val assignerEntry =
          new Bundle.BundleEntryComponent()
              .setResource(assignerOrganization)
              .setFullUrl(assignerFullUrl);
      kbv.getEntry().add(assignerEntry);

      // PKV has also an extension for PKV Tariff
      kbv.getComposition().addExtension(PkvTariff.BASIS.asExtension());
    }

    compositionBuilder.addExtension(statusKennzeichen.asExtension());

    Identifier identifier;
    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      identifier = this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
    } else {
      identifier = this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
    }
    kbv.setIdentifier(identifier);

    return kbv;
  }

  /**
   * this method will create a BundleEntryComponent and link this one properly to the given consumer
   * via its reference
   *
   * @param resource is the wrapped resource within the BundleEntryComponent
   * @param refSupplier provides the proper reference for the resource
   * @param consumer adds the reference to composition
   * @return a ready to use BundleEntryComponent
   */
  private <T extends Reference> Bundle.BundleEntryComponent createBundleEntry(
      Resource resource, Supplier<T> refSupplier, Consumer<T> consumer) {
    val reference = refSupplier.get();
    val fullUrl = createFullUrl(reference.getReference());
    consumer.accept(reference);
    return new Bundle.BundleEntryComponent().setResource(resource).setFullUrl(fullUrl);
  }

  private String createFullUrl(String id) {
    return BASE_URL + id;
  }

  private void checkRequired() {
    this.checkRequired(patient, "KBV Bundle requires a patient");
    this.checkRequired(coverage, "KBV Bundle requires a coverage");
    if (supplyRequest == null) {
      this.checkRequired(
          medicationRequest, "KBV Bundle requires a medication request without a supply request");
    }
    if (medicationRequest == null) {
      this.checkRequired(
          supplyRequest, "KBV Bundle requires a supply request without a medication request");
    }
    this.checkRequired(medication, "KBV Bundle requires a medication");
    this.checkRequired(practitioner, "KBV Bundle requires a practitioner");
    this.checkRequired(medicalOrganization, "KBV Bundle requires a custodian organization");

    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      // old profile
      if (coverage.getInsuranceKind() == VersicherungsArtDeBasis.PKV) {
        // assigner organization not required from kbv.ita.erp-1.1.0??
        this.checkRequired(
            assignerOrganization,
            format(
                "KBV Bundle with PKV patient requires an assigner organization for {0}",
                kbvItaErpVersion));
      }
    } else {
      if (medicationRequest != null) {
        // new profile
        medicationRequest
            .getAccident()
            .filter(accident -> !accident.accidentCauseType().equals(AccidentCauseType.ACCIDENT))
            .ifPresent(
                accident -> {
                  // in case of "Arbeitsunfall" or "Berufskrankheit" coverage is provided by a
                  // "Berufsgenossenschaft"
                  // and the patient is in this case always GKV (gesetzlich krankenversichert)
                  if (!coverage.getInsuranceKind().equals(VersicherungsArtDeBasis.BG)) {
                    log.warn(
                        format(
                            "Accident set to {0} and insurance is of type {1} but must be {2}",
                            accident.accidentCauseType().getDisplay(),
                            coverage.getInsuranceKind(),
                            VersicherungsArtDeBasis.BG));
                  }
                });
      }
    }
  }
}
