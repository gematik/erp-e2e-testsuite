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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.values.PrescriptionId;
import org.junit.jupiter.api.Test;

class ChargeItemDeleteCommandTest {

  @Test
  void getRequestLocatorCorrectIDTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    var requestString = chargeItemDeleteCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals(prescriptionID.getValue(), requestStringArray[2]);
  }

  @Test
  void getRequestLocatorCorrectLengthTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    assertEquals(34, chargeItemDeleteCommand.getRequestLocator().length());
  }

  @Test
  void getRequestLocatorStartsWithSlashTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    var requestString = chargeItemDeleteCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsChargeItemTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    var requestString = chargeItemDeleteCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals("ChargeItem", requestStringArray[1]);
  }

  @Test
  void getRequestBodyNotNullTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    assertNotNull(chargeItemDeleteCommand.getRequestBody());
  }

  @Test
  void getRequestBodyOptionalEmptyStringTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    assertTrue(chargeItemDeleteCommand.getRequestBody().isEmpty());
  }
}
