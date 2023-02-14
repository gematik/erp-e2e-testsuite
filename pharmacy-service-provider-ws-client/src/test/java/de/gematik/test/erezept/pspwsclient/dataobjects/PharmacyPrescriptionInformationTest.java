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

package de.gematik.test.erezept.pspwsclient.dataobjects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class PharmacyPrescriptionInformationTest {

  static PharmacyPrescriptionInformation pharmacyPrescriptionInformation =
      new PharmacyPrescriptionInformation();

  @BeforeAll
  static void setup() {

    pharmacyPrescriptionInformation.setVersion("2");
    pharmacyPrescriptionInformation.setSupplyOptionsType("delivery");
    pharmacyPrescriptionInformation.setName("Dr. Maximilian von Muster");
    pharmacyPrescriptionInformation.setAddress(
        new String[] {"Bundesallee", "312", "12345", "Berlin"});
    pharmacyPrescriptionInformation.setHint("Bitte im Morsecode klingeln: -.-.");
    pharmacyPrescriptionInformation.setText("123456");
    pharmacyPrescriptionInformation.setPhone("004916094858168");
    pharmacyPrescriptionInformation.setMail("max@musterfrau.de");
    pharmacyPrescriptionInformation.setTransaction("ee63e415-9a99-4051-ab07-257632faf985");
    pharmacyPrescriptionInformation.setTaskID("160.123.456.789.123.58");
    pharmacyPrescriptionInformation.setAccessCode(
        "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea");
  }

  @Test
  void fromRawStringGetAccessCode() {
    assertEquals(
        "777bea0e13cc9c42ceec14aec3ddee2263325dc2c6c699db115f58fe423607ea",
        pharmacyPrescriptionInformation.getAccessCode());
  }

  @Test
  void fromRawStringGetSupplyOption() {
    assertEquals("delivery", pharmacyPrescriptionInformation.getSupplyOptionsType());
  }

  @Test
  void failToGetFromRawStringGetWrongInfo() {
    assertNotEquals("432", pharmacyPrescriptionInformation.getAddress()[2]);
  }

  @Test
  void pharmacyPrescriptionShouldThrowExeption() {
    assertThrows(
        MismatchedInputException.class,
        () -> {
          pharmacyPrescriptionInformation.fromRawString(" ");
        });
  }

  @SneakyThrows
  @Test
  void fromRawStringShouldResponseCorrect() {
    val objectMapper = new ObjectMapper();
    val json = objectMapper.writeValueAsString(pharmacyPrescriptionInformation);
    val pPI = PharmacyPrescriptionInformation.fromRawString(json);
    assertEquals(pharmacyPrescriptionInformation, pPI);
  }
}
