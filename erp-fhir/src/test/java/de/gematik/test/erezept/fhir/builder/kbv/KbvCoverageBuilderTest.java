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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.bbriccs.fhir.builder.exceptions.BuilderException;
import de.gematik.bbriccs.fhir.de.value.IKNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.test.erezept.fhir.profiles.version.KbvItaForVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.testutil.ValidatorUtil;
import de.gematik.test.erezept.fhir.values.GkvInsuranceCoverageInfo;
import de.gematik.test.erezept.fhir.valuesets.DmpKennzeichen;
import de.gematik.test.erezept.fhir.valuesets.PayorType;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import java.util.List;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class KbvCoverageBuilderTest extends ErpFhirParsingTest {

  @ParameterizedTest(name = "[{index}] -> Build KBV GKV Coverage with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFixedValues(KbvItaForVersion version) {
    val patient =
        KbvPatientFaker.builder()
            .withVersion(version)
            .withInsuranceType(InsuranceTypeDe.GKV)
            .fake();
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
    assertEquals(patient.getInsuranceType(), coverage.getInsuranceKind());
    assertEquals("101575519", coverage.getIknrOrThrow().getValue());
    assertEquals("Techniker Krankenkasse", coverage.getName());
    assertNotNull(coverage.getDescription());
  }

  @ParameterizedTest(
      name = "[{index}] -> Build KBV Coverage with UK PayorType with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithUKPayorType(KbvItaForVersion version) {
    val patient = KbvPatientFaker.builder().withVersion(version).fake();
    val tkCoverageInfo = GkvInsuranceCoverageInfo.TK;
    val coverage =
        KbvCoverageBuilder.insurance(tkCoverageInfo)
            .version(version)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET)
            .dmpKennzeichen(DmpKennzeichen.ASTHMA)
            .wop(Wop.BERLIN)
            .versichertenStatus(VersichertenStatus.PENSIONER)
            .insuranceType(PayorType.UK)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());

    // check the getters here because we do not yet have static files for coverages
    assertEquals(tkCoverageInfo.getIknr(), coverage.getIknrOrThrow().getValue());
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
    val patient = KbvPatientFaker.builder().withVersion(version).fake();
    val tkCoverageInfo = GkvInsuranceCoverageInfo.TK;
    val coverage =
        KbvCoverageBuilder.insurance(tkCoverageInfo)
            .version(version)
            .beneficiary(patient)
            .personGroup(PersonGroup.NOT_SET)
            .dmpKennzeichen(DmpKennzeichen.ASTHMA)
            .wop(Wop.BERLIN)
            .versichertenStatus(VersichertenStatus.PENSIONER)
            .insuranceType(PayorType.SKT)
            .build();
    val result = ValidatorUtil.encodeAndValidate(parser, coverage);
    assertTrue(result.isSuccessful());

    // check the getters here because we do not yet have static files for coverages
    assertEquals(tkCoverageInfo.getIknr(), coverage.getIknrOrThrow().getValue());
    assertFalse(coverage.getAlternativeIknr().isPresent());
    assertEquals(tkCoverageInfo.getName(), coverage.getName());
    assertNotNull(coverage.getDescription());
  }

  @ParameterizedTest(name = "[{index}] -> Build faker KBV Coverage with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFaker01(KbvItaForVersion version) {
    for (var i = 0; i < 2; i++) {
      val coverage = KbvCoverageFaker.builder().withVersion(version).fake();
      val result = ValidatorUtil.encodeAndValidate(parser, coverage);
      assertTrue(result.isSuccessful());
    }
  }

  @ParameterizedTest(
      name = "[{index}] -> Build faker KBV Coverage for GKV/PKV/BG/BEI with KbvItaForVersion {0}")
  @MethodSource("de.gematik.test.erezept.fhir.testutil.VersionArgumentProvider#kbvItaForVersions")
  void buildCoverageWithFaker02(KbvItaForVersion version) {
    val insuranceKinds = List.of(InsuranceTypeDe.GKV, InsuranceTypeDe.PKV, InsuranceTypeDe.BG);

    insuranceKinds.forEach(
        ik -> {
          for (var i = 0; i < 2; i++) {
            val coverage =
                KbvCoverageFaker.builder().withInsuranceType(ik).withVersion(version).fake();
            val result = ValidatorUtil.encodeAndValidate(parser, coverage);
            assertTrue(result.isSuccessful());
          }
        });
  }

  @Test
  void shouldNotAllowArgeIknrs() {
    val argeIknr = IKNR.asArgeIknr("123123123");
    assertThrows(
        BuilderException.class, () -> KbvCoverageBuilder.insurance(argeIknr, "Insurance Name"));
  }
}
