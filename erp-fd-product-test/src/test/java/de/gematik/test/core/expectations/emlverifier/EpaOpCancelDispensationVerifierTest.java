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

package de.gematik.test.core.expectations.emlverifier;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.core.expectations.verifier.emlverifier.EpaOpCancelDispensationVerifier;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelDispensation;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpCancelDispensationVerifierTest {

  private static EpaOpCancelDispensation epaOpCancelDispensation;
  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));

  @BeforeAll
  static void setup() {
    val fhir = EpaFhirFactory.create();
    epaOpCancelDispensation =
        fhir.decode(
            EpaOpCancelDispensation.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/parameters/Parameters-example-epa-op-cancel-dispensation-erp-input-parameters-1.json"));
  }

  @Test
  void emlPrescriptionIdSuccessTest() {
    val expectedPrescriptionId = PrescriptionId.from("160.153.303.257.459");
    VerificationStep<EpaOpCancelDispensation> verificationStep =
        EpaOpCancelDispensationVerifier.emlPrescriptionIdIsEqualTo(expectedPrescriptionId);

    assertTrue(verificationStep.getPredicate().test(epaOpCancelDispensation));
  }

  @Test
  void emlPrescriptionIdFailureTest() {
    val wrongPrescriptionId = PrescriptionId.from("123456");
    VerificationStep<EpaOpCancelDispensation> verificationStep =
        EpaOpCancelDispensationVerifier.emlPrescriptionIdIsEqualTo(wrongPrescriptionId);

    assertFalse(verificationStep.getPredicate().test(epaOpCancelDispensation));
  }

  @Test
  void emlAuthoredOnSuccessTest() {
    VerificationStep<EpaOpCancelDispensation> verificationStep =
        EpaOpCancelDispensationVerifier.emlAuthoredOnIsEqualTo(testDate_22_01_2025);

    assertTrue(verificationStep.getPredicate().test(epaOpCancelDispensation));
  }

  @Test
  void emlAuthoredOnFailureTest() {
    val incorrectDate =
        DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 30));
    VerificationStep<EpaOpCancelDispensation> verificationStep =
        EpaOpCancelDispensationVerifier.emlAuthoredOnIsEqualTo(incorrectDate);

    assertFalse(verificationStep.getPredicate().test(epaOpCancelDispensation));
  }
}
