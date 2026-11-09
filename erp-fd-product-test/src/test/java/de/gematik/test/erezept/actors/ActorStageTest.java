/*
 * Copyright (Change Date see Readme), gematik GmbH
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

import static org.junit.jupiter.api.Assertions.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.core.exceptions.MissingCacheException;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.utils.ActorStageUtil;
import lombok.val;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ActorStageTest extends ErpFhirParsingTest {

  private static ActorStage stage;

  @BeforeAll
  static void setup() {
    StopwatchProvider.init();
    stage = ActorStageUtil.createMockedActorStage(parser);
  }

  @AfterAll
  static void tearDown() {
    stage.drawTheCurtain();
  }

  @Test
  void getDoctorNamed() {
    val doctor = assertDoesNotThrow(() -> stage.getDoctorNamed("Adelheid Ulmenwald"));
    val doctor2 = assertDoesNotThrow(() -> stage.getDoctorNamed("Adelheid Ulmenwald"));

    // make sure the actor is instantiated only once
    assertEquals(doctor, doctor2);
  }

  @Test
  void getPatientNamed() {
    val patient = assertDoesNotThrow(() -> stage.getPatientNamed("Fridolin Straßer"));
    val patient2 = assertDoesNotThrow(() -> stage.getPatientNamed("Fridolin Straßer"));

    // make sure the actor is instantiated only once
    assertEquals(patient, patient2);
  }

  @Test
  void getPharmacyNamed() {
    val pharmacy = assertDoesNotThrow(() -> stage.getPharmacyNamed("Am Flughafen"));
    val pharmacy2 = assertDoesNotThrow(() -> stage.getPharmacyNamed("Am Flughafen"));

    // make sure the actor is instantiated only once
    assertEquals(pharmacy, pharmacy2);
  }

  @Test
  void getKtrNamed() {
    val ktr = assertDoesNotThrow(() -> stage.getKtrNamed("AOK Bremen"));
    val ktr2 = assertDoesNotThrow(() -> stage.getKtrNamed("AOK Bremen"));

    // make sure the actor is instantiated only once
    assertEquals(ktr, ktr2);
    assertNotNull(ktr.abilityTo(UseTheErpClient.class));
    assertNotNull(ktr.abilityTo(UseTheKonnektor.class));
    assertNotNull(ktr.abilityTo(UseSMCB.class));
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
