/*
 * Copyright (c) 2022 gematik GmbH
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

import static org.junit.Assert.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.valuesets.PersonGroup;
import de.gematik.test.erezept.fhir.valuesets.VersichertenStatus;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.fhir.valuesets.Wop;
import jakarta.ws.rs.WebApplicationException;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class CoverageDataTest {

  private ObjectMapper mapper;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  public void roundTripSerialize() throws JsonProcessingException {
    val data = CoverageData.create();
    assertNotNull(data);
    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  public void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
    val data = CoverageData.fromKbvBundle(kbvBundle);
    assertNotNull(data);
    assertEquals(kbvBundle.getCoverageIknr(), data.getIknr());
    assertEquals(kbvBundle.getCoverageName(), data.getInsuranceName());

    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  public void mustThrowOnInvalidWop() {
    val coverage = new CoverageData();
    coverage.setWop("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumWop);
  }

  @Test
  public void mustNotThrowOnValidWop() {
    val coverage = new CoverageData();
    val wop = Wop.BERLIN;
    coverage.setWop(wop.getCode());
    assertEquals(wop, coverage.getEnumWop());
  }

  @Test
  public void mustThrowOnInvalidPersonGroup() {
    val coverage = new CoverageData();
    coverage.setPersonGroup("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumPersonGroup);
  }

  @Test
  public void mustNotThrowOnValidPersonGroup() {
    val coverage = new CoverageData();
    val pg = PersonGroup.SOZ;
    coverage.setPersonGroup(pg.getCode());
    assertEquals(pg, coverage.getEnumPersonGroup());
  }

  @Test
  public void mustThrowOnInvalidInsuranceKind() {
    val coverage = new CoverageData();
    coverage.setInsuranceKind("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumInsuranceKind);
  }

  @Test
  public void mustNotThrowOnValidInsuranceKind() {
    val coverage = new CoverageData();
    val ik = VersicherungsArtDeBasis.GKV;
    coverage.setInsuranceKind(ik.getCode());
    assertEquals(ik, coverage.getEnumInsuranceKind());
  }

  @Test
  public void mustThrowOnInvalidInsuranceState() {
    val coverage = new CoverageData();
    coverage.setInsuranceState("invalid");
    assertThrows(WebApplicationException.class, coverage::getEnumInsuranceState);
  }

  @Test
  public void mustNotThrowOnValidInsuranceState() {
    val coverage = new CoverageData();
    val ik = VersichertenStatus.PENSIONER;
    coverage.setInsuranceState(ik.getCode());
    assertEquals(ik, coverage.getEnumInsuranceState());
  }
}
