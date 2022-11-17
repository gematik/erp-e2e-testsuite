/*
 * Copyright (c) 2022 gematik GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the License);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.gematik.test.erezept.primsys.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.primsys.rest.data.AcceptData;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ActorContextTest {

  @Test
  void shouldInstantiateWithConfig() {
    System.setProperty("erp.config.actors.doctors.*.konnektor", "Soft-Konn");
    System.setProperty("erp.config.actors.pharmacies.*.konnektor", "Soft-Konn");

    try (MockedStatic<ErpClientFactory> erpClientFactoryMockedStatic =
        Mockito.mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any()))
          .thenReturn(erpClient);

      val ctx = ActorContext.getInstance();
      assertNotNull(ctx);
      assertFalse(ctx.getActors().isEmpty());
    }
  }

  @Test
  void shouldNotRemovePrescription() {
    AcceptData acceptData = new AcceptData();
    acceptData.setTaskId("160.000.166.678.325.82");
    acceptData.setAccessCode("133ff36cbb92784b1c372e1166f92290d83b98596a37ef133ec1fbae500fd1bf");
    acceptData.setSecret("dc2c283afae58da2e5249faac31644c8436f25aab0f3758faf736a14c2cb1d93");
    try (MockedStatic<ActorContext> mockedStaticActor = mockStatic(ActorContext.class)) {
      val mockActorContext = mock(ActorContext.class);
      mockedStaticActor.when(() -> ActorContext.getInstance()).thenReturn(mockActorContext);
      mockActorContext.addAcceptedPrescription(acceptData);
      assertFalse(ActorContext.getInstance().removeAcceptedPrescription(acceptData));
    }
  }
}