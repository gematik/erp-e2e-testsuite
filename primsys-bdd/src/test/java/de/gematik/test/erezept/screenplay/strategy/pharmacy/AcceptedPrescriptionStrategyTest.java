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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.strategy.*;
import de.gematik.test.erezept.screenplay.strategy.pharmacy.PharmacyPrescriptionStrategyTest.*;
import java.util.*;
import lombok.*;
import net.serenitybdd.screenplay.*;
import org.junit.jupiter.api.*;

class AcceptedPrescriptionStrategyTest {

  @Test
  void shouldNotManipulateAnyValues() {
    val kvnr = KVNR.from("X123456789");
    val taskId = TaskId.from("123");
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val secret = Secret.fromString("123");

    val task = mock(ErxTask.class);
    val accepted = mock(ErxAcceptBundle.class);

    when(accepted.getTask()).thenReturn(task);
    when(accepted.getTaskId()).thenReturn(taskId);
    when(task.getPrescriptionId()).thenReturn(prescriptionId);
    when(task.getForKvnr()).thenReturn(Optional.of(kvnr));
    when(task.getSecret()).thenReturn(Optional.of(secret));
    when(task.getAccessCode()).thenReturn(accessCode);

    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.appendAcceptedPrescription(accepted);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val strategyBuilder =
        new AcceptedPrescriptionStrategy.ConcreteBuilder<>(
            deque, PharmacyPrescriptionStrategyReceiver::new);
    val strategy = strategyBuilder.and().strategy();
    strategy.init(alice);

    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(secret, strategy.getSecret());
    assertEquals(accessCode, strategy.getAccessCode());
    assertEquals(taskId, strategy.getTaskId());
    assertEquals(kvnr, strategy.getReceiverKvnr());
  }

  @Test
  void shouldManipulateValues() {
    val kvid = KVNR.from("X123456789");
    val taskId = TaskId.from("123");
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val secret = Secret.fromString("123");

    val task = mock(ErxTask.class);
    val accepted = mock(ErxAcceptBundle.class);

    when(accepted.getTask()).thenReturn(task);
    when(accepted.getTaskId()).thenReturn(taskId);
    when(task.getPrescriptionId()).thenReturn(prescriptionId);
    when(task.getForKvnr()).thenReturn(Optional.of(kvid));
    when(task.getSecret()).thenReturn(Optional.of(secret));
    when(task.getAccessCode()).thenReturn(accessCode);

    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.appendAcceptedPrescription(accepted);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val customAccessCode = AccessCode.random();
    val customSecret = Secret.fromString("456");
    val strategyBuilder =
        new AcceptedPrescriptionStrategy.ConcreteBuilder<>(
            deque, PharmacyPrescriptionStrategyReceiver::new);
    val strategy =
        strategyBuilder
            .withCustomAccessCode(customAccessCode.getValue())
            .withCustomSecret(customSecret.getValue())
            .and()
            .strategy();
    strategy.init(alice);

    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(customSecret, strategy.getSecret());
    assertEquals(customAccessCode, strategy.getAccessCode());
    assertEquals(taskId, strategy.getTaskId());
    assertEquals(kvid, strategy.getReceiverKvnr());
  }
}
