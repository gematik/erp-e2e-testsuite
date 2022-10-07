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

package de.gematik.test.erezept.actors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.lei.cfg.TestsuiteConfiguration;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ActorStageTest {

  private static ActorStage stage;

  @BeforeAll
  static void setup() {
    val cfg = TestsuiteConfiguration.getInstance();
    val actors = cfg.getActors();
    actors.getDoctors().forEach(doc -> doc.setKonnektor("Soft-Konn"));
    actors.getPharmacies().forEach(pharm -> pharm.setKonnektor("Soft-Konn"));
    stage = new ActorStage();
  }

  @Test
  void getDoctorNamed() {
    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      val doctor = assertDoesNotThrow(() -> stage.getDoctorNamed("Bernd Claudius"));
      val doctor2 = stage.getDoctorNamed("Bernd Claudius");

      // make sure the actor is instantiated only once
      assertEquals(doctor, doctor2);
    }
  }

  @Test
  void getPatientNamed() {
    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      val patient = assertDoesNotThrow(() -> stage.getPatientNamed("Fridolin Straßer"));
      val patient2 = stage.getPatientNamed("Fridolin Straßer");

      // make sure the actor is instantiated only once
      assertEquals(patient, patient2);
    }
  }

  @Test
  void getPharmacyNamed() {
    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      val pharmacy = assertDoesNotThrow(() -> stage.getPharmacyNamed("Am Flughafen"));
      val pharmacy2 = stage.getPharmacyNamed("Am Flughafen");

      // make sure the actor is instantiated only once
      assertEquals(pharmacy, pharmacy2);
    }
  }
}
