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
 */

package de.gematik.test.erezept.screenplay.strategy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.konnektor.soap.mock.LocalSigner;
import java.util.Optional;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class PrescriptionToDispenseStrategyTest {

  @Test
  void shouldUseNotManipulatedPrescription() {
    val hba = SmartcardArchive.fromResources().getHbaByICCSN("80276001011699901501");

    val stack = ManagePharmacyPrescriptions.itWorksWith();
    val erxBundle = mock(ErxAcceptBundle.class);
    val erxTask = mock(ErxTask.class);
    when(erxBundle.getTask()).thenReturn(erxTask);
    when(erxTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    val accessCode = new AccessCode("123");
    when(erxTask.getAccessCode()).thenReturn(accessCode);
    val secret = new Secret("123");
    when(erxTask.getSecret()).thenReturn(Optional.of(secret));
    val prescriptionId = PrescriptionId.random();
    when(erxTask.getPrescriptionId()).thenReturn(prescriptionId);
    when(erxBundle.getTaskId()).thenReturn(TaskId.from("890"));
    when(erxBundle.hasConsent()).thenReturn(false);
    when(erxBundle.getKbvBundleId()).thenReturn("678");
    when(erxBundle.getSignedKbvBundle())
        .thenReturn(
            LocalSigner.signQES(hba, CryptoSystem.ECC_256)
                .signDocument(false, "<xml>bundle</xml>"));

    stack.appendAcceptedPrescription(erxBundle);
    val strategy = PrescriptionToDispenseStrategy.withDequeue(DequeStrategy.FIFO).initialize(stack);
    assertEquals("X123456789", strategy.getKvnr().getValue());
    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(secret, strategy.getSecret());
    assertEquals(accessCode, strategy.getAccessCode());
    assertEquals(TaskId.from("890"), strategy.getTaskId());
    assertEquals("678", strategy.getKbvBundleId());
    assertEquals("<xml>bundle</xml>", strategy.getKbvBundleAsString());
    assertFalse(strategy.hasPatient());
    assertTrue(strategy.getPatient().isEmpty());
    assertFalse(strategy.hasConsent());

    strategy.teardown();
    assertTrue(stack.getAssignedList().isEmpty());
  }

  @Test
  void shouldUseManipulatedPrescription() {
    val stack = ManagePharmacyPrescriptions.itWorksWith();
    val erxBundle = mock(ErxAcceptBundle.class);
    val erxTask = mock(ErxTask.class);
    when(erxBundle.getTask()).thenReturn(erxTask);
    when(erxTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    val accessCode = new AccessCode("123");
    when(erxTask.getAccessCode()).thenReturn(accessCode);
    val secret = new Secret("123");
    when(erxTask.getSecret()).thenReturn(Optional.of(secret));
    val prescriptionId = PrescriptionId.random();
    when(erxTask.getPrescriptionId()).thenReturn(prescriptionId);
    when(erxBundle.getTaskId()).thenReturn(TaskId.from("890"));
    when(erxBundle.hasConsent()).thenReturn(true);
    when(erxBundle.getKbvBundleId()).thenReturn("678");

    stack.appendAcceptedPrescription(erxBundle);
    val strategy =
        PrescriptionToDispenseStrategy.withDequeue(DequeStrategy.FIFO)
            .kvnr(KVNR.from("M123456789"))
            .secret("456")
            .taskId("098")
            .initialize(stack);
    assertEquals("M123456789", strategy.getKvnr().getValue());
    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(new Secret("456"), strategy.getSecret());
    assertEquals(accessCode, strategy.getAccessCode());
    assertEquals(TaskId.from("098"), strategy.getTaskId());
    assertEquals("678", strategy.getKbvBundleId());
    assertFalse(strategy.hasPatient());
    assertTrue(strategy.getPatient().isEmpty());
    assertTrue(strategy.hasConsent());
  }

  @Test
  void shouldUseManipulatedPrescriptionKvnrFromActor() {
    val stack = ManagePharmacyPrescriptions.itWorksWith();
    val erxBundle = mock(ErxAcceptBundle.class);
    val erxTask = mock(ErxTask.class);
    when(erxBundle.getTask()).thenReturn(erxTask);
    when(erxTask.getForKvnr()).thenReturn(Optional.of(KVNR.from("X123456789")));
    val mockActor = mock(Actor.class);
    val mockPatientData = mock(ProvidePatientBaseData.class);
    when(mockActor.abilityTo(ProvidePatientBaseData.class)).thenReturn(mockPatientData);
    when(mockPatientData.getKvnr()).thenReturn(KVNR.from("B123456789"));

    stack.appendAcceptedPrescription(erxBundle);
    val strategy =
        PrescriptionToDispenseStrategy.withDequeue(DequeStrategy.FIFO)
            .kvnr(KVNR.from("M123456789"))
            .patient(mockActor)
            .initialize(stack);
    assertEquals("B123456789", strategy.getKvnr().getValue());
    assertTrue(strategy.hasPatient());
    assertTrue(strategy.getPatient().isPresent());
  }
}
