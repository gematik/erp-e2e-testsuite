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

package de.gematik.test.erezept.client.usecases;

import static java.text.MessageFormat.format;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.resources.erp.*;
import de.gematik.test.erezept.fhir.values.*;
import lombok.*;
import org.junit.jupiter.api.*;

class ChargeItemPutCommandTest {

  @Test
  void shouldBuildCorrectRequest() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.fromString("123");
    val chargeItem = new ErxChargeItem();

    val cmd = new ChargeItemPutCommand(prescriptionId, accessCode, chargeItem);
    assertTrue(cmd.getRequestLocator().contains(format("ac={0}", accessCode.getValue())));
    assertTrue(
        cmd.getRequestLocator().contains(format("ChargeItem/{0}", prescriptionId.getValue())));
    assertTrue(cmd.getRequestBody().isPresent());
    assertEquals(chargeItem, cmd.getRequestBody().orElseThrow());
  }

  @Test
  void shouldBuildRequestWithPrescriptionIdFromChargeItem() {
    val prescriptionId = PrescriptionId.random();
    val accessCode = AccessCode.fromString("123");
    val chargeItem = ErxChargeItemBuilder.faker(prescriptionId).build();

    val cmd = new ChargeItemPutCommand(accessCode, chargeItem);
    assertTrue(cmd.getRequestLocator().contains(format("ac={0}", accessCode.getValue())));
    assertTrue(
        cmd.getRequestLocator().contains(format("ChargeItem/{0}", prescriptionId.getValue())));
    assertTrue(cmd.getRequestBody().isPresent());
    assertEquals(chargeItem, cmd.getRequestBody().orElseThrow());
  }
}
