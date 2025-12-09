/*
 * Copyright 2025 gematik GmbH
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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.primsys.PrimSysRestFactory;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.*;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
import de.gematik.test.erezept.primsys.data.valuesets.PatientInsuranceTypeDto;
import de.gematik.test.erezept.primsys.rest.params.PrescriptionFilterParams;
import jakarta.ws.rs.WebApplicationException;
import lombok.val;
import org.junit.jupiter.api.Test;

class ActorContextTest extends TestWithActorContext {

  @Test
  void shouldInstantiateWithConfig() {
    val ctx = ActorContext.getInstance();
    assertNotNull(ctx);
    assertFalse(ctx.getActors().isEmpty());
    assertDoesNotThrow(ctx::shutdown);
  }

  @Test
  void shouldAddPrescription() {
    val ctx = ActorContext.getInstance();
    val prescriptionId = GemFaker.fakerPrescriptionId();
    val prescriptionData = new PrescriptionDto();
    prescriptionData.setPrescriptionId(prescriptionId);
    prescriptionData.setTaskId(prescriptionId);
    prescriptionData.setPatient(PatientDto.withKvnr("X110407071").build());
    val size1 = ctx.getPrescriptions().size();
    ctx.addPrescription(prescriptionData);
    assertTrue(size1 < ctx.getPrescriptions().size());
    assertTrue(ctx.getPrescription(prescriptionId).isPresent());
    assertFalse(ctx.getPrescriptions(PrescriptionFilterParams.with("X110407071")).isEmpty());
  }

  @Test
  void shouldAddAndRemoveAcceptedPrescriptions() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ctx = ActorContext.getInstance();
    val acceptData = new AcceptedPrescriptionDto();
    acceptData.setPrescriptionId(prescriptionId);
    acceptData.setForKvnr("X110407071");
    ctx.addAcceptedPrescription(acceptData);
    assertFalse(ctx.getAcceptedPrescriptions().isEmpty());
    assertFalse(
        ctx.getAcceptedPrescriptions(PrescriptionFilterParams.with("X110407071")).isEmpty());
    assertTrue(ctx.getAcceptedPrescription(prescriptionId).isPresent());

    assertTrue(ctx.removeAcceptedPrescription(acceptData));
    assertFalse(ctx.getAcceptedPrescription(prescriptionId).isPresent());
  }

  @Test
  void shouldAddDispensedPrescriptions() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ctx = ActorContext.getInstance();
    val dispensedData = new DispensedMedicationDto();
    dispensedData.setPrescriptionId(prescriptionId);
    dispensedData.setAcceptData(
        AcceptedPrescriptionDto.withPrescriptionId("123")
            .forKvnr("X110407071", PatientInsuranceTypeDto.PKV)
            .andMedication(PznMedicationDto.medicine("123", "Test-Pillen").asPrescribed()));
    ctx.addDispensedMedications(dispensedData);
    assertTrue(ctx.getDispensedMedication(prescriptionId).isPresent());
    assertFalse(ctx.getDispensedMedications(PrescriptionFilterParams.empty()).isEmpty());
  }

  @Test
  void shouldFilterDispensedPrescriptionsByKvnr() {
    val ctx = ActorContext.getInstance();
    val dispenseData = new DispensedMedicationDto();
    dispenseData.setPrescriptionId(PrescriptionId.random().getValue());
    dispenseData.setAcceptData(
        AcceptedPrescriptionDto.withPrescriptionId("123")
            .forKvnr("X110407071", PatientInsuranceTypeDto.GKV)
            .andMedication(PznMedicationDto.medicine("123", "Test-Pillen").asPrescribed()));
    ctx.addDispensedMedications(dispenseData);
    val dispensed = ctx.getDispensedMedications(PrescriptionFilterParams.with("X110407071"));
    assertFalse(dispensed.isEmpty());
  }

  @Test
  void shouldRemovePrescription() {
    val acceptData = new AcceptedPrescriptionDto();
    acceptData.setPrescriptionId(PrescriptionId.random().getValue());
    acceptData.setAccessCode(AccessCode.random().getValue());
    acceptData.setSecret("dc2c283afae58da2e5249faac31644c8436f25aab0f3758faf736a14c2cb1d93");

    val ctx = ActorContext.getInstance();
    ctx.addAcceptedPrescription(acceptData);
    assertTrue(ctx.removeAcceptedPrescription(acceptData));
  }

  @Test
  void shouldBeEmptyOnUnknownDoctorId() {
    val ctx = ActorContext.getInstance();
    val optDoc = ctx.getDoctor("123123123");
    assertTrue(optDoc.isEmpty());
  }

  @Test
  void shouldGetKnownDoctor() {
    val ctx = ActorContext.getInstance();
    val doctor = ctx.getDoctors().get(0);
    val doctor2 = assertDoesNotThrow(() -> ctx.getDoctorOrThrowNotFound(doctor.getIdentifier()));
    assertEquals(doctor, doctor2);
  }

  @Test
  void shouldThrow404OnUnknownDoctor() {
    val ctx = ActorContext.getInstance();
    assertThrows(WebApplicationException.class, () -> ctx.getDoctorOrThrowNotFound("123123"));
  }

  @Test
  void shouldGetKnownKtr() {
    val ctx = ActorContext.getInstance();
    val ktr = ctx.getHealthInsurances().get(0);
    val ktr2 = assertDoesNotThrow(() -> ctx.getHealthInsuranceOrThrowNotFound(ktr.getIdentifier()));
    assertEquals(ktr, ktr2);
  }

  @Test
  void shouldThrow404OnUnknownKtr() {
    val ctx = ActorContext.getInstance();
    assertThrows(
        WebApplicationException.class, () -> ctx.getHealthInsuranceOrThrowNotFound("123123"));
  }

  @Test
  void shouldGetKnownPharmacy() {
    val ctx = ActorContext.getInstance();
    val pharmacy = ctx.getPharmacies().get(0);
    val pharmacy2 =
        assertDoesNotThrow(() -> ctx.getPharmacyOrThrowNotFound(pharmacy.getIdentifier()));
    assertEquals(pharmacy, pharmacy2);
  }

  @Test
  void shouldThrow404OnUnknownPharmacy() {
    val ctx = ActorContext.getInstance();
    assertThrows(WebApplicationException.class, () -> ctx.getPharmacyOrThrowNotFound("123123"));
  }

  @Test
  void shouldBeEmptyOnUnknownPharmacyId() {
    val ctx = ActorContext.getInstance();
    val optDoc = ctx.getPharmacy("123");
    assertTrue(optDoc.isEmpty());
  }

  @Test
  void shouldGetActorSummaries() {
    val ctx = ActorContext.getInstance();
    val summaries = ctx.getActorsSummary();
    val doctors = ctx.getActorsSummary(ActorType.DOCTOR);
    val pharmacies = ctx.getActorsSummary(ActorType.PHARMACY);
    val ktrs = ctx.getActorsSummary(ActorType.HEALTH_INSURANCE);

    assertEquals(11, summaries.size());
    assertEquals(3, doctors.size());
    assertEquals(7, pharmacies.size());
    assertEquals(1, ktrs.size());
  }

  @Test
  void shouldBeEmptyOnUnknownDispensedPrescriptionId() {
    val prescriptionId = PrescriptionId.random().getValue();
    val ctx = ActorContext.getInstance();
    val dispensed = ctx.getDispensedMedication(prescriptionId);
    assertTrue(dispensed.isEmpty());
  }

  @Test
  void shouldNotInitializeTwice() {
    val ctx = ActorContext.getInstance();
    ActorContext.init(mock(PrimSysRestFactory.class));
    assertEquals(ctx, ActorContext.getInstance());
  }
}
