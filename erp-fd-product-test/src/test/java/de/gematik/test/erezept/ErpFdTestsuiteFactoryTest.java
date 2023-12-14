package de.gematik.test.erezept;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.screenplay.abilities.*;
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
