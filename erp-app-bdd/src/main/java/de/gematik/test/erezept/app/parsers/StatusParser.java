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

package de.gematik.test.erezept.app.parsers;

import static java.text.MessageFormat.format;
import static org.hl7.fhir.r4.model.Task.TaskStatus.INPROGRESS;
import static org.hl7.fhir.r4.model.Task.TaskStatus.READY;

import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import lombok.val;

@UtilityClass
@Slf4j
public class StatusParser {
  public static String getExpectedStatusInfo(ErxTask task, KbvErpBundle kbvBundle) {
    val taskStatus = task.getStatus();

    if (taskStatus.equals(READY)) {
      return getStatusInfoForReady(task, kbvBundle);
    } else if (task.getLastMedicationDispenseDate().isPresent() && taskStatus.equals(INPROGRESS)) {
      return "Bereitgestellt heute";
    } else if (taskStatus.equals(INPROGRESS)) {
      return "Angenommen vor \\d+ (Sekunde[n]?|Minute[n]?)";
    } else {
      throw new IllegalStateException("Erx Task Status" + taskStatus + "is invalid");
    }
  }

  public static boolean compareStatusInfo(String expectedStatusInfo, String actualStatusInfo) {
    return actualStatusInfo.matches(expectedStatusInfo);
  }

  private static String getStatusInfoForReady(ErxTask task, KbvErpBundle kbvBundle) {
    val dc = DateConverter.getInstance();
    val now = LocalDate.now();

    LocalDate expiry;
    if (kbvBundle.getMedicationRequest().isMultiple()) {
      expiry =
          kbvBundle
              .getMedicationRequest()
              .getMvoEnd()
              .map(dc::dateToLocalDate)
              .orElseGet(() -> dc.dateToLocalDate(task.getExpiryDate()));
    } else {
      expiry = dc.dateToLocalDate(task.getAcceptDate());
    }

    // Note: .minusDays(1) is required because the expiryDate is the first day when the prescription
    // is no longer valid!
    val remainingDays =
        Duration.between(now.atStartOfDay(), expiry.minusDays(1).atStartOfDay()).toDays();

    val start =
        kbvBundle
            .getMedicationRequest()
            .getMvoStart()
            .map(dc::dateToLocalDate)
            .orElse(dc.dateToLocalDate(task.getAuthoredOn()));

    if (start.isAfter(now)) {
      return format("Einlösbar ab {0}", start.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    } else {
      return format("Noch {0} Tage einlösbar", remainingDays);
    }
  }
}
