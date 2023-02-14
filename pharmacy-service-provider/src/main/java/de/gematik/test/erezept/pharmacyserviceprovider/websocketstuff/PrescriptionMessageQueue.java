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

package de.gematik.test.erezept.pharmacyserviceprovider.websocketstuff;

import de.gematik.test.erezept.pspwsclient.dataobjects.PspMessage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@Slf4j
public class PrescriptionMessageQueue {

  @Getter
  private final List<PspMessage> pspMessageMessageQueueList =
      Collections.synchronizedList(new ArrayList<>());

  public boolean hasSavedMessages() {
    return !pspMessageMessageQueueList.isEmpty();
  }

  protected void removePrescriptions(String clientId) {
    val result = getPrescriptionWith(clientId);
    for (val r : result) {
      pspMessageMessageQueueList.remove(r);
    }
  }

  protected List<PspMessage> popPrescriptionWith(String clientId) {
    val result = getPrescriptionWith(clientId);
    removePrescriptions(clientId);
    return result;
  }

  private List<PspMessage> getPrescriptionWith(String clientId) {
    return pspMessageMessageQueueList.stream()
        .filter(x -> x.getClientId().equals(clientId))
        .toList();
  }
}
