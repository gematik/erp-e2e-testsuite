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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaErpVersion;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.valuesets.MedicationCategory;
import de.gematik.test.erezept.fhir.valuesets.QualificationType;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;

class KbvErpBundleBuilderWithTeratogenicPrescTest extends ErpFhirParsingTest {

  final KbvErpMedication medication =
      KbvErpMedicationPZNFaker.builder(KbvItaErpVersion.V1_4_0)
          .withCategory(MedicationCategory.C_02)
          .withVaccine(false)
          .withPznMedication(PZN.from("19201712"), "Pomalidomid Accord 1 mg 21 x 1 Hartkapseln")
          .fake();

  @RepeatedTest(3)
  void shouldBuildTPrescriptionWithFaker() {
    val kbvBundle =
        KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMedication(medication)
            .withDispenseQuantity(3535)
            .expectedSupplyDurationInWeeks(3)
            .withKvnr(KVNR.random())
            .fake();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, kbvBundle).isSuccessful());
    assertEquals(
        3,
        kbvBundle
            .getMedicationRequest()
            .getDispenseRequest()
            .getExpectedSupplyDuration()
            .getValue()
            .intValue());
  }

  @RepeatedTest(3)
  void shouldBuildTPrescriptionWithMinimalFaker() {
    val kbvBundle =
        KbvErpBundleFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMedication(KbvErpMedicationPZNFaker.asTPrescription())
            .withKvnr(KVNR.random())
            .fake();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, kbvBundle).isSuccessful());
  }

  @RepeatedTest(3)
  void shouldBuildTPrescriptionWitBuilder() {

    val medRequest =
        KbvErpMedicationRequestFaker.builder(KbvItaErpVersion.V1_4_0, KbvItaForVersion.V1_3_0)
            .withMedication(medication)
            .withExpectedSupplyDurationInWeeks(3)
            .fake();
    val coverage = KbvCoverageFaker.builder(KbvItaForVersion.V1_3_0).fake();
    val practitioner =
        KbvPractitionerFaker.builder(KbvItaForVersion.V1_3_0)
            .withQualificationType(QualificationType.DOCTOR)
            .fake();
    val organization =
        KbvMedicalOrganizationFaker.forPractitioner(practitioner, KbvItaForVersion.V1_3_0).fake();

    val kbvBundle =
        KbvErpBundleBuilder.builder()
            .version(KbvItaErpVersion.V1_4_0)
            .medication(medication)
            .insurance(coverage)
            .practitioner(practitioner)
            .medicalOrganization(organization)
            .prescriptionId(PrescriptionId.random())
            .medicationRequest(medRequest)
            .patient(KbvPatientFaker.builder(KbvItaForVersion.V1_3_0).fake())
            .build();
    assertTrue(ValidatorUtil.encodeAndValidate(parser, kbvBundle).isSuccessful());
  }
}
