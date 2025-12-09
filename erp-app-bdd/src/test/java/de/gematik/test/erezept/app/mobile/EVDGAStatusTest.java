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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.erezept.fhir.extensions.erp.RedeemCode;
import de.gematik.test.erezept.fhir.r4.erp.*;
import de.gematik.test.erezept.fhir.values.PrescriptionId;
import java.util.*;
import java.util.stream.Stream;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;
import org.hl7.fhir.r4.model.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EVDGAStatusTest {

  PrescriptionId mockTask(ErxPrescriptionBundle prescriptionBundle, Task.TaskStatus taskStatus) {
    val task = mock(ErxTask.class);
    when(prescriptionBundle.getTask()).thenReturn(task);
    when(prescriptionBundle.getTask().getStatus()).thenReturn(taskStatus);
    val prescriptionId = PrescriptionId.random();
    when(prescriptionBundle.getTask().getPrescriptionId()).thenReturn(prescriptionId);

    return prescriptionId;
  }

  static Stream<Arguments> evdgaStringProvider() {
    return Stream.of(
        Arguments.of("READY_FOR_REQUEST", EVDGAStatus.READY_FOR_REQUEST),
        Arguments.of("WAITING_OR_ACCEPTED", EVDGAStatus.WAITING_OR_ACCEPTED),
        Arguments.of("DECLINED", EVDGAStatus.DECLINED),
        Arguments.of("GRANTED", EVDGAStatus.GRANTED),
        Arguments.of("DOWNLOADED", EVDGAStatus.DOWNLOADED),
        Arguments.of("ACTIVATED", EVDGAStatus.ACTIVATED),
        Arguments.of("DELETED", EVDGAStatus.DELETED));
  }

  static Stream<Arguments> evdgaStatusProvider() {
    return Stream.of(
        Arguments.of(EVDGAStatus.READY_FOR_REQUEST, Task.TaskStatus.READY),
        Arguments.of(EVDGAStatus.WAITING_OR_ACCEPTED, Task.TaskStatus.INPROGRESS),
        Arguments.of(EVDGAStatus.DECLINED, Task.TaskStatus.COMPLETED),
        Arguments.of(EVDGAStatus.GRANTED, Task.TaskStatus.COMPLETED),
        Arguments.of(EVDGAStatus.DOWNLOADED, Task.TaskStatus.COMPLETED),
        Arguments.of(EVDGAStatus.ACTIVATED, Task.TaskStatus.COMPLETED),
        Arguments.of(EVDGAStatus.DELETED, Task.TaskStatus.CANCELLED),
        Arguments.of(EVDGAStatus.NULL, Task.TaskStatus.NULL));
  }

  static Stream<Arguments> taskStatusProvider() {
    return Stream.of(
        Arguments.of(Task.TaskStatus.READY, EVDGAStatus.READY_FOR_REQUEST),
        Arguments.of(Task.TaskStatus.INPROGRESS, EVDGAStatus.WAITING_OR_ACCEPTED),
        Arguments.of(Task.TaskStatus.COMPLETED, EVDGAStatus.DECLINED),
        Arguments.of(Task.TaskStatus.COMPLETED, EVDGAStatus.GRANTED),
        Arguments.of(Task.TaskStatus.CANCELLED, EVDGAStatus.DELETED),
        Arguments.of(Task.TaskStatus.NULL, EVDGAStatus.NULL));
  }

  @ParameterizedTest
  @MethodSource("evdgaStringProvider")
  void shouldMatchStringsToStatus(String inputString, EVDGAStatus expectedStatus) {
    val actualStatus = EVDGAStatus.fromString(inputString);
    assertEquals(expectedStatus, actualStatus);
  }

  @Test
  void shouldThrowOnStringMismatch() {
    assertThrows(NoSuchElementException.class, () -> EVDGAStatus.fromString("PAYBACK-PUNKTE"));
  }

  @ParameterizedTest
  @MethodSource("evdgaStatusProvider")
  void shouldMatchStatusToTaskStatus(EVDGAStatus inputStatus, Task.TaskStatus expectedStatus) {
    val actualStatus = EVDGAStatus.toCorrespondingStatus(inputStatus);
    assertEquals(expectedStatus, actualStatus);
  }

  @ParameterizedTest
  @MethodSource("taskStatusProvider")
  void shouldMatchTaskstatusToStatus(Task.TaskStatus inputStatus, EVDGAStatus expectedStatus) {
    val dispenseBundle = mock(ErxMedicationDispenseBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val prescriptionId = mockTask(prescriptionBundle, inputStatus);
    val dispense = mock(ErxMedicationDispense.class);
    GemErpMedication medication = null;

    val dispensePair = Pair.of(dispense, medication);
    List<Pair<ErxMedicationDispense, GemErpMedication>> list =
        Collections.singletonList(dispensePair);
    when(dispenseBundle.getDispensePairBy(prescriptionId)).thenReturn(list);

    val code = mock(RedeemCode.class);
    val redeemCode = spy(Optional.of(code));
    when(dispense.getRedeemCode()).thenReturn(redeemCode);

    if (expectedStatus.equals(EVDGAStatus.GRANTED)) {
      when(redeemCode.isPresent()).thenReturn(true);
    } else {
      when(redeemCode.isPresent()).thenReturn(false);
    }

    val actualStatus = EVDGAStatus.fromBackendStatus(dispenseBundle, prescriptionBundle);
    assertEquals(expectedStatus, actualStatus);
  }

  @Test
  void shouldThrowErrorOnDispenseBundleMismatch() {
    val dispenseBundle = mock(ErxMedicationDispenseBundle.class);
    val prescriptionBundle = mock(ErxPrescriptionBundle.class);
    val prescriptionId = mockTask(prescriptionBundle, Task.TaskStatus.READY);
    val dispense = mock(ErxMedicationDispense.class);
    val medication = mock(GemErpMedication.class);

    val dispensePair = Pair.of(dispense, medication);
    List<Pair<ErxMedicationDispense, GemErpMedication>> list =
        Collections.singletonList(dispensePair);
    when(dispenseBundle.getDispensePairBy(prescriptionId)).thenReturn(list);

    assertThrows(
        IllegalArgumentException.class,
        () -> EVDGAStatus.fromBackendStatus(dispenseBundle, prescriptionBundle));
  }
}
