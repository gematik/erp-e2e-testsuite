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

import static de.gematik.test.erezept.app.parsers.StatusParser.*;
import static java.lang.String.format;
import static org.hl7.fhir.r4.model.Task.TaskStatus.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.fhir.date.DateConverter;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpBundle;
import de.gematik.test.erezept.fhir.r4.kbv.KbvErpMedicationRequest;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;
import lombok.val;
import org.junit.jupiter.api.Test;

class StatusParserTest {
  @Test
  void shouldReturnCorrectStatusInfoForReadyAndValidInTwoDays() {
    val erxTask = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);

    val currentDate = LocalDate.now();
    val validityStartDate = currentDate.plusDays(2);
    val validityEndDate = currentDate.plusDays(10);

    when(erxTask.getStatus()).thenReturn(READY);
    when(erxTask.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(validityEndDate));
    when(erxTask.getAuthoredOn())
        .thenReturn(DateConverter.getInstance().localDateToDate(validityEndDate));
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.getMvoStart())
        .thenReturn(Optional.of(DateConverter.getInstance().localDateToDate(validityStartDate)));
    when(medicationRequest.isMultiple()).thenReturn(false);

    val expectedStatusInfo =
        format(
            "Einlösbar ab %s", validityStartDate.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")));
    assertEquals(expectedStatusInfo, getExpectedStatusInfo(erxTask, kbvBundle));
  }

  @Test
  void shouldReturnCorrectStatusInfoForReadyWithTwoDaysLeft() {
    val erxTask = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);
    val medicationRequest = mock(KbvErpMedicationRequest.class);

    when(erxTask.getStatus()).thenReturn(READY);
    when(erxTask.getAcceptDate())
        .thenReturn(DateConverter.getInstance().localDateToDate(LocalDate.now().plusDays(3)));
    when(erxTask.getAuthoredOn()).thenReturn(new Date());
    when(kbvBundle.getMedicationRequest()).thenReturn(medicationRequest);
    when(medicationRequest.isMultiple()).thenReturn(false);

    assertEquals("Noch 2 Tage einlösbar", getExpectedStatusInfo(erxTask, kbvBundle));
  }

  @Test
  void shouldReturnCorrectStatusInfoForInProgress() {
    val erxTask = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);

    when(erxTask.getLastMedicationDispenseDate()).thenReturn(Optional.empty());
    when(erxTask.getStatus()).thenReturn(INPROGRESS);

    val expectedStatusInfo = getExpectedStatusInfo(erxTask, kbvBundle);

    assertEquals("Angenommen vor \\d+ (Sekunde[n]?|Minute[n]?)", expectedStatusInfo);
    assertTrue("Angenommen vor 1 Sekunde".matches(expectedStatusInfo));
    assertTrue("Angenommen vor 10 Sekunden".matches(expectedStatusInfo));
    assertTrue("Angenommen vor 1 Minute".matches(expectedStatusInfo));
    assertTrue("Angenommen vor 2 Minuten".matches(expectedStatusInfo));
  }

  @Test
  void shouldReturnCorrectStatusInfoForInProgressAndDispensed() {
    val erxTask = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);

    when(erxTask.getLastMedicationDispenseDate()).thenReturn(Optional.ofNullable(Instant.now()));
    when(erxTask.getStatus()).thenReturn(INPROGRESS);

    assertEquals("Bereitgestellt heute", getExpectedStatusInfo(erxTask, kbvBundle));
  }

  @Test
  void shouldThrowForAnotherErxTaskStatus() {
    val erxTask = mock(ErxTask.class);
    val kbvBundle = mock(KbvErpBundle.class);

    when(erxTask.getLastMedicationDispenseDate()).thenReturn(Optional.ofNullable(Instant.now()));
    when(erxTask.getStatus()).thenReturn(COMPLETED);

    assertThrows(IllegalStateException.class, () -> getExpectedStatusInfo(erxTask, kbvBundle));
  }

  @Test
  void shouldHaveTheSameStatusInfo() {
    assertTrue(compareStatusInfo("Angenommen vor \\d+ Sekunden", "Angenommen vor 2 Sekunden"));
  }

  @Test
  void shouldNotHaveTheSameStatusInfo() {
    assertFalse(compareStatusInfo("Angenommen vor \\d+ Sekunden", "Bereitgestellt heute"));
  }
}
