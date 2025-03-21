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

package de.gematik.test.erezept;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.exceptions.WebSocketException;
import de.gematik.test.erezept.pspwsclient.PSPClient;
import de.gematik.test.erezept.pspwsclient.config.PSPClientFactory;
import de.gematik.test.erezept.screenplay.abilities.DecideUserBehaviour;
import de.gematik.test.erezept.screenplay.abilities.ManageChargeItems;
import de.gematik.test.erezept.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManagePatientPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideApoVzdInformation;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.ReceiveDispensedDrugs;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import lombok.val;
import net.serenitybdd.junit5.SerenityJUnit5Extension;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

@ExtendWith(SerenityJUnit5Extension.class)
class PrimSysBddFactoryTest {

  private SmartcardArchive sca;
  private PrimSysBddFactory factory;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(new Cast() {});
    sca = SmartcardArchive.fromResources();
    factory =
        ConfigurationReader.forPrimSysConfiguration()
            .wrappedBy(dto -> PrimSysBddFactory.fromDto(dto, sca));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldEquipWithPspClient() {
    val name = factory.getDto().getActors().getPharmacies().get(0).getName();
    val pharmacy = new Actor(name);
    val smcb = sca.getSmcbCards().get(0);
    pharmacy.can(UseSMCB.itHasAccessTo(smcb));

    try (val mockedPspFactory = mockStatic(PSPClientFactory.class)) {
      val pspClient = mock(PSPClient.class);
      when(pspClient.isConnected()).thenReturn(true);
      mockedPspFactory.when(() -> PSPClientFactory.create(any(), any())).thenReturn(pspClient);

      assertDoesNotThrow(() -> factory.equipPharmacyWithPspClient(pharmacy));
    }
  }

