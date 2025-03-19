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
 */

package de.gematik.test.erezept.primsys.model;

import de.gematik.test.erezept.primsys.data.AcceptedPrescriptionDto;
import de.gematik.test.erezept.primsys.data.DispensedMedicationDto;
import de.gematik.test.erezept.primsys.data.PrescriptionDto;
import java.util.LinkedList;
import java.util.List;
import lombok.Getter;

@Getter
public class ContextData {

  public static final int FIRST_ENTRY = 0;
  public static final int MAX_QUEUE_LENGTH = 100;
  private final List<PrescriptionDto> readyPrescriptions;
  private final List<AcceptedPrescriptionDto> acceptedPrescriptions;
  private final List<DispensedMedicationDto> dispensedMedications;

  public ContextData() {
    this.readyPrescriptions = new LinkedList<>();
    this.acceptedPrescriptions = new LinkedList<>();
    this.dispensedMedications = new LinkedList<>();
  }

  public void addPrescription(PrescriptionDto prescription) {
    ensureMaxLength(readyPrescriptions);
    this.readyPrescriptions.add(prescription);
  }

  public void addAcceptedPrescription(AcceptedPrescriptionDto prescription) {
    ensureMaxLength(acceptedPrescriptions);
    this.acceptedPrescriptions.add(prescription);
  }

  public void addDispensedMedications(DispensedMedicationDto dispensed) {
    ensureMaxLength(dispensedMedications);
    this.dispensedMedications.add(dispensed);
  }

  public boolean removeAcceptedPrescription(String prescriptionId) {
    return this.acceptedPrescriptions.removeIf(
        prescription -> prescription.getPrescriptionId().equals(prescriptionId));
  }

  public List<PrescriptionDto> getReadyPrescriptionsByKvnr(String kvnr) {
    return readyPrescriptions.stream().filter(p -> p.getPatient().getKvnr().equals(kvnr)).toList();
  }

  public List<AcceptedPrescriptionDto> getAcceptedPrescriptionsByKvnr(String kvnr) {
    return acceptedPrescriptions.stream().filter(p -> p.getForKvnr().equals(kvnr)).toList();
  }

  public List<DispensedMedicationDto> getDispensedPrescriptionsByKvnr(String kvnr) {
    return dispensedMedications.stream()
        .filter(p -> p.getAcceptData().getForKvnr().equals(kvnr))
        .toList();
  }

  private void ensureMaxLength(List<?> list) {
    if (maxLengthHasReached(list)) list.remove(FIRST_ENTRY);
  }

  private <T> boolean maxLengthHasReached(List<T> t) {
    return t.size() >= MAX_QUEUE_LENGTH;
  }
}
