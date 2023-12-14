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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.values.*;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChargeItemGetByIdCommandTest {

  @Test
  void getRequestLocatorCorrectIDTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemDeleteCommand chargeItemDeleteCommand = new ChargeItemDeleteCommand(prescriptionID);
    var requestString = chargeItemDeleteCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals(prescriptionID.getValue(), requestStringArray[2]);
  }

  @Test
  void getRequestLocatorCoorectLengthTest() {
    var prescriptionID = PrescriptionId.random();
    val chargeItemGetByIdCommand = new ChargeItemGetByIdCommand(prescriptionID);
    assertEquals(34, chargeItemGetByIdCommand.getRequestLocator().length());
  }

  @Test
  void getRequestLocatorStartsWithSlashTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemGetByIdCommand chargeItemGetByIdCommand =
        new ChargeItemGetByIdCommand(prescriptionID);
    var requestString = chargeItemGetByIdCommand.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsChargeItemTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemGetByIdCommand chargeItemGetByIdCommand =
        new ChargeItemGetByIdCommand(prescriptionID);
    var requestString = chargeItemGetByIdCommand.getRequestLocator();
    var requestStringArray = requestString.split("/");
    assertEquals("ChargeItem", requestStringArray[1]);
  }

  @Test
  void getRequestBodyNotNullTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemGetByIdCommand chargeItemGetByIdCommand =
        new ChargeItemGetByIdCommand(prescriptionID);
    assertNotNull(chargeItemGetByIdCommand.getRequestBody());
  }

  @Test
  void getRequestBodyOptionalEmptyStringTest() {
    var prescriptionID = PrescriptionId.random();
    ChargeItemGetByIdCommand chargeItemGetByIdCommand =
        new ChargeItemGetByIdCommand(prescriptionID);
    assertTrue(chargeItemGetByIdCommand.getRequestBody().isEmpty());
  }

  @Test
  void shouldProvideAccessCodeForRequest() {
    val ac = AccessCode.random();
    val prescriptionID = PrescriptionId.random();
    val cmd = new ChargeItemGetByIdCommand(prescriptionID, ac);
    assertTrue(cmd.getRequestLocator().contains(format("ac={0}", ac.getValue())));
  }
}
