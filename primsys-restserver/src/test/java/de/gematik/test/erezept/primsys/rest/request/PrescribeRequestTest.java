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

package de.gematik.test.erezept.primsys.rest.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.primsys.rest.data.CoverageData;
import de.gematik.test.erezept.primsys.rest.data.MedicationData;
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class PrescribeRequestTest {

  private ObjectMapper mapper;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  public void createRandomCoverage() throws JsonProcessingException {
    val request = new PrescribeRequest();
    val requestCoverage = request.getCoverage();
    assertNotNull(requestCoverage);
    val json = mapper.writeValueAsString(requestCoverage);
    assertNotNull(json);
  }

  @Test
  public void dontCreateRandomCoverage() throws JsonProcessingException {
    val request = new PrescribeRequest();
    val coverage = CoverageData.create();
    request.setCoverage(coverage);
    val requestCoverage = request.getCoverage();
    assertNotNull(requestCoverage);
    assertEquals(coverage, requestCoverage);
    val json = mapper.writeValueAsString(requestCoverage);
    assertNotNull(json);
  }

  @Test
  public void createRandomMedication() throws JsonProcessingException {
    val request = new PrescribeRequest();
    val requestMedication = request.getMedication();
    assertNotNull(requestMedication);
    val json = mapper.writeValueAsString(requestMedication);
    assertNotNull(json);
  }

  @Test
  public void dontCreateRandomMedication() throws JsonProcessingException {
    val request = new PrescribeRequest();
    val medication = MedicationData.create();
    request.setMedication(medication);
    val requestMedication = request.getMedication();
    assertNotNull(requestMedication);
    assertEquals(medication, requestMedication);
    val json = mapper.writeValueAsString(requestMedication);
    assertNotNull(json);
  }
}
