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

import static de.gematik.test.erezept.fhir.builder.GemFaker.*;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.extensions.kbv.AccidentExtension;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.resources.InstitutionalOrganization;
import de.gematik.test.erezept.fhir.resources.kbv.*;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PZN;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.StatusKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import lombok.val;
import org.hl7.fhir.r4.model.MedicationRequest;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.Quantity;

public class KbvErpBundleFaker {
  private PrescriptionId prescriptionId = fakerPrescriptionId();
  private final KbvCoverageFaker kbvCoverageFaker;
  private final MedicationRequestFaker medicationRequestFaker;
  private final Map<String, Consumer<KbvErpBundleBuilder>> builderConsumers = new HashMap<>();
  private static final String KEY_COVERAGE = "coverage"; // key used for builderConsumers map

  private KbvErpBundleFaker() {
    val assignerOrganization = AssignerOrganizationFaker.builder().fake();
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .withAssignerRef(assignerOrganization)
            .fake();
    val practitioner = PractitionerFaker.builder().fake();
    kbvCoverageFaker =
        KbvCoverageFaker.builder()
            .withInsuranceType(VersicherungsArtDeBasis.GKV)
            .withBeneficiary(patient);

    val kbvErpMedication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();

    medicationRequestFaker =
        MedicationRequestFaker.builder()
            .withPatient(patient)
            .withRequester(practitioner)
            .withMedication(kbvErpMedication)
            .withAuthorDate(new Date())
            .withSubstitution(fakerBool());

    builderConsumers.put("patient", b -> b.patient(patient));
    builderConsumers.put(
        "medOrganization", b -> b.custodian(MedicalOrganizationFaker.builder().fake()));
    builderConsumers.put("assignerOrganization", b -> b.assigner(assignerOrganization));
    builderConsumers.put("practitioner", b -> b.practitioner(practitioner));
    builderConsumers.put("medication", b -> b.medication(kbvErpMedication));
  }

  public static KbvErpBundleFaker builder() {
    return new KbvErpBundleFaker();
  }

  public KbvErpBundleFaker withVersion(KbvItaErpVersion version) {
    builderConsumers.put("version", b -> b.version(version));
    return this;
  }

  public KbvErpBundleFaker withPrescriptionId(PrescriptionId prescriptionId) {
    this.prescriptionId = prescriptionId;
    return this;
  }

  public KbvErpBundleFaker withPrescriptionId(String code) {
    this.prescriptionId = PrescriptionId.from(code);
    return this;
  }

  public KbvErpBundleFaker withStatusKennzeichen(StatusKennzeichen statusKennzeichen) {
    builderConsumers.put("statusKennzeichen", b -> b.statusKennzeichen(statusKennzeichen));
    return this;
  }

  public KbvErpBundleFaker withStatusKennzeichen(String code) {
    return this.withStatusKennzeichen(StatusKennzeichen.fromCode(code));
  }

  public KbvErpBundleFaker withKvnr(KVNR kvnr) {
    val newAssignerOrg = AssignerOrganizationFaker.builder().fake();
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(kvnr, VersicherungsArtDeBasis.GKV)
            .withAssignerRef(newAssignerOrg)
            .fake();
    this.withAssignerOrganization(newAssignerOrg);
    return this.withPatient(patient);
  }

  public KbvErpBundleFaker withPatient(KbvPatient patient) {
    kbvCoverageFaker.withBeneficiary(patient);
    medicationRequestFaker.withPatient(patient);
    builderConsumers.put("patient", b -> b.patient(patient));
    return this;
  }

  public KbvErpBundleFaker withInsurance(KbvCoverage coverage, KbvPatient patient) {
    medicationRequestFaker.withInsurance(coverage);
    this.withPatient(patient);
    builderConsumers.put(KEY_COVERAGE, b -> b.insurance(coverage));
    return this;
  }

  public KbvErpBundleFaker withDosageInstruction(String text) {
    medicationRequestFaker.withDosageInstruction(text);
    return this;
  }

  public KbvErpBundleFaker withBvg(boolean bvg) {
    medicationRequestFaker.withBvg(bvg);
    return this;
  }

  public KbvErpBundleFaker withEmergencyServiceFee(boolean emergencyServiceFee) {
    medicationRequestFaker.withEmergencyServiceFee(emergencyServiceFee);
    return this;
  }

  public KbvErpBundleFaker withAccident(AccidentExtension accident) {
    medicationRequestFaker.withAccident(accident);
    return this;
  }

