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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.primsys.model.actor.Doctor;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DoctorDataTest {

  private ObjectMapper mapper;

  @BeforeEach
  void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val doc = mock(Doctor.class);
    val tiData = new TelematikData();
    tiData.setFachdienst("http://localhost");
    tiData.setTsl("tsl");
    tiData.setDiscoveryDocument("discovery");

    val baseData = new DoctorData();
    baseData.setType("Doctor");
    baseData.setId("123");
    baseData.setTi(tiData);

    when(doc.getBaseData()).thenReturn(baseData);

    val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
    val data = DoctorData.fromKbvBundle(doc, kbvBundle);
    assertNotNull(data);
    assertEquals(kbvBundle.getPractitioner().getFullName(), data.getName());

    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }
}
