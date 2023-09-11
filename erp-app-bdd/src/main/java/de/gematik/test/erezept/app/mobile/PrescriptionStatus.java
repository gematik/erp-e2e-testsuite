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

package de.gematik.test.erezept.app.mobile;

import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.resources.erp.ErxPrescriptionBundle;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.hl7.fhir.r4.model.Period;
import org.hl7.fhir.r4.model.Task;

import java.time.LocalDate;

@Getter
@RequiredArgsConstructor
public enum PrescriptionStatus {
  REDEEMABLE("Einlösbar"),
  LATER_REDEEMABLE("Später einlösbar"),
  CLAIMED("In Einlösung");

  private final String label;

  @Override
  public String toString() {
    return label;
  }

  public static PrescriptionStatus from(ErxPrescriptionBundle bundle) {
    if (bundle.getTask().getStatus() == Task.TaskStatus.INPROGRESS) {
      return PrescriptionStatus.CLAIMED;
    }

    val kbvBundle = bundle.getKbvBundle();
    val medReq = kbvBundle.getMedicationRequest();

    // check if MVO and provide staus accordingly
    val period = medReq.getMvoPeriod().orElse(null);
    if (period != null) {
      return from(period);
    }

    // what about expired prescriptions?
    return PrescriptionStatus.REDEEMABLE;
  }

  public static PrescriptionStatus from(Period period) {
    val dc = DateConverter.getInstance();
    val start = dc.dateToLocalDate(period.getStart());

    if (start.isAfter(LocalDate.now())) {
      return PrescriptionStatus.LATER_REDEEMABLE;
    } else {
      // what about expired prescriptions?
      return PrescriptionStatus.REDEEMABLE;
    }
  }
}
