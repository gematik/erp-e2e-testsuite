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

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;

import com.ibm.icu.impl.Pair;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvMedicalOrganization;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPatient;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept mit PracticeSupply ausstellen")
class DeclinePrescriptionWithPracticeSupplyIT extends ErpTest {
  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor patient;

  @TestcaseId("ERP_TASK_INVALID_PRESCRIPTION_01")
  @DisplayName("invalides E-Rezept mit PracticeSupply und MedicationRequest ausstellen")
  @Test
  void activateInvalidPrescriptionWithPracticeSupplyAndMedicationRequest() {
    patient.changePatientInsuranceType(InsuranceTypeDe.GKV);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val supplyRequest = getSupplyRequest(medication);

    val patientCoverage = patient.getPatientCoverage();
    val kbvBundleBuilder =
        generateKbvBundle(
            patientCoverage,
            medication,
            KbvMedicalOrganizationFaker.builder().fake(),
            supplyRequest);

    kbvBundleBuilder.medicationRequest(
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patientCoverage.first)
            .withMedication(medication)
            .withRequester(doctor.getPractitioner())
            .fake());

    val issuePrescription =
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY);
    val activation = doctor.performs(issuePrescription.withKbvBundleFrom(kbvBundleBuilder));

    doctor.attemptsTo(
        Verify.that(activation)
            .withIndefiniteType()
            .hasResponseWith(
                returnCodeIs(400, KbvProfileRules.SUPPLY_REQUEST_AND_MEDICATION_REQUEST))
            .isCorrect());
  }

  @TestcaseId("ERP_TASK_VALID_PRESCRIPTION_WITH_SUPPLY_REQUEST_02")
  @DisplayName("valides E-Rezept mit PracticeSupply wird vom FD abgelehnt")
  @Test
  void activateValidPrescriptionWithPracticeSupply() {
    patient.changePatientInsuranceType(InsuranceTypeDe.GKV);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val supplyRequest = getSupplyRequest(medication);
    val patientCoverage = patient.getPatientCoverage();
    val kbvBundleBuilder =
        generateKbvBundle(
            patientCoverage,
            medication,
            KbvMedicalOrganizationFaker.builder().fake(),
            supplyRequest);

    val issuePrescription =
        IssuePrescription.forPatient(patient)
            .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY);

    val activation = doctor.performs(issuePrescription.withKbvBundleFrom(kbvBundleBuilder));

    doctor.attemptsTo(
        Verify.that(activation)
            .withIndefiniteType()
            .hasResponseWith(
                returnCodeIs(400, KbvProfileRules.SUPPLY_REQUEST_AND_MEDICATION_REQUEST))
            .isCorrect());
  }

  private SupplyRequest getSupplyRequest(KbvErpMedication medication) {
    val patientCoverage = patient.getPatientCoverage();
    return SupplyRequestBuilder.withCoverage(patientCoverage.second)
        .medication(medication)
        .requester(doctor.getPractitioner())
        .build();
  }

  private KbvErpBundleBuilder generateKbvBundle(
      Pair<KbvPatient, KbvCoverage> patientCoverage,
      Medication medication,
      KbvMedicalOrganization organization,
      SupplyRequest supplyRequest) {

    return KbvErpBundleBuilder.forPrescription(PrescriptionId.random())
        .practitioner(doctor.getPractitioner())
        .medicalOrganization(organization)
        .patient(patientCoverage.first)
        .insurance(patientCoverage.second)
        .statusKennzeichen("00", doctor.getPractitioner()) // 00/NONE is default
        .supplyRequest(supplyRequest)
        .medication(medication);
  }
}
