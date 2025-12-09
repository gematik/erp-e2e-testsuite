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
import static org.mockito.Mockito.when;

import de.gematik.test.erezept.actions.ErpAction;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuGrantAccessGetCommand;
import de.gematik.test.erezept.fhir.r4.eu.EuAccessPermission;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;

class GetEuAccessPermissionTest {

  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;
  private PatientActor patientActor;

  @BeforeEach
  void setup() {
    mockUtil = new MockActorsUtils();
    this.erpClientMock = mockUtil.erpClientMock;
    this.patientActor = mockUtil.actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldGetEuAccessPermissionSuccessfully() {
    val euAccessPermission = new EuAccessPermission();
    val response = mockUtil.createErpResponse(euAccessPermission, EuAccessPermission.class, 200);

    when(erpClientMock.request(ArgumentMatchers.any(EuGrantAccessGetCommand.class)))
        .thenReturn(response);

    assertDoesNotThrow(() -> patientActor.performs(GetEuAccessPermission.forOneSelf()));
  }

  @Test
  void shouldCreateInstance() {
    val action = GetEuAccessPermission.forOneSelf();
    assertNotNull(action, "Factory method must return non-null instance");
  }

  @Test
  void shouldBeSubtypeOfErpAction() {
    assertTrue(
        ErpAction.class.isAssignableFrom(GetEuAccessPermission.class),
        "GetEuAccessPermission must be an ErpAction");
  }
}
