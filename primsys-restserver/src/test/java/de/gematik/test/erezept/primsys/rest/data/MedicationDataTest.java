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
import lombok.val;
import org.junit.Before;
import org.junit.Test;

public class MedicationDataTest {

  private ObjectMapper mapper;

  @Before
  public void setup() {
    mapper = new ObjectMapper();
  }

  @Test
  public void roundTripSerialize() throws JsonProcessingException {
    val data = MedicationData.create();
    data.setNote("example note");
    assertTrue(mapper.canSerialize(MedicationData.class));
    assertNotNull(data);
    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  public void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val kbvBundle = KbvErpBundleBuilder.faker("X123456789", "04773414").build();
    val data = MedicationData.fromKbvBundle(kbvBundle);
    assertNotNull(data);
    assertEquals(kbvBundle.getMedication().getMedicationName(), data.getName());
    assertEquals(kbvBundle.getMedication().getPzn().get(0), data.getPzn());

    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }
}
