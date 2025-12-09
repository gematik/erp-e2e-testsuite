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

package de.gematik.test.erezept.actions;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.TaskGetByIdAsAcceptBundleCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.util.DmcPrescription;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadDiGATaskAsAcceptBundleTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();

    this.mockUtil = new MockActorsUtils();
    this.erpClientMock = mockUtil.erpClientMock;

    this.patientActor = mockUtil.actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldPerformReadDiGATaskSuccessfully() {
    val taskId = TaskId.from("162");
    val accessCode = AccessCode.from("0123456789");

    val mockResponse = mockUtil.createErpResponse(null, ErxAcceptBundle.class, 200);

    when(erpClientMock.request(any(TaskGetByIdAsAcceptBundleCommand.class)))
        .thenReturn(mockResponse);

    val action = new ReadDiGATaskAsAcceptBundle(taskId, accessCode);

    assertDoesNotThrow(() -> patientActor.performs(action));
  }

  @Test
  void shouldCreateInstanceFromDmc() {
    val dmc = mock(DmcPrescription.class);

    when(dmc.getTaskId()).thenReturn(TaskId.from("162"));
    when(dmc.getAccessCode()).thenReturn(AccessCode.from("0123456789"));

    val action = ReadDiGATaskAsAcceptBundle.fromDmc(dmc);

    assertNotNull(action);
  }
}
