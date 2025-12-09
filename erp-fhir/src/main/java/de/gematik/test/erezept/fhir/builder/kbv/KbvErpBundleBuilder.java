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

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.DeBasisProfilNamingSystem;
import de.gematik.test.erezept.fhir.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.*;

/**
 * This builder provides a convenient way to build <a
 * href="https://simplifier.net/erezept/kbvprerpbundle">KBV ERP Bundles</a>
 */
@Slf4j
public class KbvErpBundleBuilder
    extends KbvBaseDocumentBundleBuilder<KbvItaErpVersion, KbvErpBundle, KbvErpBundleBuilder> {

  private KbvErpMedicationRequest medicationRequest;
  private SupplyRequest supplyRequest;
  private Medication medication;

  private KbvErpBundleBuilder() {
    super(KbvErpCompositionBuilder.builder(), KbvItaErpVersion.getDefaultVersion());
  }

  public static KbvErpBundleBuilder builder() {
    return new KbvErpBundleBuilder();
  }

  public static KbvErpBundleBuilder forPrescription(String prescriptionId) {
    return forPrescription(PrescriptionId.from(prescriptionId));
  }

  public static KbvErpBundleBuilder forPrescription(PrescriptionId prescriptionId) {
    return new KbvErpBundleBuilder().prescriptionId(prescriptionId);
  }

  public KbvErpBundleBuilder medicationRequest(KbvErpMedicationRequest medicationRequest) {
    this.medicationRequest = medicationRequest;
    return this;
  }

  public KbvErpBundleBuilder supplyRequest(SupplyRequest supplyRequest) {
    this.supplyRequest = supplyRequest;
    return this;
  }

  public KbvErpBundleBuilder medication(Medication medication) {
    this.medication = medication;
    return this;
  }

  @Override
  public KbvErpBundle build() {
    checkRequired();
    practitioner.getQualificationType();
    val bundle = this.createResource(KbvErpBundle::new, KbvItaErpStructDef.BUNDLE, version);

    // set FHIR-specific values provided by HAPI
    bundle.setType(Bundle.BundleType.DOCUMENT);
    bundle.setTimestamp(new Date());

    bundle.setIdentifier(this.prescriptionId.asIdentifier());

    bundle.addEntry(compositionBuilder.createEntryFor("Coverage", coverage));
    bundle.addEntry(compositionBuilder.createEntryFor(Composition::getSubject, patient));
    bundle.addEntry(compositionBuilder.createEntryFor(Composition::addAuthor, practitioner, true));
    bundle.addEntry(
        compositionBuilder.createEntryFor(Composition::getCustodian, this.medicalOrganization));

    // Note: Medication does not require an entry within the composition
    bundle.addEntry(compositionBuilder.createEntryFor(medication));

    Optional.ofNullable(this.attester)
        .ifPresent(a -> bundle.addEntry(compositionBuilder.createAttesterEntry(a)));

    // note: MedicationRequestEntry is valid without SupplyRequestEntry
    Optional.ofNullable(this.medicationRequest)
        .ifPresent(
            mr -> {
              bundle.addEntry(compositionBuilder.createEntryFor("Prescription", medicationRequest));

              // adjust the references in medication request to ensure integrity of the references
              mr.setRequester(practitioner.asReference());
              mr.setSubject(patient.asReference());
              mr.getInsurance().get(0).setReference(coverage.asReference().getReference());
            });

    // note: SupplyRequestEntry is valid without MedicationRequestEntry
    Optional.ofNullable(this.supplyRequest)
        .ifPresent(
            sr ->
                bundle.addEntry(
                    compositionBuilder.createEntryFor("PracticeSupply", supplyRequest)));
    // in case of ASV a KBV_PR_FOR_PRACTITIONER_ROLE ist mandatory
    Optional.ofNullable(this.practitionerRole)
        .ifPresent(
            pR -> bundle.addEntry(compositionBuilder.createEntryFor("FOR_PractitionerRole", pR)));

    compositionBuilder.addExtension(statusKennzeichen.asExtension());
    val compositionEntry = compositionBuilder.buildBundleEntryComponent();
    bundle.getEntry().add(0, compositionEntry);

    // adjust some more references
    coverage.setBeneficiary(patient.asReference());

    return bundle;
  }

  private void checkRequired() {
    this.checkRequired(patient, "KBV Bundle requires a patient");
    this.checkRequired(coverage, "KBV Bundle requires a coverage");
    this.checkRequired(medication, "KBV Bundle requires a medication");
    this.checkRequired(practitioner, "KBV Bundle requires a practitioner");
    this.checkRequired(medicalOrganization, "KBV Bundle requires a custodian organization");
    if (version.compareTo(KbvItaErpVersion.V1_1_0) > 0
        && practitioner.getQualificationType().equals(QualificationType.DENTIST)
        && medicalOrganization.getIdentifier().stream()
            .noneMatch(
                i ->
                    i.getSystem()
                        .contains(
                            DeBasisProfilNamingSystem.KZBV_KZVA_ABRECHNUNGSNUMMER_SID
                                .getCanonicalUrl()))) {

      val msg =
          format(
              "Medical Organization requires a KZVA Abrechnungsnummer for Dentists in KBV Bundle"
                  + " from version {0} also see"
                  + " https://simplifier.net/packages/kbv.ita.for/1.2.0/files/2777636",
              version.getVersion());
      throw new BuilderException(msg);
    }
    if (supplyRequest == null) {
      this.checkRequired(
          medicationRequest, "KBV Bundle requires a medication request without a supply request");
    }
    if (medicationRequest == null) {
      this.checkRequired(
          supplyRequest, "KBV Bundle requires a supply request without a medication request");
    }
  }
}
