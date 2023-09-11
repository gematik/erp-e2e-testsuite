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

import static de.gematik.test.erezept.primsys.rest.data.PatientData.create;
import static org.junit.jupiter.api.Assertions.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.gematik.test.erezept.fhir.builder.kbv.KbvErpBundleBuilder;
import de.gematik.test.erezept.fhir.values.KVNR;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PatientDataTest {
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
  void shouldSerializeWithKvid() {
    val json = """
    {
      "kvid": "X1231230"
    }
    """;
    // avoid breaking changes
    //required because originally we've been using kvid and switched to kvnr
    assertDoesNotThrow(() -> mapper.readValue(json, PatientData.class));
  }

  @Test
  void roundTripSerializeFromKbvBundle() throws JsonProcessingException {
    val kbvBundle = KbvErpBundleBuilder.faker(KVNR.from("X123456789"), "04773414").build();
    val data = PatientData.fromKbvBundle(kbvBundle);
    assertNotNull(data);
    assertEquals("X123456789", data.getKvnr());
    assertEquals(kbvBundle.getPatient().getAddressFirstRep().getCity(), data.getCity());
    assertEquals(kbvBundle.getPatient().getAddressFirstRep().getPostalCode(), data.getPostal());
    assertEquals(kbvBundle.getPatient().getNameFirstRep().getFamily(), data.getLastName());
    assertEquals(
        kbvBundle.getPatient().getNameFirstRep().getGivenAsSingleString(), data.getFirstName());

    val json = mapper.writeValueAsString(data);
    assertNotNull(json);
  }

  @Test
  void generateRandomBaseData() {
    val data = new PatientData();
    assertNull(data.getKvnr(), "Patient must NOT get a faked KVNR");
    data.setKvnr("X1234567890");
    assertEquals("X1234567890", data.getKvnr());

    assertNotNull(data.getFirstName(), "Patient must get a faked FirstName");
    assertNotNull(data.getLastName(), "Patient must get a faked LastName");
    assertNotNull(data.getBirthDate(), "Patient must get a faked Birthdate");

    assertNotNull(data.getCity(), "Patient must get a faked City");
    assertNotNull(data.getPostal(), "Patient must get a faked Postal Code");
    assertNotNull(data.getStreet(), "Patient must get a faked Street");
  }

  @Test
  void creatPatientDataWithEmptyPatientData() {
    val emptyPd = new PatientData();
    emptyPd.fakeMissing();
    assertFalse(emptyPd.getKvnr().isEmpty());
    assertFalse(emptyPd.getFirstName().isEmpty());
    assertFalse(emptyPd.getLastName().isEmpty());
    assertFalse(emptyPd.getBirthDate().isEmpty());
    assertFalse(emptyPd.getCity().isEmpty());
    assertFalse(emptyPd.getPostal().isEmpty());
    assertFalse(emptyPd.getStreet().isEmpty());
  }

  @Test
  void creatPatientDataWithFilledPatientData() {
    val filledUpWithCreate = create();
    filledUpWithCreate.fakeMissing();
    assertFalse(filledUpWithCreate.getKvnr().isEmpty());
    assertFalse(filledUpWithCreate.getFirstName().isEmpty());
    assertFalse(filledUpWithCreate.getLastName().isEmpty());
    assertFalse(filledUpWithCreate.getBirthDate().isEmpty());
    assertFalse(filledUpWithCreate.getCity().isEmpty());
    assertFalse(filledUpWithCreate.getPostal().isEmpty());
    assertFalse(filledUpWithCreate.getStreet().isEmpty());
  }

  @Test
  void creatPatientDataWithNullParts() {
    val emptyPd = new PatientData();
    emptyPd.setKvnr(null);
    emptyPd.setFirstName(null);
    emptyPd.setLastName(null);
    emptyPd.setBirthDate(null);
    emptyPd.setCity(null);
    emptyPd.setPostal(null);
    emptyPd.setStreet(null);
    emptyPd.fakeMissing();
    assertFalse(emptyPd.getKvnr().isEmpty());
    assertFalse(emptyPd.getFirstName().isEmpty());
    assertFalse(emptyPd.getLastName().isEmpty());
    assertFalse(emptyPd.getBirthDate().isEmpty());
    assertFalse(emptyPd.getCity().isEmpty());
    assertFalse(emptyPd.getPostal().isEmpty());
    assertFalse(emptyPd.getStreet().isEmpty());
  }

  @Test
  void creatPatientDataWithEmptyParts() {
    val emptyPd = new PatientData();
    emptyPd.setKvnr("");
    emptyPd.setFirstName("");
    emptyPd.setLastName("");
    emptyPd.setBirthDate("");
    emptyPd.setCity("");
    emptyPd.setPostal("");
    emptyPd.setStreet("");
    emptyPd.fakeMissing();
    assertFalse(emptyPd.getKvnr().isEmpty());
    assertFalse(emptyPd.getFirstName().isEmpty());
    assertFalse(emptyPd.getLastName().isEmpty());
    assertFalse(emptyPd.getBirthDate().isEmpty());
    assertFalse(emptyPd.getCity().isEmpty());
    assertFalse(emptyPd.getPostal().isEmpty());
    assertFalse(emptyPd.getStreet().isEmpty());
  }
}
