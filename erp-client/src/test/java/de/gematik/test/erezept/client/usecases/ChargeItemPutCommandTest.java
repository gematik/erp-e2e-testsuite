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

class ChargeItemPutCommandTest {

  private ChargeItemPutCommand getChargeItemPutCommand() {
    val prescriptionId = PrescriptionId.random();
    return getChargeItemPutCommand(prescriptionId);
  }

  private ChargeItemPutCommand getChargeItemPutCommand(PrescriptionId prescriptionId) {
    val erxChargeItem = ErxChargeItemBuilder.faker(prescriptionId).build();
    return new ChargeItemPutCommand(erxChargeItem);
  }

  @Test
  void getRequestLocatorTestEqualObjectValue() {
    val prescriptionId = PrescriptionId.random();
    val item = getChargeItemPutCommand(prescriptionId);
    val item1 = getChargeItemPutCommand(prescriptionId);
    assertEquals(item.getRequestLocator(), item1.getRequestLocator());
  }

  @Test
  void getRequestLocatorTestCorrectLengthTest() {
    ChargeItemPutCommand chargeItemPostCommand = getChargeItemPutCommand();
    assertEquals(34, chargeItemPostCommand.getRequestLocator().length());
  }

  @Test
  void getRequestLocatorTestCorrectLengthStartsWithSlashTest() {
    ChargeItemPutCommand chargeItemPutCommand = getChargeItemPutCommand();
    var requestString = chargeItemPutCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsChargeItemTest() {
    ChargeItemPutCommand chargeItemPutCommand = getChargeItemPutCommand();
    var requestString = chargeItemPutCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals("ChargeItem", requestStringArray[1]);
  }

  @Test
  void getRequestLocatorTestCorrectPrescriptionID() {
    val prescriptionId = PrescriptionId.random();
    ChargeItemPutCommand chargeItemPutCommand = getChargeItemPutCommand(prescriptionId);
    var requestString = chargeItemPutCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals(prescriptionId.getValue(), requestStringArray[2]);
  }

  @Test
  void getRequestBody() {
    ChargeItemPutCommand chargeItemPutCommand = getChargeItemPutCommand();
    assertTrue(chargeItemPutCommand.getRequestBody().isPresent());
  }
}
