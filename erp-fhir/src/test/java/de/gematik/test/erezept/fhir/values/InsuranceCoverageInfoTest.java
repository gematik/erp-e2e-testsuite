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

package de.gematik.test.erezept.fhir.values;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import java.util.List;
import java.util.stream.Stream;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.MethodSource;

class InsuranceCoverageInfoTest {

  static Stream<Arguments> coverageDataImplementors() {
    return Stream.of(
        arguments(VersicherungsArtDeBasis.PKV, List.of(PkvInsuranceCoverageInfo.values())),
        arguments(VersicherungsArtDeBasis.GKV, List.of(GkvInsuranceCoverageInfo.values())),
        arguments(VersicherungsArtDeBasis.BG, List.of(BGInsuranceCoverageInfo.values())));
  }

  @Test
  void shouldProvidePkvName() {
    assertFalse(PkvInsuranceCoverageInfo.BARMENIA.getName().isEmpty());
  }

  @Test
  void shouldProvideContact() {
    assertFalse(PkvInsuranceCoverageInfo.ALTE_OLDENBURGER.getContact().isEmpty());
  }

  @Test
  void shouldProvideGkvName() {
    val gkvCoverageData = GkvInsuranceCoverageInfo.ACTIMONDA;
    assertFalse(gkvCoverageData.getName().isEmpty());
  }

  @Test
  void getIknrGKVIknr() {
    val testdata = GkvInsuranceCoverageInfo.AOK_BAYERN.getIknr();
    assertEquals("108310400", testdata);
  }

  @ParameterizedTest(name = "{0} Name must not exceed max length of 45")
  @MethodSource("coverageDataImplementors")
  void shouldNotExceedMaxNameLength(VersicherungsArtDeBasis type, List<InsuranceCoverageInfo> data) {
    data.forEach(
        cid ->
            assertTrue(
                cid.getName().length() <= 45,
                format(
                    "{0} Insurance {1} ({2}) exceeds max length",
                    type.getCode(), cid.getName(), cid.getIknr())));
  }

  @ParameterizedTest(name = "{0} IKNRs must have length of 9 digits")
  @MethodSource("coverageDataImplementors")
  void shouldMatchIknrLength(VersicherungsArtDeBasis type, List<InsuranceCoverageInfo> data) {
    data.forEach(
        icd ->
            assertEquals(
                9,
                icd.getIknr().length(),
                format(
                    "{0} Insurance {1} has more than 9 digits Nr: {2}",
                    type.getCode(), icd.getName(), icd.getIknr())));
  }

  @ParameterizedTest(name = "Random Insurance Coverage Information for {0}")
  @EnumSource(value = VersicherungsArtDeBasis.class)
  void shouldGetRandomFor(VersicherungsArtDeBasis insuranceKind) {
    val data = InsuranceCoverageInfo.randomFor(insuranceKind);
    assertNotNull(data.getIknr());
    assertNotNull(data.getName());
  }
  
  @Test
  void shouldGetGkvByIknr() {
    val element = GkvInsuranceCoverageInfo.getByIknr("108018007");
    assertTrue(element.isPresent());
    element.ifPresent(cov -> assertEquals(GkvInsuranceCoverageInfo.AOK_BAD_WUERT, cov));
  }

  @Test
  void shouldGetPkvByIknr() {
    val element = PkvInsuranceCoverageInfo.getByIknr("168140346");
    assertTrue(element.isPresent());
    element.ifPresent(cov -> assertEquals(PkvInsuranceCoverageInfo.ALLIANZ, cov));
  }

  @Test
  void shouldGetBGByIknr() {
    val element = BGInsuranceCoverageInfo.getByIknr("120390887");
    assertTrue(element.isPresent());
    element.ifPresent(cov -> assertEquals(BGInsuranceCoverageInfo.BG_BAU, cov));
  }

  @Test
  void shouldFindByAnyInsurance() {
    val elementGkv = InsuranceCoverageInfo.getByIknr("108018007");
    assertTrue(elementGkv.isPresent());
    elementGkv.ifPresent(cov -> assertEquals(GkvInsuranceCoverageInfo.AOK_BAD_WUERT, cov));

    val elementPkv = InsuranceCoverageInfo.getByIknr("168140346");
    assertTrue(elementPkv.isPresent());
    elementPkv.ifPresent(cov -> assertEquals(PkvInsuranceCoverageInfo.ALLIANZ, cov));

    val elementBg = InsuranceCoverageInfo.getByIknr("120390887");
    assertTrue(elementBg.isPresent());
    elementBg.ifPresent(cov -> assertEquals(BGInsuranceCoverageInfo.BG_BAU, cov));
  }
}
