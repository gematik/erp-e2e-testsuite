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

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.format;

import de.gematik.test.erezept.fhir.parser.profiles.definitions.KbvItaErpStructDef;
import de.gematik.test.erezept.fhir.parser.profiles.systems.ErpWorkflowNamingSystem;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedicationRequest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.AccidentCauseType;
import de.gematik.test.erezept.fhir.valuesets.PkvTariff;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Date;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.SupplyRequest;

/**
 * This builder provides a convenient way to build <a
 * href="https://simplifier.net/erezept/kbvprerpbundle">KBV ERP Bundles</a>
 */
@Slf4j
public class KbvErpBundleBuilder
    extends KbvBaseDocumentBundleBuilder<KbvItaErpVersion, KbvErpBundle, KbvErpBundleBuilder> {

  private InstitutionalOrganization assignerOrganization;
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
    return forPrescription(new PrescriptionId(prescriptionId));
  }

  public static KbvErpBundleBuilder forPrescription(PrescriptionId prescriptionId) {
    return new KbvErpBundleBuilder().prescriptionId(prescriptionId);
  }

  public KbvErpBundleBuilder assigner(InstitutionalOrganization organization) {
    this.assignerOrganization = organization;
    return self();
  }

  public KbvErpBundleBuilder medicationRequest(KbvErpMedicationRequest medicationRequest) {
    this.medicationRequest = medicationRequest;
    return self();
  }

  public KbvErpBundleBuilder supplyRequest(SupplyRequest supplyRequest) {
    this.supplyRequest = supplyRequest;
    return self();
  }

  public KbvErpBundleBuilder medication(Medication medication) {
    this.medication = medication;
    return self();
  }

  public KbvErpBundle build() {
    checkRequired();
    val profileCanonical = KbvItaErpStructDef.BUNDLE.asCanonicalType(version);
    val kbv = this.createResource(KbvErpBundle::new, profileCanonical);

    // set FHIR-specific values provided by HAPI
    kbv.setType(Bundle.BundleType.DOCUMENT);
    kbv.setTimestamp(new Date());

    kbv.addEntry(compositionBuilder.createEntryFor("Coverage", coverage));
    kbv.addEntry(compositionBuilder.createEntryFor(Composition::getSubject, patient));
    kbv.addEntry(compositionBuilder.createEntryFor(Composition::addAuthor, practitioner, true));
    kbv.addEntry(compositionBuilder.createEntryFor(Composition::getCustodian, medicalOrganization));

    // Note: Medication does not require an entry within the composition
    kbv.addEntry(compositionBuilder.createEntryFor(medication));

    Optional.ofNullable(this.attester)
        .ifPresent(a -> kbv.addEntry(compositionBuilder.createAttesterEntry(a)));

    // note: MedicationRequestEntry is valid without SupplyRequestEntry
    Optional.ofNullable(this.medicationRequest)
        .ifPresent(
            mr ->
                kbv.addEntry(compositionBuilder.createEntryFor("Prescription", medicationRequest)));

    // note: SupplyRequestEntry is valid without MedicationRequestEntry
    Optional.ofNullable(this.supplyRequest)
        .ifPresent(
            sr -> kbv.addEntry(compositionBuilder.createEntryFor("PracticeSupply", supplyRequest)));

    val isOldProfile = version.compareTo(KbvItaErpVersion.V1_1_0) < 0;
    val isPkvCoverage =
        coverage
            .getInsuranceKindOptional()
            .map(insuranceKind -> insuranceKind.equals(VersicherungsArtDeBasis.PKV))
            .orElse(false);
    if (isPkvCoverage && isOldProfile) {
      kbv.addEntry(compositionBuilder.createEntryFor(assignerOrganization));

      // PKV has also an extension for PKV Tariff
      compositionBuilder.addExtension(PkvTariff.BASIS.asExtension());
    }

    if (version.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      kbv.setIdentifier(this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID));
    } else {
      kbv.setIdentifier(
          this.prescriptionId.asIdentifier(ErpWorkflowNamingSystem.PRESCRIPTION_ID_121));
    }

    compositionBuilder.addExtension(statusKennzeichen.asExtension());
    val compositionEntry = compositionBuilder.buildBundleEntryComponent();
    kbv.getEntry().add(0, compositionEntry);

    return kbv;
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

    if (version.compareTo(KbvItaErpVersion.V1_1_0) < 0) {
      // old profile
      if (coverage.getInsuranceKind() == VersicherungsArtDeBasis.PKV) {
        // assigner organization not required from kbv.ita.erp-1.1.0??
        this.checkRequired(
            assignerOrganization,
            format(
                "KBV Bundle with PKV patient requires an assigner organization for {0}", version));
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
