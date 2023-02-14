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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ContextDataTest {

  static PrescriptionData prescriptionData;
  static PrescriptionData prescriptionData2;
  static AcceptData acceptData;
  static AcceptData acceptData2;
  static DispensedData dispensedData;
  static DispensedData dispensedData2;
  static ContextData contextData;

  @BeforeAll
  static void setup() {
    String oneToZero = "1234567890";

    contextData = new ContextData();

    prescriptionData = new PrescriptionData();
    prescriptionData.setAccessCode(oneToZero);
    prescriptionData.setPrescriptionId(oneToZero);
    prescriptionData.setTaskId(oneToZero);

    prescriptionData2 = new PrescriptionData();
    prescriptionData2.setAccessCode(oneToZero);
    prescriptionData2.setPrescriptionId(oneToZero);
    prescriptionData2.setTaskId("testID");

    acceptData = new AcceptData();
    acceptData.setTaskId(oneToZero);
    acceptData.setSecret(oneToZero);

    acceptData2 = new AcceptData();
    acceptData2.setTaskId("testID");
    acceptData2.setSecret("testID");

    dispensedData = new DispensedData();
    dispensedData.setDispensedDate(GemFaker.fakerBirthday());
    dispensedData2 = new DispensedData();
    dispensedData2.setDispensedDate(GemFaker.fakerBirthday());
  }

  @Test
  void addPrescription() {
    contextData.addPrescription(prescriptionData);
    assertTrue(contextData.getPrescriptions().contains(prescriptionData));
  }

  @Test
  void addMaxPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addPrescription(prescriptionData);
    }
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getPrescriptions().size());
  }

  @Test
  void addMoreThaMaxPrescriptions() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addPrescription(prescriptionData);
    }
    contextData.addPrescription(prescriptionData2);
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getPrescriptions().size());
    assertTrue(contextData.getPrescriptions().contains(prescriptionData2));
  }

  @Test
  void addAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData);
    assertTrue(contextData.getAcceptedPrescriptions().contains(acceptData));
  }

  @Test
  void addMaxAcceptedPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getAcceptedPrescriptions().size());
  }

  @Test
  void addMoreThaMaxAcceptedPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    contextData.addAcceptedPrescription(acceptData2);
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getAcceptedPrescriptions().size());
    assertTrue(contextData.getAcceptedPrescriptions().contains(acceptData2));
  }

  @Test
  void addDispensedMedications() {
    contextData.addDispensedMedications(dispensedData);
    assertTrue(contextData.getDispensedMedications().contains(dispensedData));
  }

  @Test
  void addMaxDispensedMedications() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addDispensedMedications(dispensedData);
    }
    assertTrue(contextData.getDispensedMedications().contains(dispensedData));
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getDispensedMedications().size());
  }

  @Test
  void addMoreThenMaxDispensedMedications() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addDispensedMedications(dispensedData);
    }
    contextData.addDispensedMedications(dispensedData2);
    assertTrue(contextData.getDispensedMedications().contains(dispensedData2));
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getDispensedMedications().size());
  }

  @Test
  void removeAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData);
    assertTrue(contextData.removeAcceptedPrescription(acceptData.getTaskId()));
  }

  @Test
  void shouldNotRemoveAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData2);
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    assertFalse(contextData.removeAcceptedPrescription(acceptData2.getTaskId()));
  }

  @Test
  void removeLastAcceptedPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    contextData.addAcceptedPrescription(acceptData2);
    assertTrue(contextData.removeAcceptedPrescription(acceptData2.getTaskId()));
  }

  @Test
  void shouldNotRemoveFirstAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData2);
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    assertFalse(contextData.removeAcceptedPrescription(acceptData2.getTaskId()));
    contextData.getAcceptedPrescriptions();
  }
}
