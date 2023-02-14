/*
 * Copyright (c) 2023 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.fhir.builder.kbv;

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.exceptions.*;
import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.valuesets.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

@Slf4j
class PatientBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildGkvPatientWithFaker(KbvItaForVersion version) {
    val patient = PatientBuilder.faker("X123123123", IdentifierTypeDe.GKV).version(version).build();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.GKV, insuranceKind);
    assertTrue(patient.hasGkvId());
  }

  @ParameterizedTest(name = "[{index}] -> Build KBV PKV Patient with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildPkvPatientWithFaker(KbvItaForVersion version) {
    val patient = PatientBuilder.faker("X123123123", IdentifierTypeDe.PKV).version(version).build();
    log.info(format("Validating Faker Patient with ID {0}", patient.getLogicalId()));
    val result = ValidatorUtil.encodeAndValidate(parser, patient);
    assertTrue(result.isSuccessful());

    val insuranceKind = patient.getInsuranceKind();
    assertEquals(VersicherungsArtDeBasis.PKV, insuranceKind);
    assertTrue(patient.hasPkvId());
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
        PatientBuilder.faker("X123123123", IdentifierTypeDe.GKV) // GKV Faker won't set assigner!
            .version(version);
    pb.kvIdentifierDe("X123123123", IdentifierTypeDe.PKV); // setting PKV without assigner
    assertThrows(BuilderException.class, pb::build);
  }
}
