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
 */

package de.gematik.test.erezept.app.mocker;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.builder.kbv.KbvAssignerOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationPZNFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpMedicationRequestFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvMedicalOrganizationFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import java.util.Date;
import lombok.val;

public class KbvBundleDummyFactory {

  public static KbvErpBundle createSimpleKbvBundle(
      PrescriptionId prescriptionId, MultiplePrescriptionExtension mvo) {
    return createSimpleKbvBundle(prescriptionId, StatusCoPayment.STATUS_0, mvo);
  }

  public static KbvErpBundle createSimpleKbvBundle(
      PrescriptionId prescriptionId, StatusCoPayment coPayment, MultiplePrescriptionExtension mvo) {
    val practitioner = KbvPractitionerFaker.builder().fake();
    val medicalOrganization = KbvMedicalOrganizationFaker.builder().fake();
    val assignerOrganization = KbvAssignerOrganizationFaker.builder().fake();

    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
            .withAssignerRef(assignerOrganization)
            .fake();
    val insurance =
        KbvCoverageFaker.builder()
            .withInsuranceType(InsuranceTypeDe.GKV)
            .withBeneficiary(patient)
            .fake();
    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();
    val medicationRequest =
        KbvErpMedicationRequestFaker.builder()
            .withPatient(patient)
            .withInsurance(insurance)
            .withRequester(practitioner)
            .withMedication(medication)
            .withCoPaymentStatus(coPayment)
            .withMvo(mvo)
            .withSubstitution(false)
            .withAuthorDate(new Date())
            .fake();

    return KbvErpBundleBuilder.forPrescription(prescriptionId.getValue())
        .practitioner(practitioner)
        .medicalOrganization(medicalOrganization)
        .assigner(assignerOrganization)
        .patient(patient)
        .insurance(insurance)
        .medicationRequest(medicationRequest)
        .medication(medication)
        .build();
  }
}
