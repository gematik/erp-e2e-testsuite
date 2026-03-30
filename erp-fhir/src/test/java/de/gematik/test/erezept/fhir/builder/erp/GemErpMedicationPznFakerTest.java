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

package de.gematik.test.erezept.fhir.builder.erp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.fhir.de.value.PZN;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.profiles.version.ErpWorkflowVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedClass;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;

@ParameterizedClass
@MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#erpWorkflowVersions")
@RequiredArgsConstructor
class GemErpMedicationPznFakerTest extends ErpFhirParsingTest {

  private final ErpWorkflowVersion version;

  @RepeatedTest(5)
  void shouldRandomlyFake() {
    val medication = GemErpMedicationFaker.forPznMedication(version).fake();
    assertTrue(parser.isValid(medication));
  }

  @Test
  void shouldFakeEverything() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(true);

      val medication = GemErpMedicationFaker.forPznMedication(version).fake();
      assertTrue(parser.isValid(medication));
    }
  }

  @Test
  void shouldFakeWithFixedPzn() {
    try (val mockFaker = mockStatic(GemFaker.class, Mockito.CALLS_REAL_METHODS)) {
      mockFaker.when(GemFaker::fakerBool).thenReturn(false);
      val pzn = PZN.random();
      val medication = GemErpMedicationFaker.forPznMedication(version).withPzn(pzn).fake();
      assertTrue(parser.isValid(medication));
      assertEquals(
          pzn.getValue(), medication.getPzn().stream().findFirst().orElseThrow().getValue());
    }
  }
}
