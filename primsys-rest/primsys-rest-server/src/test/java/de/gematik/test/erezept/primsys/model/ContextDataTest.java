/*
 * Copyright 2024 gematik GmbH
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

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PatientDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ContextDataTest {

  static PrescriptionDto prescriptionData;
  static PrescriptionDto prescriptionData2;
  static AcceptedPrescriptionDto acceptData;
  static AcceptedPrescriptionDto acceptData2;
  static DispensedMedicationDto dispensedData;
  static DispensedMedicationDto dispensedData2;
  static ContextData contextData;

  @BeforeAll
  static void setup() {
    val oneToZero = "1234567890";

    contextData = new ContextData();

    prescriptionData =
        PrescriptionDto.builder()
            .prescriptionId(oneToZero)
            .accessCode(oneToZero)
            .taskId(oneToZero)
            .patient(PatientDto.withKvnr("X110407071").build())
            .build();

    prescriptionData2 =
        PrescriptionDto.builder()
            .prescriptionId(oneToZero)
            .accessCode(oneToZero)
            .taskId("testID")
            .patient(PatientDto.withKvnr("X110407071").build())
            .build();

    acceptData = new AcceptedPrescriptionDto();
    acceptData.setForKvnr("X110407071");
    acceptData.setPrescriptionId(oneToZero);
    acceptData.setSecret(oneToZero);

    acceptData2 = new AcceptedPrescriptionDto();
    acceptData2.setForKvnr("X110407071");
    acceptData2.setPrescriptionId("testID");
    acceptData2.setSecret("testID");

    dispensedData = new DispensedMedicationDto();
    dispensedData.setAcceptData(acceptData);
    dispensedData.setDispensedDate(GemFaker.fakerBirthday());
    dispensedData2 = new DispensedMedicationDto();
    dispensedData2.setAcceptData(acceptData2);
    dispensedData2.setDispensedDate(GemFaker.fakerBirthday());
  }

  @Test
  void addPrescription() {
    contextData.addPrescription(prescriptionData);
    assertTrue(contextData.getReadyPrescriptions().contains(prescriptionData));
  }

  @Test
  void addMaxPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addPrescription(prescriptionData);
    }
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getReadyPrescriptions().size());
  }

  @Test
  void addMoreThaMaxPrescriptions() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addPrescription(prescriptionData);
    }
    contextData.addPrescription(prescriptionData2);
    assertEquals(ContextData.MAX_QUEUE_LENGTH, contextData.getReadyPrescriptions().size());
    assertTrue(contextData.getReadyPrescriptions().contains(prescriptionData2));
  }

  @Test
  void addAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData);
    assertTrue(contextData.getAcceptedPrescriptions().contains(acceptData));
  }

  @Test
  void shouldFilterReadyPrescriptionsByKvnr() {
    contextData.addPrescription(prescriptionData);
    val ready = contextData.getReadyPrescriptionsByKvnr(prescriptionData.getPatient().getKvnr());
    assertFalse(ready.isEmpty());
  }

  @Test
  void shouldFilterAcceptedPrescriptionsByKvnr() {
    contextData.addAcceptedPrescription(acceptData);
    val accepted = contextData.getAcceptedPrescriptionsByKvnr(acceptData.getForKvnr());
    assertFalse(accepted.isEmpty());
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
  void shouldFilterDispensedPrescriptionsByKvnr() {
    contextData.addDispensedMedications(dispensedData);
    val dispensed =
        contextData.getDispensedPrescriptionsByKvnr(dispensedData.getAcceptData().getForKvnr());
    assertFalse(dispensed.isEmpty());
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
    assertTrue(contextData.removeAcceptedPrescription(acceptData.getPrescriptionId()));
  }

  @Test
  void shouldNotRemoveAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData2);
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    assertFalse(contextData.removeAcceptedPrescription(acceptData2.getPrescriptionId()));
  }

  @Test
  void removeLastAcceptedPrescription() {
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    contextData.addAcceptedPrescription(acceptData2);
    assertTrue(contextData.removeAcceptedPrescription(acceptData2.getPrescriptionId()));
  }

  @Test
  void shouldNotRemoveFirstAcceptedPrescription() {
    contextData.addAcceptedPrescription(acceptData2);
    for (int i = 0; i < ContextData.MAX_QUEUE_LENGTH; i++) {
      contextData.addAcceptedPrescription(acceptData);
    }
    assertFalse(contextData.removeAcceptedPrescription(acceptData2.getPrescriptionId()));
  }
}
