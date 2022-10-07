/*
 * Copyright (c) 2022 gematik GmbH
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

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.builder.erp.ErxChargeItemBuilder;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChargeItemPostCommandTest {

  private ChargeItemPostCommand getChargeItemPostCommand() {
    val prescriptionId = PrescriptionId.random();
    return getChargeItemPostCommand(prescriptionId);
  }

  private ChargeItemPostCommand getChargeItemPostCommand(PrescriptionId prescriptionId) {
    val erxChargeItem = ErxChargeItemBuilder.faker(prescriptionId).build();
    return new ChargeItemPostCommand(erxChargeItem);
  }

  @Test
  void getRequestLocatorTestEqualObjectTest() {
    val prescriptionId = PrescriptionId.random();
    val chargeItemPostCommand = getChargeItemPostCommand(prescriptionId);
    val chargeItemPostCommand1 = getChargeItemPostCommand(prescriptionId);
    assertEquals(
        chargeItemPostCommand.getRequestLocator(), chargeItemPostCommand1.getRequestLocator());
  }

  @Test
  void getRequestLocatorTestCorrectLengthTest() {
    ChargeItemPostCommand chargeItemPostCommand = getChargeItemPostCommand();
    assertEquals(34, chargeItemPostCommand.getRequestLocator().length());
  }

  @Test
  void getRequestLocatorTestCorrectLengthStartsWithSlashTest() {
    ChargeItemPostCommand chargeItemPostCommand = getChargeItemPostCommand();
    var requestString = chargeItemPostCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsChargeItemTest() {
    ChargeItemPostCommand chargeItemPostCommand = getChargeItemPostCommand();
    var requestString = chargeItemPostCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals("ChargeItem", requestStringArray[1]);
  }

  @Test
  void getRequestLocatorTestCorrectPrescriptionID() {
    val prescriptionId = PrescriptionId.random();
    ChargeItemPostCommand chargeItemPostCommand = getChargeItemPostCommand(prescriptionId);
    var requestString = chargeItemPostCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals(prescriptionId.getValue(), requestStringArray[2]);
  }

  @Test
  void getRequestBody() {
    ChargeItemPostCommand chargeItemPostCommand = getChargeItemPostCommand();
    assertTrue(chargeItemPostCommand.getRequestBody().isPresent());
  }
}