  @Test
  void shouldEquipAsDoctor() {
    val name = factory.getDto().getActors().getDoctors().get(0).getName();
    val doctor = OnStage.theActorCalled(name);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> factory.equipAsDoctor(doctor));
      assertNotNull(doctor.abilityTo(UseTheKonnektor.class));
      assertNotNull(doctor.abilityTo(UseTheErpClient.class));
      assertNotNull(doctor.abilityTo(ProvideDoctorBaseData.class));
      assertNotNull(doctor.abilityTo(ManageDoctorsPrescriptions.class));
    }
  }

  @Test
  void shouldEquipAsPharmacy() {
    val name = factory.getDto().getActors().getPharmacies().get(0).getName();
    val pharmacy = OnStage.theActorCalled(name);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> factory.equipAsPharmacy(pharmacy));
      assertNotNull(pharmacy.abilityTo(UseTheKonnektor.class));
      assertNotNull(pharmacy.abilityTo(UseTheErpClient.class));
      assertNotNull(pharmacy.abilityTo(UseSubscriptionService.class));
      assertNotNull(pharmacy.abilityTo(UseSMCB.class));
      assertNotNull(pharmacy.abilityTo(ManagePharmacyPrescriptions.class));
      assertNotNull(pharmacy.abilityTo(ManageCommunications.class));
      assertNotNull(pharmacy.abilityTo(ProvideApoVzdInformation.class));
    }
  }

  @Test
  void shouldEquipAsHealthInsuranceInstitution() {
    val name = factory.getDto().getActors().getHealthInsurances().get(0).getName();
    val lei = OnStage.theActorCalled(name);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PsActorConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> factory.equipAsHealthInsurance(lei));
      assertNotNull(lei.abilityTo(UseTheKonnektor.class));
      assertNotNull(lei.abilityTo(UseTheErpClient.class));
      assertNotNull(lei.abilityTo(UseSMCB.class));
      assertNotNull(lei.abilityTo(ManagePharmacyPrescriptions.class));
      assertNotNull(lei.abilityTo(ManageCommunications.class));
    }
  }

  @Test
  void shouldEquipAsApothecary() {
    val name = factory.getDto().getActors().getApothecaries().get(0).getName();
    val apothecary = OnStage.theActorCalled(name);

    assertDoesNotThrow(() -> factory.equipAsApothecary(apothecary));
    assertNotNull(apothecary.abilityTo(UseTheKonnektor.class));
  }

  @ParameterizedTest(name = "Equip Actor as {0} Patient")
  @EnumSource(
      value = InsuranceTypeDe.class,
      names = {"GKV", "PKV"})
  void shouldEquipAsGkvPatient(InsuranceTypeDe insuranceType) {
    val name = factory.getDto().getActors().getPatients().get(0).getName();
    val patient = OnStage.theActorCalled(name);

    try (val erpClientFactoryMockedStatic = mockStatic(ErpClientFactory.class)) {
      val erpClient = mock(ErpClient.class);
      erpClientFactoryMockedStatic
          .when(() -> ErpClientFactory.createErpClient(any(), any(PatientConfiguration.class)))
          .thenReturn(erpClient);

      assertDoesNotThrow(() -> factory.equipAsPatient(patient, insuranceType.getCode()));
      assertNotNull(patient.abilityTo(UseTheErpClient.class));
      assertNotNull(patient.abilityTo(ProvideEGK.class));
      assertNotNull(patient.abilityTo(ManageDataMatrixCodes.class));
      assertNotNull(patient.abilityTo(ReceiveDispensedDrugs.class));
      assertNotNull(patient.abilityTo(ProvidePatientBaseData.class));
      assertNotNull(patient.abilityTo(ManagePatientPrescriptions.class));
      assertNotNull(patient.abilityTo(ManageCommunications.class));
      assertNotNull(patient.abilityTo(DecideUserBehaviour.class));

      if (insuranceType.equals(InsuranceTypeDe.PKV)) {
        assertNotNull(patient.abilityTo(ManageChargeItems.class));
      } else {
        assertNull(patient.abilityTo(ManageChargeItems.class));
      }
    }
  }

  @ParameterizedTest(name = "Equip Actor as {0} Patient for VSDM")
  @EnumSource(
      value = InsuranceTypeDe.class,
      names = {"GKV", "PKV"})
  void shouldEquipAsGkvPatientForVsdm(InsuranceTypeDe insuranceType) {
    val name = factory.getDto().getActors().getPatients().get(0).getName();
    val patient = OnStage.theActorCalled(name);

    assertDoesNotThrow(() -> factory.equipAsPatientForVsdm(patient, insuranceType.getCode()));

    assertNotNull(patient.abilityTo(ProvideEGK.class));
    assertNotNull(patient.abilityTo(ManageDataMatrixCodes.class));
    assertNotNull(patient.abilityTo(ReceiveDispensedDrugs.class));
    assertNotNull(patient.abilityTo(ProvidePatientBaseData.class));
    assertNotNull(patient.abilityTo(ManagePatientPrescriptions.class));
    assertNotNull(patient.abilityTo(ManageCommunications.class));
    assertNotNull(patient.abilityTo(DecideUserBehaviour.class));

    if (insuranceType.equals(InsuranceTypeDe.PKV)) {
      assertNotNull(patient.abilityTo(ManageChargeItems.class));
    } else {
      assertNull(patient.abilityTo(ManageChargeItems.class));
    }
  }

  @Test
  void shouldThrowWhenPspDoesNotConnect() {
    val name = factory.getDto().getActors().getPharmacies().get(0).getName();
    val pharmacy = OnStage.theActorCalled(name);
    pharmacy.can(UseSMCB.itHasAccessTo(sca.getSmcbCards().get(0)));

    // manipulate the psp address to provoke the exception
    factory.getDto().getPspClientConfig().setUrl("http://localhost:1234");

    assertThrows(WebSocketException.class, () -> factory.equipPharmacyWithPspClient(pharmacy));
  }
}
