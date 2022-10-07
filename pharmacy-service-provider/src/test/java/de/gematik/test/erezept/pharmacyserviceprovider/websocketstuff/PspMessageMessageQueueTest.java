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

package de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.pspwsclient.dataobjects.DeliveryOption;
import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PspMessageMessageQueueTest {

  static DeliveryOption deliverOption;
  static String telematikId;
  static String transactionId;
  static byte[] blob;
  static PspMessage pspMessage;
  static PspMessage pspMessage2;
  static PrescriptionMessageQueue prescriptionMessageQueue;

  @BeforeAll
  static void setup() {
    deliverOption = DeliveryOption.ON_PREMISE;
    telematikId = "testTelemID";
    transactionId = "testTranacID";
    blob =
        "senselessUnididentifiedAndExtraLongNeedlessStringTo build an sensless unpracticable byteArray with a number : 1"
            .getBytes();
    pspMessage = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);
    telematikId = "testTelemID---2";
    pspMessage2 = PspMessage.create(deliverOption, telematikId, transactionId, blob, null);

    prescriptionMessageQueue = new PrescriptionMessageQueue();
  }

  @BeforeEach
  void emptyList() {
    prescriptionMessageQueue.getPspMessageMessageQueueList().clear();
  }

  @Test
  void hasSavedMessages() {
    prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage);
    assertTrue(prescriptionMessageQueue.hasSavedMessages());
  }

  @Test
  void popPrescriptionWorks() {
    for (int i = 0; i < 100; i++)
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage2);
    assertTrue(prescriptionMessageQueue.hasSavedMessages());
    prescriptionMessageQueue.popPrescriptionWith(telematikId);
    assertFalse(prescriptionMessageQueue.hasSavedMessages());
  }

  @Test
  void hasNoSavedMessages() {
    assertFalse(prescriptionMessageQueue.hasSavedMessages());
  }

  @Test
  void getPrescriptionList() {
    for (int i = 0; i < 10; i++) {
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage);
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage2);
    }
    var prescriptionList = prescriptionMessageQueue.popPrescriptionWith(telematikId);
    assertEquals(10, prescriptionList.size());
  }

  @Test
  void getPrescriptionList2() {
    for (int i = 0; i < 10; i++) {
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage);
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage2);
    }
    var prescriptionList = prescriptionMessageQueue.popPrescriptionWith(" 123 ");
    assertEquals(0, prescriptionList.size());
  }

  @Test
  void removeEntriesOfIdFromList() {
    for (int i = 0; i < 20; i++) {
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage);
      prescriptionMessageQueue.getPspMessageMessageQueueList().add(pspMessage2);
    }
    prescriptionMessageQueue.removePrescriptions(telematikId);
    assertEquals(20, prescriptionMessageQueue.getPspMessageMessageQueueList().size());
  }
}
