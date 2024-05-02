/*
 * Copyright 2023 gematik GmbH
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
 */

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.usecases.ConsentGetCommand;
import de.gematik.test.erezept.fhir.resources.erp.ErxConsentBundle;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReadConsentTest {
  private ErpClient erpClientMock;
  private MockActorsUtils mockUtil;

  private PatientActor patientActor;

  @BeforeEach
  void init() {
    StopwatchProvider.init();
    this.mockUtil = new MockActorsUtils();
    val actorStage = mockUtil.actorStage;
    this.erpClientMock = mockUtil.erpClientMock;
    patientActor = actorStage.getPatientNamed("Leonie Hütter");
  }

  @Test
  void shouldGetConsent() {
    val mockResponse = mockUtil.createErpResponse(null, ErxConsentBundle.class, 200);
    when(erpClientMock.request(any(ConsentGetCommand.class))).thenReturn(mockResponse);
    assertDoesNotThrow(() -> patientActor.performs(ReadConsent.forOneSelf()));
  }
}
