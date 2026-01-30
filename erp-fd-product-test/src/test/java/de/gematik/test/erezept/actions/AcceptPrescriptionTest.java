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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.client.usecases.TaskAcceptCommand;
import de.gematik.test.erezept.fhir.r4.erp.ErxAcceptBundle;
import de.gematik.test.erezept.fhir.r4.erp.ErxTask;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.fhir.values.AccessCode;
import de.gematik.test.erezept.fhir.values.TaskId;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AcceptPrescriptionTest extends ErpFhirBuildingTest {

  private MockActorsUtils mockUtil;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
  }

  @Test
  void shouldRememberAcceptedPrescription() {
    val erpClientMock = mockUtil.erpClientMock;
    val acceptBundle = spy(new ErxAcceptBundle());
    val task = mock(ErxTask.class);
    doReturn(task).when(acceptBundle).getTask();
    when(task.getTaskId()).thenReturn(TaskId.random());
    when(task.getAccessCode()).thenReturn(AccessCode.random());

    val mockResponse = mockUtil.createErpResponse(acceptBundle, ErxAcceptBundle.class, 200);
    when(erpClientMock.request(any(TaskAcceptCommand.class))).thenReturn(mockResponse);

    val pharmacy = mockUtil.actorStage.getPharmacyNamed("Am Flughafen");
    assertDoesNotThrow(() -> pharmacy.performs(AcceptPrescription.forTheTask(task)));
    val accepted =
        pharmacy
            .abilityTo(ManagePharmacyPrescriptions.class)
            .getAcceptedPrescriptions()
            .getRawList();
    assertFalse(accepted.isEmpty());
  }
}
