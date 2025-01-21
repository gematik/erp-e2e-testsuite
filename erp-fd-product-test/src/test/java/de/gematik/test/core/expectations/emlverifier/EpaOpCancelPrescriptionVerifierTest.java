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

package de.gematik.test.core.expectations.emlverifier;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.verifier.VerificationStep;
import de.gematik.test.core.expectations.verifier.emlverifier.EpaOpCancelPrescriptionVerifier;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.eml.fhir.r4.EpaOpCancelPrescription;
import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.time.LocalDate;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.function.Predicate;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class EpaOpCancelPrescriptionVerifierTest {

  private static EpaOpCancelPrescription epaOpCancelPrescription;
  private static final Date testDate_22_01_2025 =
      DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 22));

  @BeforeAll
  static void setup() {
    val fhir = EpaFhirFactory.create();
    epaOpCancelPrescription =
        fhir.decode(
            EpaOpCancelPrescription.class,
            ResourceLoader.readFileFromResource(
                "fhir/valid/medication/Parameters-example-epa-op-cancel-prescription-erp-input-parameters-1.json"));
  }

  @Test
  void emlPrescriptionIdSuccessTest() {
    val expectedPrescriptionId = new PrescriptionId("160.153.303.257.459");
    VerificationStep<EpaOpCancelPrescription> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlPrescriptionIdIsEqualTo(expectedPrescriptionId);

    assertTrue(verificationStep.getPredicate().test(epaOpCancelPrescription));
  }

  @Test
  void emlPrescriptionIdFailureTest() {
    val wrongPrescriptionId = new PrescriptionId("123456");
    VerificationStep<EpaOpCancelPrescription> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlPrescriptionIdIsEqualTo(wrongPrescriptionId);

    assertFalse(verificationStep.getPredicate().test(epaOpCancelPrescription));
  }

  @Test
  void emlAuthoredOnSuccessTest() {
    VerificationStep<EpaOpCancelPrescription> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlAuthoredOnIsEqualTo(testDate_22_01_2025);

    assertTrue(verificationStep.getPredicate().test(epaOpCancelPrescription));
  }

  @Test
  void emlAuthoredOnFailureTest() {
    val incorrectDate =
        DateConverter.getInstance().localDateToDate(LocalDate.of(2025, Month.JANUARY, 23));
    VerificationStep<EpaOpCancelPrescription> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlAuthoredOnIsEqualTo(incorrectDate);

    assertFalse(verificationStep.getPredicate().test(epaOpCancelPrescription));
  }

  @Test
  void EmlDoesNotContainAnythingWithEmptyList() {
    List<EpaOpCancelPrescription> emptyList = List.of();

    VerificationStep<List<EpaOpCancelPrescription>> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlDoesNotContainAnything();

    Predicate<List<EpaOpCancelPrescription>> predicate = verificationStep.getPredicate();

    assertTrue(predicate.test(emptyList), "Expected verification step to pass for an empty list.");
  }

  @Test
  void EmlDoesNotContainAnythingWithNonEmptyList() {
    List<EpaOpCancelPrescription> nonEmptyList =
        List.of(new EpaOpCancelPrescription(), new EpaOpCancelPrescription());

    VerificationStep<List<EpaOpCancelPrescription>> verificationStep =
        EpaOpCancelPrescriptionVerifier.emlDoesNotContainAnything();

    Predicate<List<EpaOpCancelPrescription>> predicate = verificationStep.getPredicate();

    assertFalse(
        predicate.test(nonEmptyList), "Expected verification step to fail for a non-empty list.");
  }
}
