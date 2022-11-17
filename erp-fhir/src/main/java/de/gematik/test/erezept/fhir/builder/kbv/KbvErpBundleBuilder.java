/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.fhir.builder.kbv;

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;
import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.builder.AbstractResourceBuilder;
import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.references.kbv.CoverageReference;
import de.gematik.test.erezept.fhir.references.kbv.MedicationRequestReference;
import de.gematik.test.erezept.fhir.references.kbv.OrganizationReference;
import de.gematik.test.erezept.fhir.references.kbv.RequesterReference;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import lombok.NonNull;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * This builder provides a convenient way to build <a
 * href="https://simplifier.net/erezept/kbvprerpbundle">KBV ERP Bundles</a>
 */
public class KbvErpBundleBuilder extends AbstractResourceBuilder<KbvErpBundleBuilder> {

  /**
   * KBV Prüfnummer e.g. <a
   * href="https://update.kbv.de/ita-update/Service-Informationen/Zulassungsverzeichnisse/KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf">KBV_ITA_SIEX_Verzeichnis_AVWG_eRezept.pdf</a>
   */
  private static final String DEVICE_AUTHOR_ID = "GEMATIK/410/2109/36/123";

  private static final String BASE_URL = "https://pvs.gematik.de/fhir/";

  private KbvItaErpVersion kbvItaErpVersion = KbvItaErpVersion.getDefaultVersion();

  private PrescriptionId prescriptionId;

  private Bundle.BundleEntryComponent compositionEntry;
  private final KbvCompositionBuilder compositionBuilder = KbvCompositionBuilder.builder();

  private StatusKennzeichen statusKennzeichen = StatusKennzeichen.NONE;

  private KbvPatient patient;
  private Coverage coverage;
  private MedicalOrganization medicalOrganization; // the organization issuing the prescription
  private InstitutionalOrganization assignerOrganization;
  private Practitioner practitioner;
  private MedicationRequest medicationRequest;
  private Medication medication;

  private KbvErpBundleBuilder() {}

  public static KbvErpBundleBuilder builder() {
    return new KbvErpBundleBuilder();
  }

  public static KbvErpBundleBuilder forPrescription(@NonNull String prescriptionId) {
    return forPrescription(new PrescriptionId(prescriptionId));
  }

  public static KbvErpBundleBuilder forPrescription(@NonNull PrescriptionId prescriptionId) {
    return new KbvErpBundleBuilder().prescriptionId(prescriptionId);
  }

  public static KbvErpBundleBuilder faker() {
    return forPrescription(fakerPrescriptionId());
  }

  public static KbvErpBundleBuilder faker(String kvid) {
    return faker(kvid, new Date());
  }

  public static KbvErpBundleBuilder faker(String kvid, Date authoredOn) {
    return faker(kvid, authoredOn, fakerPzn());
  }

  public static KbvErpBundleBuilder faker(String kvid, String pzn) {
    return faker(kvid, new Date(), pzn);
  }

  public static KbvErpBundleBuilder faker(String kvid, Date authoredOn, String pzn) {
    return faker(kvid, pzn, authoredOn, fakerPrescriptionId());
  }

