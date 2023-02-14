/*
 * Copyright (c) 2023 gematik GmbH
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

import de.gematik.test.erezept.ErpConfiguration;
import de.gematik.test.erezept.screenplay.abilities.*;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ActorDecoratorTest {

  @Test
  void decorateDoctorActor() {
    val doctorName = "Bernd Claudius";
    val config = ErpConfiguration.create();
    config.getDoctorConfig(doctorName).setKonnektor("Soft-Konn");
    val doctor = new DoctorActor(doctorName);

    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      assertDoesNotThrow(() -> ActorDecorator.decorateDoctorActor(doctor, config));
      assertNotNull(doctor.abilityTo(UseTheErpClient.class));
      assertNotNull(doctor.abilityTo(UseSMCB.class));
      assertNotNull(doctor.abilityTo(UseTheKonnektor.class));
      assertNotNull(doctor.abilityTo(ProvideDoctorBaseData.class));
      assertNotNull(doctor.getDescription());
    }
  }

  @Test
  void decoratePharmacyActor() {
    val pharmacyName = "Am Flughafen";
    val config = ErpConfiguration.create();
    config.getPharmacyConfig(pharmacyName).setKonnektor("Soft-Konn");
    val doctor = new PharmacyActor(pharmacyName);

    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      assertDoesNotThrow(() -> ActorDecorator.decoratePharmacyActor(doctor, config));
      assertNotNull(doctor.abilityTo(UseTheErpClient.class));
      assertNotNull(doctor.abilityTo(UseSMCB.class));
      assertNotNull(doctor.abilityTo(UseTheKonnektor.class));
      assertNotNull(doctor.getDescription());
    }
  }

  @Test
  void decoratePatientActor() {
    val patientName = "Fridolin Straßer";
    val config = ErpConfiguration.create();
    val doctor = new PatientActor(patientName);

    try (MockedStatic<UseTheErpClient> useTheErpClientMockedStatic =
        Mockito.mockStatic(UseTheErpClient.class)) {
      val ability = mock(UseTheErpClient.class);
      useTheErpClientMockedStatic.when(() -> UseTheErpClient.with(any())).thenReturn(ability);

      assertDoesNotThrow(() -> ActorDecorator.decoratePatientActor(doctor, config));
      assertNotNull(doctor.abilityTo(UseTheErpClient.class));
      assertNotNull(doctor.abilityTo(ProvidePatientBaseData.class));
      assertNotNull(doctor.getDescription());
    }
  }
}
