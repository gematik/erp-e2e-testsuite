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

package de.gematik.test.erezept.fhir.builder.kbv;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ca.uhn.fhir.model.api.TemporalPrecisionEnum;
import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.r4.kbv.KbvCoverage;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedication;
import de.gematik.test.erezept.fhir.r4.kbv.KbvPractitioner;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import java.util.Date;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class SupplyRequestBuilderTest extends ErpFhirParsingTest {

  private static KbvCoverage coverage;

  private static KbvErpMedication medication;

  private static KbvPractitioner practitioner;

  @BeforeAll
  static void setup() {
    coverage = KbvCoverageFaker.builder().fake();
    medication = KbvErpMedicationPZNFaker.builder().fake();
    practitioner = KbvPractitionerFaker.builder().fake();
  }

  @Test
  void shouldBuildSimpleSupplyRequest() {
    val builder =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(medication)
            .requester(practitioner)
            .authoredOn(new Date());
    val supplyRequest = assertDoesNotThrow(builder::build);
    assertNotNull(supplyRequest);

    val resultSupplyRequest = ValidatorUtil.encodeAndValidate(parser, supplyRequest);
    assertTrue(resultSupplyRequest.isSuccessful());
    assertEquals(
        "https://fhir.kbv.de/StructureDefinition/KBV_PR_ERP_PracticeSupply|1.1.0",
        supplyRequest.getMeta().getProfile().get(0).getValue());
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
  void shouldThrowNullPointerExceptionCausedByEmptyCoverage() {
    val sr = SupplyRequestBuilder.withCoverage(new KbvCoverage()).requester(practitioner);
    assertThrows(BuilderException.class, sr::build);
  }

  @Test
  void authoredOnShouldWorkWithTemporalPrecision() {
    val supplyRequest =
        SupplyRequestBuilder.withCoverage(coverage)
            .medication(KbvErpMedicationPZNFaker.builder().fake())
            .requester(KbvPractitionerFaker.builder().fake())
            .authoredOn(new Date(), TemporalPrecisionEnum.DAY)
            .build();
    assertNotNull(supplyRequest);
    val resultSupplyRequest = ValidatorUtil.encodeAndValidate(parser, supplyRequest);
    assertTrue(resultSupplyRequest.isSuccessful());
  }
}
