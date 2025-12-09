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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.exceptions.MissingCacheException;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ActorStageTest {

  private static ActorStage stage;

  @BeforeAll
  static void setup() {
    StopwatchProvider.init();
    stage = new ActorStage();
  }

  @AfterAll
  static void tearDown() {
    stage.drawTheCurtain();
  }

  @Test
  void getDoctorNamed() {
    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      val doctor = assertDoesNotThrow(() -> stage.getDoctorNamed("Adelheid Ulmenwald"));
      val doctor2 = stage.getDoctorNamed("Adelheid Ulmenwald");

      // make sure the actor is instantiated only once
      assertEquals(doctor, doctor2);
    }
  }

  @Test
  void getPatientNamed() {
    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);

      val patient = assertDoesNotThrow(() -> stage.getPatientNamed("Fridolin Straßer"));
      val patient2 = stage.getPatientNamed("Fridolin Straßer");

      // make sure the actor is instantiated only once
      assertEquals(patient, patient2);
    }
  }

  @Test
  void getPharmacyNamed() {
    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      val pharmacy = assertDoesNotThrow(() -> stage.getPharmacyNamed("Am Flughafen"));
      val pharmacy2 = stage.getPharmacyNamed("Am Flughafen");

      // make sure the actor is instantiated only once
      assertEquals(pharmacy, pharmacy2);
    }
  }

  @Test
  void getKtrNamed() {
    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      val ktr = assertDoesNotThrow(() -> stage.getKtrNamed("AOK Bremen"));
      val ktr2 = stage.getKtrNamed("AOK Bremen");

      // make sure the actor is instantiated only once
      assertEquals(ktr, ktr2);
      assertNotNull(ktr.abilityTo(UseTheErpClient.class));
      assertNotNull(ktr.abilityTo(UseTheKonnektor.class));
      assertNotNull(ktr.abilityTo(UseSMCB.class));
    }
  }

  @Test
  void shouldThrowOnUnsupportedActorType() {
    assertThrows(
        MissingCacheException.class,
        () -> stage.instrumentNewActor(MyTestActor.class, "Leonie Hütter"));
  }

  private static class MyTestActor extends ErpActor {

    protected MyTestActor() {
      super(ActorType.PATIENT, "test");
    }
  }
}
