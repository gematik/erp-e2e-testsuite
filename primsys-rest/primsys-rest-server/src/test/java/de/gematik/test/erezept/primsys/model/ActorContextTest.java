/*
 * Copyright 2023 gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import de.gematik.test.erezept.primsys.PrimSysRestFactory;
import de.gematik.test.erezept.primsys.TestWithActorContext;
import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import de.gematik.test.erezept.primsys.data.actors.ActorType;
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
  void shouldAddAndRemovePrescriptions() {
    val ctx = ActorContext.getInstance();
    val prescriptionData = new PrescriptionDto();
    ctx.addPrescription(prescriptionData);
    assertEquals(1, ctx.getPrescriptions().size());
  }

  @Test
  void shouldAddAndRemoveAcceptedPrescriptions() {
    val ctx = ActorContext.getInstance();
    val acceptData = new AcceptedPrescriptionDto();
    acceptData.setPrescriptionId("123");
    ctx.addAcceptedPrescription(acceptData);
    assertEquals(1, ctx.getAcceptedPrescriptions().size());

    assertTrue(ctx.removeAcceptedPrescription(acceptData));
    assertEquals(0, ctx.getAcceptedPrescriptions().size());
  }

  @Test
  void shouldAddAndRemoveDispensedPrescriptions() {
    val ctx = ActorContext.getInstance();
    val dispensedData = new DispensedMedicationDto();
    ctx.addDispensedMedications(dispensedData);
    assertEquals(1, ctx.getDispensedMedications().size());
  }

  @Test
  void shouldRemovePrescription() {
    val acceptData = new AcceptedPrescriptionDto();
    acceptData.setPrescriptionId("160.000.166.678.325.82");
    acceptData.setAccessCode("133ff36cbb92784b1c372e1166f92290d83b98596a37ef133ec1fbae500fd1bf");
    acceptData.setSecret("dc2c283afae58da2e5249faac31644c8436f25aab0f3758faf736a14c2cb1d93");

    val ctx = ActorContext.getInstance();
    ctx.addAcceptedPrescription(acceptData);
    assertTrue(ctx.removeAcceptedPrescription(acceptData));
  }

  @Test
  void shouldBeEmptyOnUnknownDoctorId() {
    val ctx = ActorContext.getInstance();
    val optDoc = ctx.getDoctor("123");
    assertTrue(optDoc.isEmpty());
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

    assertEquals(9, summaries.size());
    assertEquals(3, doctors.size());
    assertEquals(6, pharmacies.size());
  }

  @Test
  void shouldBeEmptyOnUnknownDispensedPrescriptionId() {
    val ctx = ActorContext.getInstance();
    val dispensed = ctx.getDispensedMedication("123");
    assertTrue(dispensed.isEmpty());
  }

  @Test
  void shouldNotInitializeTwice() {
    val ctx = ActorContext.getInstance();
    ActorContext.init(mock(PrimSysRestFactory.class));
    assertEquals(ctx, ActorContext.getInstance());
  }

  @Test
  void shouldThrowOnMissingInit() {
    resetSingleton(ActorContext.class, "instance");
    assertThrows(ConfigurationException.class, ActorContext::getInstance);
  }
}
