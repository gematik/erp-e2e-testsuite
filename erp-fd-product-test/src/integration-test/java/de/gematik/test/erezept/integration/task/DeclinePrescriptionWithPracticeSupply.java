/*
 *  Copyright 2024 gematik GmbH
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package de.gematik.test.erezept.integration.task;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCodeIs;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.KbvProfileRules;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.MedicationRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.SupplyRequestBuilder;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.resources.kbv.MedicalOrganization;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.screenplay.util.PrescriptionAssignmentKind;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit.runners.SerenityParameterizedRunner;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.hl7.fhir.r4.model.Medication;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@Slf4j
@RunWith(SerenityParameterizedRunner.class)
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("E-Rezept mit PracticeSupply ausstellen")
class DeclinePrescriptionWithPracticeSupply extends ErpTest {
    @Actor(name = "Adelheid Ulmenwald")
    private DoctorActor doctor;

    @Actor(name = "Sina Hüllmann")
    private PatientActor sina;

    @TestcaseId("ERP_TASK_INVALID_PRESCRIPTION_01")
    @DisplayName("invalides E-Rezept mit PracticeSupply und MedicationRequest ausstellen")
    @Test
    void activateInvalidPrescriptionWithPracticeSupplyAndMedicationRequest() {
        sina.changePatientInsuranceType(VersicherungsArtDeBasis.GKV);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val supplyRequest = getSupplyRequest(medication);
    val coverage = KbvCoverageFaker.builder().fake();
    val kbvBundleBuilder =
        generateKbvBundle(
            UUID.randomUUID().toString(),
            coverage,
            medication,
            MedicalOrganizationFaker.builder().fake(),
            supplyRequest);

    kbvBundleBuilder.medicationRequest(
        MedicationRequestFaker.builder()
            .withPatient(sina.getPatientData())
            .withMedication(medication)
            .withRequester(doctor.getPractitioner())
            .fake());

    val issuePrescription =
        IssuePrescription.forPatient(sina)
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
        sina.changePatientInsuranceType(VersicherungsArtDeBasis.GKV);
    val medication = KbvErpMedicationPZNFaker.builder().fake();
    val supplyRequest = getSupplyRequest(medication);
    val kbvBundleBuilder =
        generateKbvBundle(
            UUID.randomUUID().toString(),
            KbvCoverageFaker.builder().fake(),
            medication,
            MedicalOrganizationFaker.builder().fake(),
            supplyRequest);

        val issuePrescription = IssuePrescription.forPatient(sina)
                .ofAssignmentKind(PrescriptionAssignmentKind.PHARMACY_ONLY);

        val activation = doctor.performs(issuePrescription.withKbvBundleFrom(kbvBundleBuilder));

        doctor.attemptsTo(
                Verify.that(activation)
                        .withIndefiniteType()
                        .hasResponseWith(returnCodeIs(400, KbvProfileRules.SUPPLY_REQUEST_AND_MEDICATION_REQUEST))
                        .isCorrect());
    }

    private SupplyRequest getSupplyRequest(KbvErpMedication medication) {
        return SupplyRequestBuilder.withCoverage(sina.getInsuranceCoverage())
                .medication(medication)
                .requester(doctor.getPractitioner())
                .build();
    }

    private KbvErpBundleBuilder generateKbvBundle(String prescriptionId, KbvCoverage coverage, Medication medication, MedicalOrganization organization, SupplyRequest supplyRequest) {
        return KbvErpBundleBuilder.forPrescription(prescriptionId)
                .practitioner(doctor.getPractitioner())
                .custodian(organization)
                .patient(sina.getPatientData())
                .insurance(coverage)
                .statusKennzeichen("00") // 00/NONE is default
                .supplyRequest(supplyRequest)
                .medication(medication);
    }



}
