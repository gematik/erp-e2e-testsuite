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

package de.gematik.test.erezept;

import static de.gematik.test.utils.ActorStageUtil.createMockedErpFdTestsuiteFactory;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.abilities.*;
import de.gematik.test.erezept.actors.*;
import de.gematik.test.erezept.client.testutils.VauCertificateGenerator;
import de.gematik.test.erezept.common.exceptions.MissingAbilityException;
import de.gematik.test.erezept.common.screenplay.abilities.*;
import de.gematik.test.erezept.common.screenplay.util.SafeAbility;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.eml.fhir.EpaFhirFactory;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.ValidatorType;
import de.gematik.test.erezept.fhir.testutil.ErpFhirBuildingTest;
import de.gematik.test.erezept.screenplay.abilities.*;
import java.util.List;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ErpFdTestsuiteFactoryTest extends ErpFhirBuildingTest {

  private static ErpFdTestsuiteFactory config;

  @BeforeAll
  static void init() {
    StopwatchProvider.init();
    config = createMockedErpFdTestsuiteFactory(mock(FhirCodec.class));
  }

  @Test
  void shouldEquipDoctorActor() {
    val doctorName = "Adelheid Ulmenwald";
    val doctor = new DoctorActor(doctorName);

    assertDoesNotThrow(() -> config.equipAsDoctor(doctor));
    assertNotNull(doctor.abilityTo(UseTheErpClient.class));
    assertNotNull(doctor.abilityTo(UseSMCB.class));
    assertNotNull(doctor.abilityTo(UseTheKonnektor.class));
    assertNotNull(doctor.abilityTo(ProvideDoctorBaseData.class));
    assertNotNull(doctor.getDescription());
  }

  @Test
  void shouldEquipActorWithEpaMockClient() {
    val actorName = "Fridolin Straßer";
    val actor = new PatientActor(actorName);

    // creating the real EpaFhir codec is expensive: mock the static factory for this test
    try (val epaFhirFactory = mockStatic(EpaFhirFactory.class)) {
      epaFhirFactory.when(EpaFhirFactory::create).thenReturn(mock(FhirCodec.class));

      assertDoesNotThrow(() -> config.equipWithEpaMockClient(actor));
      epaFhirFactory.verify(EpaFhirFactory::create);
    }
    assertNotNull(actor.abilityTo(UseTheEpaMockClient.class));
  }

  @Test
  void shouldEquipActorWithTRegisterMockClient() {
    val actor = new GemaTestActor("Holly Goodhead");

    assertDoesNotThrow(() -> config.equipWithTPrescriptionMockClient(actor));
    assertNotNull(actor.abilityTo(UseTheTRegisterMockClient.class));
  }

  @Test
  void shouldEquipActorWithTRegisterMockClientWhenProxyIsSet() {
    val actor = new GemaTestActor("Holly Goodhead");

    System.setProperty("https.gematikProxy", "localhost");

    try {
      assertDoesNotThrow(() -> config.equipWithTPrescriptionMockClient(actor));
      assertNotNull(actor.abilityTo(UseTheTRegisterMockClient.class));
    } finally {
      System.clearProperty("https.gematikProxy");
    }
  }

  @Test
  void shouldEquipActorWithBricksFhirValidator() {
    val actor = new GemaTestActor("Holly Goodhead");

    // mock the expensive FhirParser construction and capture the constructor arguments
    try (val fhirParserConstruction =
        mockConstruction(
            FhirParser.class,
            (mock, context) ->
                assertEquals(ValidatorType.BRICKS, context.arguments().getFirst()))) {

      assertDoesNotThrow(() -> config.equipWithTPrescriptionMockClient(actor));

      assertEquals(1, fhirParserConstruction.constructed().size());
    }
  }

  @Test
  void shouldEquipPharmacyActor() {
    val pharmacyName = "Am Flughafen";
    val pharmacy = new PharmacyActor(pharmacyName);

    assertDoesNotThrow(() -> config.equipAsPharmacy(pharmacy));
    assertNotNull(pharmacy.abilityTo(UseTheErpClient.class));
    assertNotNull(pharmacy.abilityTo(UseSMCB.class));
    assertNotNull(pharmacy.abilityTo(UseTheKonnektor.class));
    assertNotNull(pharmacy.abilityTo(ManagePharmacyPrescriptions.class));
    assertNotNull(pharmacy.getDescription());
  }

  @Test
  void shouldEquipEuPharmacyActor() {
    val pharmacyName = "Hannes Vogt";
    val pharmacy = new EuPharmacyActor(pharmacyName);

    assertDoesNotThrow(() -> config.equipAsEuPharmacy(pharmacy));
    assertNotNull(pharmacy.abilityTo(UseTheErpClient.class));
    assertNotNull(pharmacy.abilityTo(UseSMCB.class));
    assertNotNull(pharmacy.getDescription());
  }

  @Test
  void shouldEquipPatientActor() {
    val patientName = "Fridolin Straßer";
    val patient = new PatientActor(patientName);

    assertDoesNotThrow(() -> config.equipAsPatient(patient));
    assertDoesNotThrow(patient::getEgk);
    assertNotNull(patient.abilityTo(UseTheErpClient.class));
    assertNotNull(patient.abilityTo(ProvidePatientBaseData.class));
    assertNotNull(patient.abilityTo(ProvideEGK.class));
    assertNotNull(patient.abilityTo(ManageDataMatrixCodes.class));
    assertNotNull(patient.abilityTo(ManageCommunications.class));
    assertNotNull(patient.getDescription());
    assertDoesNotThrow(patient::toString);
  }

  @Test
  void shouldEquipPatientActorWithRawHttp() {
    val actorName = "Fridolin Straßer";
    val patient = new PatientActor(actorName);

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

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(doc, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipWithRawHttp(doc));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(doc, RawHttpAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getFdBaseUrl());
  }

  @Test
  void shouldEquipPatientActorForTslDownload() {
    val actorName = "Fridolin Straßer";
    val patient = new PatientActor(actorName);

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(patient, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipForTslDownload(patient));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(patient, TSLAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getInternet().getTslBaseUrl());
  }

  @Test
  void shouldEquipPharmaActorForTslDownload() {
    val actorName = "Am Flughafen";
    val pharmacy = new PharmacyActor(actorName);

    assertThrows(
        MissingAbilityException.class,
        () -> SafeAbility.getAbility(pharmacy, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipForTslDownload(pharmacy));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(pharmacy, TSLAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getTslBaseUrl());
  }

  @Test
  void shouldEquipDoctorActorForTslDownload() {
    val actorName = "Adelheid Ulmenwald";
    val doc = new DoctorActor(actorName);

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(doc, RawHttpAbility.class));
    assertDoesNotThrow(() -> config.equipForTslDownload(doc));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(doc, TSLAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getTslBaseUrl());
  }

  @Test
  void shouldEquipAsApothecary() {
    val apothecaryName = "Finn-Louis Nullmayr";
    val apothecary = new Actor(apothecaryName); // currently no specific class for apothecary

    config.equipAsApothecary(apothecary);
    assertDoesNotThrow(() -> config.equipAsApothecary(apothecary));
    assertNotNull(apothecary.abilityTo(UseTheKonnektor.class));
  }

  @Test
  void shouldCreateVsdmService() {
    val vsdmService = assertDoesNotThrow(config::getSoftKonnVsdmService);
    assertNotNull(vsdmService);
  }

  @Test
  void shouldCreatePoPPServiceMock() {
    val vsdmService = assertDoesNotThrow(config::getPoppTokenGenerator);
    assertNotNull(vsdmService);
  }

  @Test
  void shouldEquipDoctorActorForOCSP() {
    val actorName = "Adelheid Ulmenwald";
    val actor = new DoctorActor(actorName);

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(actor, OCSPAbility.class));
    assertDoesNotThrow(() -> config.equipForOCSP(actor));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(actor, OCSPAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getFdBaseUrl());
  }

  @Test
  void shouldEquipPatientActorForOCSP() {
    val actor = new PatientActor("");

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(actor, OCSPAbility.class));
    assertDoesNotThrow(() -> config.equipForOCSP(actor));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(actor, OCSPAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getInternet().getFdBaseUrl());
  }

  @Test
  void shouldEquipPharmaActorForOCSP() {
    val actorName = "Am Flughafen";
    val actor = new PharmacyActor(actorName);

    assertThrows(
        MissingAbilityException.class, () -> SafeAbility.getAbility(actor, OCSPAbility.class));
    assertDoesNotThrow(() -> config.equipForOCSP(actor));
    assertDoesNotThrow(
        () -> SafeAbility.getAbility(actor, OCSPAbility.class).config().getDefaultBaseUrl());
    assertNotNull(config.getActiveEnvironment().getTi().getFdBaseUrl());
  }

  @Test
  void shouldFailWhenEquippingPatientActorWithUnknownActor() {
    val patient = new PatientActor("Fridolin");

    assertThrows(
        ConfigurationMappingException.class,
        () -> config.equipAsPatientWithInvalidIdpToken(patient));
  }

  @Test
  void shouldEquipPatientActorWithInvalidIdpToken() {
    val patientName = "Fridolin Straßer";
    val patient = new PatientActor(patientName);

    assertDoesNotThrow(() -> config.equipAsPatientWithInvalidIdpToken(patient));

    assertNotNull(patient.abilityTo(UseTheErpClient.class));
    assertNotNull(patient.abilityTo(ProvideEGK.class));
    assertNotNull(patient.abilityTo(ProvidePatientBaseData.class));
    assertNotNull(patient.abilityTo(ManageDataMatrixCodes.class));
    assertNotNull(patient.abilityTo(ManageCommunications.class));
  }

  @Test
  void shouldReturnOcspUrl() {
    val ti = new BackendRouteConfiguration();
    ti.setOcsp("http://ehca-testref.komp-ca.telematik-test/status/ecc-qocsp");

    val dummyVauCertificate = VauCertificateGenerator.generateRandomVauCertificateAsBase64();
    ti.setVauCertificate(dummyVauCertificate);

    val environment = new EnvironmentConfiguration();
    environment.setName("TU");
    environment.setTi(ti);

    val dto = new PrimsysConfigurationDto();
    dto.setActiveEnvironment("TU");
    dto.setEnvironments(List.of(environment));

    val config = ErpFdTestsuiteFactory.fromDto(dto, mock(SmartcardArchive.class));

    assertEquals("http://ehca-testref.komp-ca.telematik-test/status/ecc-qocsp", config.getOcsp());
  }
}
