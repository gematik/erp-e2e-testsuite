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

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import de.gematik.test.erezept.primsys.rest.data.DispensedData;
import de.gematik.test.erezept.primsys.rest.data.PrescriptionData;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

public class ContextData {

  public static final int FIRST_ENTRY = 0;
  public static final int MAX_QUEUE_LENGTH = 100;
  @Getter private final List<PrescriptionData> prescriptions;
  @Getter private final List<AcceptData> acceptedPrescriptions;
  @Getter private final List<DispensedData> dispensedMedications;

  public ContextData() {
    this.prescriptions = new LinkedList<>();
    this.acceptedPrescriptions = new LinkedList<>();
    this.dispensedMedications = new LinkedList<>();
  }

  public void addPrescription(PrescriptionData prescription) {
    ensureMaxLength(prescriptions);
    this.prescriptions.add(prescription);
  }

  public void addAcceptedPrescription(AcceptData prescription) {
    ensureMaxLength(acceptedPrescriptions);
    this.acceptedPrescriptions.add(prescription);
  }

  public void addDispensedMedications(DispensedData dispensed) {
    ensureMaxLength(dispensedMedications);
    this.dispensedMedications.add(dispensed);
  }

  public boolean removeAcceptedPrescription(String taskId) {
    return this.acceptedPrescriptions.removeIf(ad -> ad.getTaskId().equals(taskId));
  }

  private void ensureMaxLength(List<?> list) {
    if (maxLengthHasReached(list)) list.remove(FIRST_ENTRY);
  }

  private <T> boolean maxLengthHasReached(List<T> t) {
    return t.size() >= MAX_QUEUE_LENGTH;
  }
}
