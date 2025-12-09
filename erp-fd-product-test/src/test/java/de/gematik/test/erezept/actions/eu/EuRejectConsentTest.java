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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actions.MockActorsUtils;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.eu.EuConsentDeleteCommand;
import lombok.val;
import org.hl7.fhir.r4.model.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EuRejectConsentTest {

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
  void shouldNotFailWithFalseEnsure() {
    val mockResponse = mockUtil.createErpResponse(null, Resource.class, 204);
    when(erpClientMock.request(any())).thenReturn(mockResponse);

    val action = new EuRejectConsent(new EuConsentDeleteCommand(), false);
    assertDoesNotThrow(() -> patient.performs(action));
  }
}
