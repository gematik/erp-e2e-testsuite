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

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemFaker;
import de.gematik.test.erezept.fhir.parser.profiles.version.PatientenrechnungVersion;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.strategy.DequeStrategy;
import de.gematik.test.erezept.screenplay.util.ChargeItemChangeAuthorization;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.Test;

class AuthorizedChargeItemStrategyTest extends ErpFhirBuildingTest {

  @Test
  void shouldNotManipulateAnyValues() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val chargeItem =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withVersion(PatientenrechnungVersion.V1_0_0)
            .fake();
    val authorization = ChargeItemChangeAuthorization.forChargeItem(chargeItem, accessCode);
    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.getChargeItemChangeAuthorizations().append(authorization);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val strategyBuilder =
        new AuthorizedChargeItemStrategy.ConcreteBuilder<>(
            deque, AuthorizedChargeItemStrategyReceiver::new);
    val strategy = strategyBuilder.and().strategy();
    strategy.init(alice);

    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertEquals(accessCode, strategy.getAccessCode());
  }

  @Test
  void shouldManipulateValues() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.random();
    val chargeItem =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withVersion(PatientenrechnungVersion.V1_0_0)
            .withAccessCode(accessCode)
            .fake();
    val authorization =
        ChargeItemChangeAuthorization.forChargeItem(chargeItem, AccessCode.random());
    val useThePharmacyStack = ManagePharmacyPrescriptions.itWorksWith();
    useThePharmacyStack.getChargeItemChangeAuthorizations().append(authorization);

    val alice = new Actor("Alice");
    alice.can(useThePharmacyStack);

    val deque = DequeStrategy.FIFO;
    val strategyBuilder =
        new AuthorizedChargeItemStrategy.ConcreteBuilder<>(
            deque, AuthorizedChargeItemStrategyReceiver::new);
    val strategy =
        strategyBuilder
            .withCustomAccessCode(accessCode.getValue()) // just for code-coverage
            .withRandomAccessCode() // will overwrite the previous accesscode
            .and()
            .strategy();
    strategy.init(alice);

    assertEquals(prescriptionId, strategy.getPrescriptionId());
    assertNotEquals(accessCode, strategy.getAccessCode());
  }

  public record AuthorizedChargeItemStrategyReceiver(AuthorizedChargeItemStrategy strategy) {}
}
