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

package de.gematik.test.erezept;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.exceptions.MissingAbilityException;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ErpFdTestsuiteFactoryTest {

  @SneakyThrows
  @BeforeEach
  void init() {
    OnStage.setTheStage(new Cast() {});
    StopwatchProvider.init();
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldEquipDoctorActor() {
    val doctorName = "Adelheid Ulmenwald";
    val config = ErpFdTestsuiteFactory.create();
    val doctor = new DoctorActor(doctorName);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> config.equipAsDoctor(doctor));
      assertNotNull(doctor.abilityTo(UseTheErpClient.class));
      assertNotNull(doctor.abilityTo(UseSMCB.class));
      assertNotNull(doctor.abilityTo(UseTheKonnektor.class));
      assertNotNull(doctor.abilityTo(ProvideDoctorBaseData.class));
      assertNotNull(doctor.getDescription());
    }
  }

  @Test
  void shouldEquipPharmacyActor() {
    val pharmacyName = "Am Flughafen";
    val config = ErpFdTestsuiteFactory.create();
    val pharmacy = new PharmacyActor(pharmacyName);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> config.equipAsPharmacy(pharmacy));
      assertNotNull(pharmacy.abilityTo(UseTheErpClient.class));
      assertNotNull(pharmacy.abilityTo(UseSMCB.class));
      assertNotNull(pharmacy.abilityTo(UseTheKonnektor.class));
      assertNotNull(pharmacy.abilityTo(ManagePharmacyPrescriptions.class));
      assertNotNull(pharmacy.getDescription());
    }
  }

  @Test
  void shouldEquipPatientActor() {
    val patientName = "Fridolin Straßer";
    val config = ErpFdTestsuiteFactory.create();
    val patient = new PatientActor(patientName);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> config.equipAsPatient(patient));
      assertDoesNotThrow(patient::getEgk);
      assertNotNull(patient.abilityTo(UseTheErpClient.class));
      assertNotNull(patient.abilityTo(ProvidePatientBaseData.class));
      assertNotNull(patient.getDescription());
      assertDoesNotThrow(patient::toString);
    }
  }

  @Test
  void shouldEquipPatientActorWithRawHttp() {
    val actorName = "Fridolin Straßer";
    val patient = new PatientActor(actorName);
    val config = ErpFdTestsuiteFactory.create();

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(patient, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipWithRawHttp(patient));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(patient, RawHttpAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getInternet().getFdBaseUrl());
  }

  @Test
  void shouldEquipPharmaActorWithRawHttp() {
    val actorName = "Am Flughafen";
    val pharmacy = new PharmacyActor(actorName);
    val config = ErpFdTestsuiteFactory.create();

    assertThrows(
        MissingAbilityException.class,
        () -> SafeAbility.getAbility(pharmacy, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipWithRawHttp(pharmacy));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(pharmacy, RawHttpAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getFdBaseUrl());
  }

  @Test
  void shouldEquipDoctorActorWithRawHttp() {
    val actorName = "Adelheid Ulmenwald";
    val doc = new DoctorActor(actorName);
    val config = ErpFdTestsuiteFactory.create();

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(doc, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipWithRawHttp(doc));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(doc, RawHttpAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getFdBaseUrl());
  }

  @Test
  void shouldEquipAsApothecary() {
    val apothecaryName = "Amanda Albrecht";
    val config = ErpFdTestsuiteFactory.create();
    val apothecary = new Actor(apothecaryName); // currently no specific class for apothecary

    assertDoesNotThrow(() -> config.equipAsApothecary(apothecary));
    assertNotNull(apothecary.abilityTo(UseTheKonnektor.class));
  }

  @Test
  void shouldCreateVsdmService() {
    val config = ErpFdTestsuiteFactory.create();
    val vsdmService = assertDoesNotThrow(config::getSoftKonnVsdmService);
    assertNotNull(vsdmService);
  }
}
