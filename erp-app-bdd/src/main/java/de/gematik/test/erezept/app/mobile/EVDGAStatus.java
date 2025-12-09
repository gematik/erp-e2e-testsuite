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

package de.gematik.test.erezept.app.mobile;

import de.gematik.test.erezept.fhir.r4.erp.ErxMedicationDispenseBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxPrescriptionBundle;
import java.util.NoSuchElementException;
import lombok.Getter;
import lombok.val;
import org.hl7.fhir.r4.model.Task;

@Getter
public enum EVDGAStatus {
  READY_FOR_REQUEST,
  WAITING_OR_ACCEPTED,
  DECLINED,
  GRANTED,
  DOWNLOADED,
  ACTIVATED,
  DELETED,
  NULL;

  public static EVDGAStatus fromString(String value) {
    return switch (value.toUpperCase()) {
      case "READY_FOR_REQUEST" -> READY_FOR_REQUEST;
      case "WAITING_OR_ACCEPTED" -> WAITING_OR_ACCEPTED;
      case "DECLINED" -> DECLINED;
      case "GRANTED" -> GRANTED;
      case "DOWNLOADED" -> DOWNLOADED;
      case "ACTIVATED" -> ACTIVATED;
      case "DELETED" -> DELETED;
      default -> throw new NoSuchElementException(value);
    };
  }

  public static Task.TaskStatus toCorrespondingStatus(EVDGAStatus shownStatus) {
    if (shownStatus != NULL) {
      if (shownStatus == READY_FOR_REQUEST) {
        return Task.TaskStatus.READY;
      } else if (shownStatus == WAITING_OR_ACCEPTED) {
        return Task.TaskStatus.INPROGRESS;
      } else if (shownStatus == DECLINED
          || shownStatus == GRANTED
          || shownStatus == DOWNLOADED
          || shownStatus == ACTIVATED) {
        return Task.TaskStatus.COMPLETED;
      } else {
        return Task.TaskStatus.CANCELLED;
      }
    } else {
      return Task.TaskStatus.NULL;
    }
  }

  public static EVDGAStatus fromBackendStatus(
      ErxMedicationDispenseBundle dispenseBundle, ErxPrescriptionBundle evdgaBundle) {
    val evdgaBackendStatus = evdgaBundle.getTask().getStatus();
    val medicationDispense =
        dispenseBundle.getDispensePairBy(evdgaBundle.getTask().getPrescriptionId());
    if (medicationDispense.get(0).getRight() != null) {
      throw new IllegalArgumentException("Not a HealthAppRequest");
    }

    val redeemCode = medicationDispense.get(0).getLeft().getRedeemCode();
    val codeReceived = redeemCode.isPresent();

    if (evdgaBackendStatus.equals(Task.TaskStatus.READY)) {
      return EVDGAStatus.READY_FOR_REQUEST;
    } else if (evdgaBackendStatus.equals(Task.TaskStatus.INPROGRESS)) {
      return EVDGAStatus.WAITING_OR_ACCEPTED;
    } else if (evdgaBackendStatus.equals(Task.TaskStatus.COMPLETED) && !codeReceived) {
      return EVDGAStatus.DECLINED;
    } else if (evdgaBackendStatus.equals(Task.TaskStatus.COMPLETED)) {
      return EVDGAStatus.GRANTED;
    } else if (evdgaBackendStatus.equals(Task.TaskStatus.CANCELLED)) {
      return EVDGAStatus.DELETED;
    }
    return EVDGAStatus.NULL;
  }
}
