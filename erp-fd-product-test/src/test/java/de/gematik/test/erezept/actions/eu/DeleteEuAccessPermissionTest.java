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

package de.gematik.test.erezept.actions.eu;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuGrantAccessDeleteCommand;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeleteEuAccessPermissionTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PatientActor patient;

  @BeforeEach
  void setup() {
    StopwatchProvider.init();
    mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    this.patient = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldDeleteEuAccessPermissionSuccessfully() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 204);
    when(erpClientMock.request(any(EuGrantAccessDeleteCommand.class))).thenReturn(mockResponse);

    assertDoesNotThrow(() -> patient.performs(DeleteEuAccessPermission.forOneSelf()));
  }

  @Test
  void shouldCreateInstance() {
    val action = DeleteEuAccessPermission.forOneSelf();
    assertNotNull(action, "Factory method must return non-null instance");
  }

  @Test
  void shouldBeSubtypeOfErpAction() {
    assertTrue(
        ErpAction.class.isAssignableFrom(DeleteEuAccessPermission.class),
        "DeleteEuAccessPermission must be an ErpAction");
  }
}
