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

package de.gematik.test.erezept.primsys;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import lombok.val;
import org.junit.jupiter.api.Test;

class PrimSysRestFactoryTest extends TestWithActorContext {

  @Test
  void shouldReadDefaultEnvironment() {
    val factory = PrimSysRestFactory.fromDto(configDto, sca);
    val activeEnv = assertDoesNotThrow(factory::getActiveEnvironment);
    assertEquals(configDto.getActiveEnvironment(), activeEnv.getName());
  }

  @Test
  void shouldCreateDoctors() {
    val factory = PrimSysRestFactory.fromDto(configDto, sca);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(mock(ErpClient.class));

      val docs = assertDoesNotThrow(factory::createDoctorActors);
      assertEquals(configDto.getActors().getDoctors().size(), docs.size());

      val pharmacies = assertDoesNotThrow(factory::createPharmacyActors);
      assertEquals(configDto.getActors().getPharmacies().size(), pharmacies.size());

      val ktrs = assertDoesNotThrow(factory::createHealthInsuranceActors);
      assertEquals(configDto.getActors().getHealthInsurances().size(), ktrs.size());
    }
  }
}
