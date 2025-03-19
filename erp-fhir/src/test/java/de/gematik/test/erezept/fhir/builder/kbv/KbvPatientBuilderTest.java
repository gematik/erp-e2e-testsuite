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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvPatientBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildGkvPatientWithFaker(KbvItaForVersion version) {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.GKV)
            .withVersion(version)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(InsuranceTypeDe.GKV, insuranceKind);
    assertTrue(patient.hasGkvKvnr());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildPkvPatientWithFaker(KbvItaForVersion version) {
    val patient =
        KbvPatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), InsuranceTypeDe.PKV)
            .withVersion(version)
            .fake();

    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(InsuranceTypeDe.PKV, insuranceKind);
    assertTrue(patient.hasPkvKvnr());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnEmptyPatientBuilder(KbvItaForVersion version) {
    val pb = KbvPatientBuilder.builder().version(version);
    assertThrows(BuilderException.class, pb::build);
  }

  @Test
  void shouldThrowOnInvalidDateFormat() {
    val builder = KbvPatientBuilder.builder();
    assertThrows(IllegalArgumentException.class, () -> builder.birthDate("123123", "hello"));
  }
}
