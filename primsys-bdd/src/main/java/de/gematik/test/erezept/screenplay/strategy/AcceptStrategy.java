/*
 * Copyright (c) 2023 gematik GmbH
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

package de.gematik.test.erezept.screenplay.strategy;

import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class AcceptStrategy {

  private DequeStrategy dequeue;
  private ManagePharmacyPrescriptions prescriptionManager;

  private AcceptStrategy() {
    // use a builder instead
  }

  public void initialize(ManagePharmacyPrescriptions prescriptionManager) {
    this.prescriptionManager = prescriptionManager;
  }

  public DmcPrescription getDmcPrescription() {
    DmcPrescription prescriptionToAccept;
    if (this.dequeue == DequeStrategy.FIFO) {
      prescriptionToAccept = prescriptionManager.getOldestAssignedPrescription();
    } else {
      prescriptionToAccept = prescriptionManager.getLastAssignedPrescription();
    }
    return prescriptionToAccept;
  }

  /**
   * Finally teardown the strategy by consuming the used the acceptedPrescription. This method needs
   * to be called by intention after a successful operation.
   *
   * <p>By this the strategy remains idempotent on failures and consumes the prescription only after
   * successful dispensation
   */
  public void teardown() {
    if (this.dequeue == DequeStrategy.FIFO) {
      prescriptionManager.consumeOldestAssignedPrescription();
    } else {
      prescriptionManager.consumeLastAssignedPrescription();
    }
  }

  public static AcceptStrategy fromStack(DequeStrategy dequeue) {
    val ptds = new AcceptStrategy();
    ptds.dequeue = dequeue;
    return ptds;
  }
}
