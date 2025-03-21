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
 */

package de.gematik.test.erezept.actions;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.fhir.r4.erp.ErxCommunication;
import lombok.val;
import org.junit.jupiter.api.Test;

class MockActorsUtilForTest {

  @Test
  void shouldGenerateMockResponse() {
    val payloadMock = mock(ErxCommunication.class);
    val response = new MockActorsUtils().createErpResponse(payloadMock, ErxCommunication.class);
    assertNotNull(response);
    assertEquals(payloadMock, response.getExpectedResource());
    assertTrue(response.isValidPayload());
    assertEquals(200, response.getStatusCode());
  }

  @Test
  void shouldBuildDefaultContext() {
    val utilMock = new MockActorsUtils();
    assertNotNull(utilMock.actorStage);
    assertNotNull(utilMock.erpClientMock);
  }
}
