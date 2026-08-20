/*
 * Copyright (Change Date see Readme), gematik GmbH
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

package de.gematik.test.erezept.integration.scenarios;

import static de.gematik.test.core.expectations.verifier.ErpResponseVerifier.returnCode;
import static de.gematik.test.core.expectations.verifier.TaskVerifier.isInReadyStatus;

import de.gematik.test.core.annotations.Actor;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.ErpBfd;
import de.gematik.test.erezept.ErpTest;
import de.gematik.test.erezept.actions.IssuePrescription;
import de.gematik.test.erezept.actions.Verify;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.fhir.builder.kbv.KbvCoverageBuilder;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPatientFaker;
import de.gematik.test.erezept.fhir.builder.kbv.KbvPractitionerFaker;
import de.gematik.test.erezept.fhir.values.GkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@Slf4j
@ExtendWith(SerenityJUnit5Extension.class)
@DisplayName("Prüfe, dass alternative Kostenträger vom FD akzeptiert werden")
@Tag("ErpDifferentInsuranceTypes")
class DifferentInsuranceTypesIT extends ErpTest {

  @Actor(name = "Adelheid Ulmenwald")
  private DoctorActor doctor;

  @Actor(name = "Sina Hüllmann")
  private PatientActor sina;

  @Test
  @TestcaseId("ERP_PRESCRIPTION_PAYOR_01")
  @DisplayName("Fachdienst akzeptiert Verordnungsdatensatz für den Kostenträger SKT")
  void shouldAcceptPrescriptionWithSKTPayorType() {
    val patientResource = KbvPatientFaker.builder().fake();
    val tkCoverageInfo = GkvInsuranceCoverageInfo.TK;
    val coverage =
        KbvCoverageBuilder.insurance(tkCoverageInfo)
            .beneficiary(patientResource)
            .insuranceType(PayorType.SKT)
            .personGroup(PersonGroup.NOT_SET)
            .versichertenStatus(VersichertenStatus.PENSIONER)
            .build();

    val kbvBundle =
        KbvErpBundleFaker.builder()
            .withInsurance(coverage, patientResource)
            .withPractitioner(KbvPractitionerFaker.builder().fake())
            .toBuilder();

    val activation =
        doctor.performs(IssuePrescription.forPatient(sina).withKbvBundleFrom(kbvBundle));

    doctor.attemptsTo(
        Verify.that(activation)
            .withExpectedType(ErpBfd.B_FD_1357)
            .hasResponseWith(returnCode(200))
            .and(isInReadyStatus())
            .isCorrect());
  }
}
