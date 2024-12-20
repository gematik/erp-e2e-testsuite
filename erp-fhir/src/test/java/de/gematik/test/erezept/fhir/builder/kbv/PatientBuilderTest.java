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

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.test.erezept.fhir.parser.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
class PatientBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildGkvPatientWithFaker(KbvItaForVersion version) {
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.GKV)
            .withVersion(version)
            .fake();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.GKV, insuranceKind);
    assertTrue(patient.hasGkvKvnr());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildPkvPatientWithFaker(KbvItaForVersion version) {
    val patient =
        PatientFaker.builder()
            .withKvnrAndInsuranceType(KVNR.random(), VersicherungsArtDeBasis.PKV)
            .withVersion(version)
            .fake();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.PKV, insuranceKind);
    assertTrue(patient.hasPkvKvnr());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnEmptyPatientBuilder(KbvItaForVersion version) {
    val pb = PatientBuilder.builder().version(version);
    assertThrows(BuilderException.class, pb::build);
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void shouldFailOnPkvPatientWithoutAssigner(KbvItaForVersion version) {
    val pb =
        PatientBuilder.faker(
                KVNR.random(), VersicherungsArtDeBasis.GKV) // GKV Faker won't set assigner!
            .version(version);
    pb.kvnr(KVNR.random(), VersicherungsArtDeBasis.PKV); // setting PKV without assigner
    if (version.compareTo(KbvItaForVersion.V1_1_0) < 0) {
      assertThrows(BuilderException.class, pb::build);
    } else {
      assertDoesNotThrow(pb::build);
    }
  }

  @Test
  void shouldThrowOnInvalidDateFormat() {
    val builder = PatientBuilder.builder();
    assertThrows(IllegalArgumentException.class, () -> builder.birthDate("123123", "hello"));
  }
}
