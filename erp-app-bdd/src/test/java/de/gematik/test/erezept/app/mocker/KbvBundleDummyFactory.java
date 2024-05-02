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

package de.gematik.test.erezept.app.mocker;

import de.gematik.test.erezept.fhir.builder.kbv.*;
import de.gematik.test.erezept.fhir.extensions.kbv.MultiplePrescriptionExtension;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.StatusCoPayment;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.Date;
import lombok.val;

public class KbvBundleDummyFactory {

  public static KbvErpBundle createSimpleKbvBundle(
      PrescriptionId prescriptionId, MultiplePrescriptionExtension mvo) {
    return createSimpleKbvBundle(prescriptionId, StatusCoPayment.STATUS_0, mvo);
  }

  public static KbvErpBundle createSimpleKbvBundle(
      PrescriptionId prescriptionId, StatusCoPayment coPayment, MultiplePrescriptionExtension mvo) {
    val practitioner = PractitionerFaker.builder().fake();
    val medicalOrganization = MedicalOrganizationFaker.builder().fake();
    val assignerOrganization = AssignerOrganizationFaker.builder().fake();

    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .withAssignerRef(assignerOrganization)
            .fake();
    val insurance =
        KbvCoverageBuilder.faker(VersicherungsArtDeBasis.GKV).beneficiary(patient).build();
    val medication =
        KbvErpMedicationPZNFaker.builder().withCategory(MedicationCategory.C_00).fake();
    val medicationRequest =
        MedicationRequestFaker.builder(patient)
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
        .custodian(medicalOrganization)
        .assigner(assignerOrganization)
        .patient(patient)
        .insurance(insurance)
        .medicationRequest(medicationRequest)
        .medication(medication)
        .build();
  }
}
