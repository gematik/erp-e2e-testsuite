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

package de.gematik.test.erezept.screenplay.util;

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.erezept.fhir.builder.erp.*;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ChargeItemChangeAuthorizationTest extends ErpFhirBuildingTest {

  @Test
  void shouldCreateFromChargeItem() {
    val prescriptionId = PrescriptionId.random();
    val chargeItem =
        ErxChargeItemFaker.builder()
            .withPrescriptionId(prescriptionId)
            .withAccessCode("123") // this is not the value which will be used!!
            .fake();
    val accessCode = AccessCode.random();
    val authorization = ChargeItemChangeAuthorization.forChargeItem(chargeItem, accessCode);
    assertEquals(prescriptionId, authorization.getPrescriptionId());
    assertEquals(accessCode, authorization.getAccessCode());
  }

  @Test
  void shouldNotThrowOnChargeItemWithoutAccessCode() {
    val prescriptionId = PrescriptionId.random();
    val chargeItem = ErxChargeItemFaker.builder().withPrescriptionId(prescriptionId).fake();
    assertDoesNotThrow(
        () -> ChargeItemChangeAuthorization.forChargeItem(chargeItem, AccessCode.random()));
  }
}
