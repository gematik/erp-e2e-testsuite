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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.test.erezept.fhir.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.resources.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.resources.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.Date;
import lombok.val;
import org.hl7.fhir.r4.model.Practitioner;
import org.hl7.fhir.r4.model.SupplyRequest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SupplyRequestBuilderTest extends ParsingTest {

  private static KbvCoverage coverage;

  private static KbvErpMedication medication;

  private static Practitioner practitioner;

  @BeforeAll
  static void setup() {
    coverage = KbvCoverageFaker.builder().fake();
    medication = KbvErpMedicationPZNFaker.builder().fake();
    practitioner = PractitionerFaker.builder().fake();
  }

  @Test
  void buildShouldBuild() {
    assertNotNull(
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(medication)
            .requester(practitioner)
            .build());
  }

  @Test
  void fakeSupplyRequestShouldWork() {
    KbvCoverage coverage = KbvCoverageFaker.builder().fake();
    SupplyRequest supplyRequest =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(medication)
            .requester(practitioner)
            .build();
    assertEquals(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply|1.1.0",
        supplyRequest.getMeta().getProfile().get(0).getValue());
  }

  @Test
  void fakeForPatientShouldWork() {
    val sr =
        SupplyRequestBuilder.fakeForPatient(PatientFaker.builder().fake())
            .medication(medication)
            .requester(practitioner)
            .coverage(coverage)
            .build();
    assertNotNull(sr);
  }

  @Test
  void supplyRequestShouldBeValid() {
    val supplyRequest =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .requester(PractitionerFaker.builder().fake())
            .build();
    val resultSupplyRequest = ValidatorUtil.encodeAndValidate(parser, supplyRequest);
    assertTrue(resultSupplyRequest.isSuccessful());
  }

  @Test
  void shouldThrowNullPointerExcCausedByMissingCoverage() {
    val patient = PatientFaker.builder().fake();
    var sr = SupplyRequestBuilder.fakeForPatient(patient).requester(practitioner);
    assertThrows(BuilderException.class, sr::build);
  }

  @Test
  void shouldThrowNullPointerExcCausedByMissingRequester() {
    val sr = SupplyRequestBuilder.withCoverage(coverage);
    assertThrows(BuilderException.class, sr::build);
  }

  @Test
  void shouldThrowNullPointerExcCausedByMissingMedicationReference() {
    val sr = SupplyRequestBuilder.withCoverage(coverage).requester(practitioner);
    assertThrows(BuilderException.class, sr::build);
  }

  @Test
  void shouldThrowNullPointerExcCausedByEmpty() {
    val sr = SupplyRequestBuilder.withCoverage(new KbvCoverage()).requester(practitioner);
    assertThrows(BuilderException.class, sr::build);
  }

  @Test
  void shouldThrowNullPointerException() {
    val sr = SupplyRequestBuilder.withCoverage(new KbvCoverage());

    assertThrows(NullPointerException.class, () -> sr.medication(null));
  }

  @Test
  void authoredOnShouldWork() {
    val supplyRequest =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .requester(PractitionerFaker.builder().fake())
            .authoredOn(new Date())
            .build();
    assertNotNull(supplyRequest);
  }

  @Test
  void authoredOnShouldWorkWithTemporalPrecision() {
    val supplyRequest =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .requester(PractitionerFaker.builder().fake())
            .authoredOn(new Date(), TemporalPrecisionEnum.DAY)
            .build();
    assertNotNull(supplyRequest);
  }
}
