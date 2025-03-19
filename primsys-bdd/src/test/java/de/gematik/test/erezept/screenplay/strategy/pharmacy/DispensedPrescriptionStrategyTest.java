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

package de.gematik.test.erezept.screenplay.strategy.pharmacy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.test.erezept.fhir.r4.erp.ErxReceipt;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.fhir.values.Secret;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.DispenseReceipt;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class DispensedPrescriptionStrategyTest extends PharmacyPrescriptionStrategyTest {

  @Test
  void shouldNotManipulateAnyValues() {
    val kvnr = KVNR.random();
    val taskId = TaskId.from("123");
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val secret = Secret.fromString("123");
    val receipt = new ErxReceipt();
    val dispensed = new DispenseReceipt(kvnr, taskId, prescriptionId, accessCode, secret, receipt);

    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.appendDispensedPrescriptions(dispensed);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val strategyBuilder =
        new DispensedPrescriptionStrategy.ConcreteBuilder<>(
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
    val kvnr = KVNR.random();
    val taskId = TaskId.from("123");
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val secret = Secret.fromString("123");
    val receipt = new ErxReceipt();
    val dispensed = new DispenseReceipt(kvnr, taskId, prescriptionId, accessCode, secret, receipt);

    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.appendDispensedPrescriptions(dispensed);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val customSecret = Secret.fromString("456");
    val strategyBuilder =
        new DispensedPrescriptionStrategy.ConcreteBuilder<>(
            deque, PharmacyPrescriptionStrategyReceiver::new);
    val strategy =
        strategyBuilder.withRandomAccessCode().withCustomSecret(customSecret).and().strategy();
    strategy.init(alice);

    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(customSecret, strategy.getSecret());
    assertNotEquals(accessCode, strategy.getAccessCode());
    assertEquals(taskId, strategy.getTaskId());
    assertEquals(kvnr, strategy.getReceiverKvnr());
  }
}
