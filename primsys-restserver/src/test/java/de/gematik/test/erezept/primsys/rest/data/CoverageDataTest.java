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

package de.gematik.test.erezept.primsys.rest.data;

import static de.gematik.test.erezept.fhir.builder.GemFaker.fakerIknr;
import static de.gematik.test.erezept.primsys.rest.data.CoverageData.create;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import jakarta.ws.rs.WebApplicationException;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class CoverageDataTest {

  static ObjectMapper mapper;

  @BeforeAll
  static void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  void roundTripSerialize() throws JsonProcessingException {
    val data = create();
    assertNotNull(data);
    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
    val data = CoverageData.fromKbvBundle(kbvBundle);
    assertNotNull(data);
    assertEquals(kbvBundle.getCoverageIknr(), data.getIknr());
    assertEquals(kbvBundle.getCoverageName(), data.getInsuranceName());

    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  void mustThrowOnInvalidWop() {
    val coverage = new CoverageData();
    coverage.setWop("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumWop);
  }

  @Test
  void mustNotThrowOnValidWop() {
    val coverage = new CoverageData();
    val wop = Wop.BERLIN;
    coverage.setWop(wop.getCode());
    assertEquals(wop, coverage.getEnumWop());
  }

  @Test
  void mustThrowOnInvalidPersonGroup() {
    val coverage = new CoverageData();
    coverage.setPersonGroup("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumPersonGroup);
  }

  @Test
  void mustNotThrowOnValidPersonGroup() {
    val coverage = new CoverageData();
    val pg = PersonGroup.SOZ;
    coverage.setPersonGroup(pg.getCode());
    assertEquals(pg, coverage.getEnumPersonGroup());
  }

  @Test
  void mustThrowOnInvalidInsuranceKind() {
    val coverage = new CoverageData();
    coverage.setInsuranceKind("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumInsuranceKind);
  }

  @Test
  void mustNotThrowOnValidInsuranceKind() {
    val coverage = new CoverageData();
    val ik = VersicherungsArtDeBasis.GKV;
    coverage.setInsuranceKind(ik.getCode());
    assertEquals(ik, coverage.getEnumInsuranceKind());
  }

  @Test
  void mustThrowOnInvalidInsuranceState() {
    val coverage = new CoverageData();
    coverage.setInsuranceState("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumInsuranceState);
  }

  @Test
  void mustNotThrowOnValidInsuranceState() {
    val coverage = new CoverageData();
    val ik = VersichertenStatus.PENSIONER;
    coverage.setInsuranceState(ik.getCode());
    assertEquals(ik, coverage.getEnumInsuranceState());
  }

  @Test
  void createCorrectWithGivenPartOfCoverageData1() {
    val coverage = new CoverageData();
    val ik = VersichertenStatus.PENSIONER;
    coverage.setInsuranceState(ik.getCode());
    val newCoverage = coverage.fakeMissing();
    assertNotNull(newCoverage.getIknr());
  }

  @Test
  void createCorrectWithGivenPartOfCoverageData2() {
    val coverage = new CoverageData();
    coverage.setIknr(fakerIknr());
    val newCoverage = coverage.fakeMissing();
    assertNotNull(newCoverage.getPayorType());
    assertNotNull(newCoverage.getInsuranceKind());
    assertNotNull(newCoverage.getInsuranceName());
    assertNotNull(newCoverage.getWop());
    assertNotNull(newCoverage.getInsuranceState());
    assertNotNull(newCoverage.getPersonGroup());
    assertNotNull(newCoverage.getIknr());
  }

  @Test
  void createCovDataWithNullEntries() {
    val emptyCd = new CoverageData();
    emptyCd.setIknr(null);
    emptyCd.setInsuranceName(null);
    emptyCd.setWop(null);
    emptyCd.setInsuranceState(null);
    emptyCd.setInsuranceKind(null);
    emptyCd.setPayorType(null);
    emptyCd.setPersonGroup(null);
    val filledCoverage = emptyCd.fakeMissing();
    assertNotNull(filledCoverage.getPayorType());
    assertNotNull(filledCoverage.getInsuranceKind());
    assertNotNull(filledCoverage.getWop());
    assertNotNull(filledCoverage.getInsuranceState());
    assertNotNull(filledCoverage.getPersonGroup());
    assertNotNull(filledCoverage.getInsuranceName());
    assertNotNull(filledCoverage.getIknr());
  }

  @Test
  void createCovDataWithEmptyEntries() {
    val emptyCd = new CoverageData();
    emptyCd.setIknr("");
    emptyCd.setInsuranceName("");
    emptyCd.setWop("");
    emptyCd.setInsuranceState("");
    emptyCd.setInsuranceKind("");
    emptyCd.setPayorType("");
    emptyCd.setPersonGroup("");
    val filledCoverage = emptyCd.fakeMissing();
    assertFalse(filledCoverage.getPayorType().isEmpty());
    assertFalse(filledCoverage.getInsuranceKind().isEmpty());
    assertFalse(filledCoverage.getWop().isEmpty());
    assertFalse(filledCoverage.getInsuranceState().isEmpty());
    assertFalse(filledCoverage.getPersonGroup().isEmpty());
    assertFalse(filledCoverage.getInsuranceName().isEmpty());
    assertFalse(filledCoverage.getIknr().isEmpty());
  }
}
