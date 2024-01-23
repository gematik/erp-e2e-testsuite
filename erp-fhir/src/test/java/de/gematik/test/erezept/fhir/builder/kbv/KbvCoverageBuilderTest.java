/*
 * Copyright 2023 gematik GmbH
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

import static java.text.MessageFormat.*;
import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.parser.profiles.version.*;
import de.gematik.test.erezept.fhir.testutil.*;
import de.gematik.test.erezept.fhir.values.GkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.*;
import java.util.*;
import lombok.*;
import lombok.extern.slf4j.*;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

@Slf4j
class KbvCoverageBuilderTest extends ParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Coverage with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFixedValues(KbvItaForVersion version) {
    val patient = PatientBuilder.faker().version(version).build();
    val coverage =
        KbvCoverageBuilder.insurance(GkvInsuranceCoverageInfo.TK)
            .version(version)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET) // default NOT_SET
            .dmpKennzeichen(DmpKennzeichen.ASTHMA) // default NOT_SET
            .wop(Wop.BERLIN) // default DUMMY
            .versichertenStatus(VersichertenStatus.PENSIONER) // default MEMBERS
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());

    // check the getters here because we do not yet have static files for coverages
    assertEquals(patient.getInsuranceKind(), coverage.getInsuranceKind());
    assertEquals("101575519", coverage.getIknr().getValue());
    assertEquals("Techniker Krankenkasse", coverage.getName());
    assertNotNull(coverage.getDescription());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build KBV Coverage with UK PayorType with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithUKPayorType(KbvItaForVersion version) {
    val patient = PatientBuilder.faker().version(version).build();
    val tkCoverageInfo = GkvInsuranceCoverageInfo.TK;
    val coverage =
        KbvCoverageBuilder.insurance(tkCoverageInfo)
            .version(version)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET)
            .dmpKennzeichen(DmpKennzeichen.ASTHMA)
            .wop(Wop.BERLIN)
            .versichertenStatus(VersichertenStatus.PENSIONER)
            .versicherungsArt(PayorType.UK)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());

    // check the getters here because we do not yet have static files for coverages
    assertEquals(tkCoverageInfo.getIknr(), coverage.getIknr().getValue());
    assertTrue(coverage.getAlternativeIknr().isPresent());
    // alternative IKNR is always fixe by default for now!!
    assertEquals("121191241", coverage.getAlternativeIknr().get().getValue());
    assertEquals(tkCoverageInfo.getName(), coverage.getName());
    assertNotNull(coverage.getDescription());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build KBV Coverage with SKT PayorType with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithSKTPayorType(KbvItaForVersion version) {
    val patient = PatientBuilder.faker().version(version).build();
    val tkCoverageInfo = GkvInsuranceCoverageInfo.TK;
    val coverage =
        KbvCoverageBuilder.insurance(tkCoverageInfo)
            .version(version)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET)
            .dmpKennzeichen(DmpKennzeichen.ASTHMA)
            .wop(Wop.BERLIN)
            .versichertenStatus(VersichertenStatus.PENSIONER)
            .versicherungsArt(PayorType.SKT)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());

    // check the getters here because we do not yet have static files for coverages
    assertEquals(tkCoverageInfo.getIknr(), coverage.getIknr().getValue());
    assertFalse(coverage.getAlternativeIknr().isPresent());
    assertEquals(tkCoverageInfo.getName(), coverage.getName());
    assertNotNull(coverage.getDescription());
  }

  @ParameterizedTest(name = "[{index}] -> Build faker KBV Coverage with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFaker01(KbvItaForVersion version) {
    for (var i = 0; i < 2; i++) {
      val coverage = KbvCoverageBuilder.faker().version(version).build();
      val result = ValidatorUtil.encodeAndValidate(parser, coverage);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name = "[{index}] -> Build faker KBV Coverage for GKV/PKV/BG/BEI with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFaker02(KbvItaForVersion version) {
    val insuranceKinds =
        List.of(
            VersicherungsArtDeBasis.GKV,
            VersicherungsArtDeBasis.PKV,
            VersicherungsArtDeBasis.BG,
            VersicherungsArtDeBasis.BEI);

    insuranceKinds.forEach(
        ik -> {
          for (var i = 0; i < 2; i++) {
            val coverage = KbvCoverageBuilder.faker(ik).version(version).build();
            log.info(format("Validating Faker Coverage with ID {0}", coverage.getId()));
            val result = ValidatorUtil.encodeAndValidate(parser, coverage);
            assertTrue(result.isSuccessful());
          }
        });
  }
}