  public KbvErpBundleFaker withMedicationRequestVersion(KbvItaErpVersion version) {
    medicationRequestFaker.withVersion(version);
    return this;
  }

  public KbvErpBundleFaker withQuantity(
      MedicationRequest.MedicationRequestDispenseRequestComponent quantity) {
    medicationRequestFaker.withQuantity(quantity);
    return this;
  }

  public KbvErpBundleFaker withQuantity(Quantity quantity) {
    medicationRequestFaker.withQuantity(quantity);
    return this;
  }

  public KbvErpBundleFaker withQuantityPackages(int amount) {
    medicationRequestFaker.withQuantityPackages(amount);
    return this;
  }

  public KbvErpBundleFaker withCoPaymentStatus(StatusCoPayment statusCoPayment) {
    medicationRequestFaker.withCoPaymentStatus(statusCoPayment);
    return this;
  }

  public KbvErpBundleFaker withStatus(MedicationRequest.MedicationRequestStatus status) {
    medicationRequestFaker.withStatus(status);
    return this;
  }

  public KbvErpBundleFaker withStatus(String code) {
    medicationRequestFaker.withStatus(code);
    return this;
  }

  public KbvErpBundleFaker withIntent(String code) {
    medicationRequestFaker.withIntent(code);
    return this;
  }

  public KbvErpBundleFaker withMvo(MultiplePrescriptionExtension mvo) {
    medicationRequestFaker.withMvo(mvo);
    return this;
  }

  public KbvErpBundleFaker withNote(String note) {
    medicationRequestFaker.withNote(note);
    return this;
  }

  public KbvErpBundleFaker withCustodian(MedicalOrganization organization) {
    builderConsumers.computeIfPresent(
        "medOrganization", (key, defaultValue) -> b -> b.custodian(organization));
    return this;
  }

  public KbvErpBundleFaker withAssignerOrganization(InstitutionalOrganization organization) {
    builderConsumers.computeIfPresent(
        "assignerOrganization", (key, defaultValue) -> b -> b.assigner(organization));
    return this;
  }

  public KbvErpBundleFaker withPractitioner(Practitioner practitioner) {
    medicationRequestFaker.withRequester(practitioner);
    builderConsumers.computeIfPresent(
        "practitioner", (key, defaultValue) -> b -> b.practitioner(practitioner));
    return this;
  }

  public KbvErpBundleFaker withAuthorDate(Date authoredOn) {
    medicationRequestFaker.withAuthorDate(authoredOn);
    return this;
  }

  public KbvErpBundleFaker withAuthorDate(Date authoredOn, TemporalPrecisionEnum precisionEnum) {
    medicationRequestFaker.withAuthorDate(authoredOn, precisionEnum);
    return this;
  }

  public KbvErpBundleFaker withSubstitution(boolean substitution) {
    medicationRequestFaker.withSubstitution(substitution);
    return this;
  }

  public KbvErpBundleFaker withMedication(KbvErpMedication medication) {
    medicationRequestFaker.withMedication(medication);
    builderConsumers.computeIfPresent(
        "medication", (key, defaultValue) -> b -> b.medication(medication));
    return this;
  }

  public KbvErpBundleFaker withPznAndMedicationName(PZN pzn, String medicationName) {
    val medication =
        KbvErpMedicationPZNFaker.builder()
            .withPznMedication(pzn, medicationName)
            .withCategory(MedicationCategory.C_00)
            .fake();
    return this.withMedication(medication);
  }

  public KbvErpBundleFaker withAttester(Practitioner attester) {
    builderConsumers.put("attester", b -> b.attester(attester));
    return this;
  }

  public KbvErpBundle fake() {
    return this.toBuilder().build();
  }

  public KbvErpBundleBuilder toBuilder() {
    val builder = KbvErpBundleBuilder.forPrescription(prescriptionId);
    val coverage = kbvCoverageFaker.fake();
    if (!builderConsumers.containsKey(KEY_COVERAGE)) {
      medicationRequestFaker.withInsurance(coverage);
    }
    builderConsumers.computeIfAbsent(KEY_COVERAGE, key -> b -> b.insurance(coverage));
    builderConsumers.put(
        "medicationRequest", b -> b.medicationRequest(medicationRequestFaker.fake()));

    builderConsumers.values().forEach(c -> c.accept(builder));
    return builder;
  }
}
