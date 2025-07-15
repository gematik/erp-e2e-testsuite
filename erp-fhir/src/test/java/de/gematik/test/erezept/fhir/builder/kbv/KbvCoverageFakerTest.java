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

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerValueSet;
import static de.gematik.test.erezept.fhir.builder.GemFaker.randomElement;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import lombok.val;
import org.junit.jupiter.api.Test;

class KbvCoverageFakerTest extends ErpFhirParsingTest {

  @Test
  void buildFakeKbvCoverageWithPersonGroup() {
    val coverage =
        KbvCoverageFaker.builder().withPersonGroup(fakerValueSet(PersonGroup.class)).fake();

    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvCoverageWithDmpKennzeichen() {
    val coverage = KbvCoverageFaker.builder().withDmpKennzeichen(DmpKennzeichen.DM1).fake();
    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvCoverageWithWop() {
    val coverage = KbvCoverageFaker.builder().withWop(fakerValueSet(Wop.class)).fake();
    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvCoverageWithInsuranceStatus() {
    val coverage =
        KbvCoverageFaker.builder()
            .withInsuranceStatus(fakerValueSet(VersichertenStatus.class))
            .fake();
    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvCoverageWithBeneficiary() {
    val patient = KbvPatientFaker.builder().fake();
    val coverage = KbvCoverageFaker.builder().withBeneficiary(patient).fake();
    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }

  @Test
  void buildFakeKbvCoverageWithInsuranceType() {
    val coverage =
        KbvCoverageFaker.builder()
            .withInsuranceType(randomElement(InsuranceTypeDe.PKV, InsuranceTypeDe.GKV))
            .fake();
    val result = parser.validate(coverage);
    assertTrue(result.isSuccessful());
  }
}