  public static KbvErpBundleBuilder faker(String kvid, PrescriptionId prescriptionId) {
    return faker(kvid, new Date(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      String kvid, Date authoredOn, PrescriptionId prescriptionId) {
    return faker(kvid, fakerPzn(), authoredOn, prescriptionId);
  }

  public static KbvErpBundleBuilder faker(String kvid, String pzn, PrescriptionId prescriptionId) {
    return faker(kvid, pzn, new Date(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      String kvid, String pzn, Date authoredOn, PrescriptionId prescriptionId) {
    return faker(kvid, pzn, fakerDrugName(), authoredOn, fakerBool(), prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      String kvid,
      String pzn,
      String medicationName,
      boolean substitution,
      PrescriptionId prescriptionId) {
    return faker(kvid, pzn, medicationName, new Date(), substitution, prescriptionId);
  }

  public static KbvErpBundleBuilder faker(
      String kvid,
      String pzn,
      String medicationName,
      Date authoredOn,
      boolean substitution,
      PrescriptionId prescriptionId) {
    val practitioner = PractitionerBuilder.faker().build();
    val medicalOrganization = MedicalOrganizationBuilder.faker().build();
    val assignerOrganization = AssignerOrganizationBuilder.faker().build();
    val patient =
        PatientBuilder.faker(kvid, IdentifierTypeDe.GKV).assigner(assignerOrganization).build();
    val insurance = CoverageBuilder.faker(VersicherungsArtDeBasis.GKV).beneficiary(patient).build();
    val medication =
        KbvErpMedicationBuilder.faker(pzn, medicationName, MedicationCategory.C_00).build();
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

  public KbvErpBundleBuilder insurance(@NonNull Coverage coverage) {
    this.coverage = coverage;
    return self();
  }

  public KbvErpBundleBuilder practitioner(@NonNull Practitioner practitioner) {
    this.practitioner = practitioner;
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

  public KbvErpBundleBuilder medicationRequest(@NonNull MedicationRequest medicationRequest) {
    this.medicationRequest = medicationRequest;
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

  private KbvErpBundleBuilder addComposition(@NonNull Composition composition) {
    val fullUrl = BASE_URL + "Composition/" + composition.getId();
    this.compositionEntry =
        new Bundle.BundleEntryComponent().setResource(composition).setFullUrl(fullUrl);

    return self();
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
    extendCoverage();
    val coverageEntry =
        createBundleEntry(coverage, () -> coverageReference, compositionBuilder::coverageReference);

    val patientEntry =
        createBundleEntry(
            patient, () -> patient.getReference(), compositionBuilder::subjectReference);

    val practitionerEntry =
        createBundleEntry(
            practitioner,
            () -> new RequesterReference(practitioner.getId()),
            compositionBuilder::requesterReference);

    val organizationEntry =
        createBundleEntry(
            medicalOrganization,
            () -> new OrganizationReference(medicalOrganization.getId()),
            compositionBuilder::custodianReference);

    val medicationRequestEntry =
        createBundleEntry(
            medicationRequest,
            () -> new MedicationRequestReference(medicationRequest.getId()),
            compositionBuilder::medicationRequestReference);

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
                compositionEntry,
                medicationRequestEntry,
                medicationEntry,
                patientEntry,
                organizationEntry,
                coverageEntry));
    kbv.getEntry().add(practitionerEntry);

    if (patient.getInsuranceKind() == VersicherungsArtDeBasis.PKV) {
      if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
        val assignerFullUrl = BASE_URL + "Organization/" + assignerOrganization.getId();
        val assignerEntry =
            new Bundle.BundleEntryComponent()
                .setResource(assignerOrganization)
                .setFullUrl(assignerFullUrl);
        kbv.getEntry().add(assignerEntry);
      }

      // PKV has also an extension for PKV Tariff
      kbv.getComposition().addExtension(PkvTariff.BASIS.asExtension());
    }

    compositionBuilder.addExtension(statusKennzeichen.asExtension());

    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      val identifier = this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID);
      kbv.setIdentifier(identifier);
    } else {
      val identifier =
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121);
      kbv.setIdentifier(identifier);
    }

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

  /** if GKV, BG, SKT or UK set legal-basis extension (Rechtsgrundlage) */
  private void extendCoverage() {
    // Note: check this rule from KBV_PR_ERP_Bundle: Rule says GKV/BG/SKT/UK but I have only GKV/BG
    //    coverage.getType().getCoding().stream()
    //        .filter(
    //            c ->
    // c.getSystem().equals(ErpCodeSystem.VERSICHERUNGSART_DE_BASIS.getCanonicalUrl()))
    //        .map(c -> VersicherungsArtDeBasis.fromCode(c.getCode()))
    //        .filter(va -> va == VersicherungsArtDeBasis.GKV || va == VersicherungsArtDeBasis.BG)
    //        .findFirst()
    //        .ifPresent(va ->
    // compositionBuilder.addExtension(StatusKennzeichen.NONE.asExtension()));
    // TODO: remove when not required anymore!!
  }

  private void checkRequired() {
    this.checkRequired(patient, "KBV Bundle requires a patient");
    this.checkRequired(coverage, "KBV Bundle requires a coverage");
    this.checkRequired(medicationRequest, "KBV Bundle requires a medication request");
    this.checkRequired(medication, "KBV Bundle requires a medication");
    this.checkRequired(practitioner, "KBV Bundle requires a practitioner");
    this.checkRequired(medicalOrganization, "KBV Bundle requires a custodian organization");

    if (kbvItaErpVersion.compareTo(KbvItaErpVersion.V1_1_0) < 0
        && patient.getInsuranceKind() == VersicherungsArtDeBasis.PKV) {
      // assigner organization not required from kbv.ita.erp-1.1.0??
      this.checkRequired(
          assignerOrganization,
          format(
              "KBV Bundle with PKV patient requires an assigner organization for {0}",
              kbvItaErpVersion));
    }
  }
}
