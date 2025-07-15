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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.client.usecases;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.fhir.extensions.erp.MarkingFlag;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import lombok.val;
import org.junit.jupiter.api.Test;

class ChargeItemPatchCommandTest extends ErpFhirParsingTest {

  private ChargeItemPatchCommand getChargeItemPatchCommand() {
    val prescriptionId = PrescriptionId.random();
    return getChargeItemPatchCommand(prescriptionId);
  }

  private ChargeItemPatchCommand getChargeItemPatchCommand(PrescriptionId prescriptionId) {
    val flags = MarkingFlag.with(true, false, true);
    return new ChargeItemPatchCommand(prescriptionId, flags);
  }

  @Test
  void getRequestLocatorTestEqualObjectValue() {
    val prescriptionId = PrescriptionId.random();
    val item = getChargeItemPatchCommand(prescriptionId);
    val item1 = getChargeItemPatchCommand(prescriptionId);
    assertEquals(item.getRequestLocator(), item1.getRequestLocator());
  }

  @Test
  void getRequestLocatorTestCorrectLengthTest() {
    val cmd = getChargeItemPatchCommand();
    assertEquals(34, cmd.getRequestLocator().length());
  }

  @Test
  void getRequestLocatorTestCorrectLengthStartsWithSlashTest() {
    val cmd = getChargeItemPatchCommand();
    val requestString = cmd.getRequestLocator();
    assertTrue(requestString.startsWith("/"));
  }

  @Test
  void getRequestLocatorSecondEntryIsChargeItemTest() {
    val cmd = getChargeItemPatchCommand();
    val requestString = cmd.getRequestLocator();
    val requestStringArray = requestString.split("/");
    assertEquals("ChargeItem", requestStringArray[1]);
  }

  @Test
  void getRequestLocatorTestCorrectPrescriptionID() {
    val prescriptionId = PrescriptionId.random();
    val cmd = getChargeItemPatchCommand(prescriptionId);
    val requestString = cmd.getRequestLocator();
    val requestStringArray = requestString.split("/");
    assertEquals(prescriptionId.getValue(), requestStringArray[2]);
  }

  @Test
  void getRequestBody() {
    val cmd = getChargeItemPatchCommand();
    assertTrue(cmd.getRequestBody().isPresent());
  }
}
