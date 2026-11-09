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

package de.gematik.test.utils;

import static de.gematik.test.erezept.client.cfg.ErpClientFactoryUtil.createMockedErpClient;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.bbriccs.cfg.NamedConfigurationElement;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.KonnektorContextConfiguration;
import de.gematik.bbriccs.konnektor.cfg.SoftKonServiceConfiguration;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.test.erezept.abilities.ProvidePharmacyBaseData;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.EuPharmacyActor;
import de.gematik.test.erezept.actors.PatientActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.common.screenplay.abilities.ManageCommunications;
import de.gematik.test.erezept.common.screenplay.abilities.ManageDataMatrixCodes;
import de.gematik.test.erezept.common.screenplay.abilities.ManageDoctorsPrescriptions;
import de.gematik.test.erezept.common.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.common.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.common.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.common.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseHBA;
import java.util.List;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

/**
 * This utility class creates mock actors for testing purposes. It provides methods to create mock
 * instances of actors with predefined behaviors and configurations, allowing for controlled testing
 * scenarios without relying on actual implementations.
 */
public class ActorMockUtil extends ErpFhirParsingTest {

  public static final SmartcardArchive SMARTCARD_ARCHIVE = SmartcardArchive.fromResources();
  public static final PrimsysConfigurationDto CONFIG =
      ConfigurationReader.forPrimSysConfiguration().create();
  private static final Konnektor SOFT_KON = createSoftKonn();

  public static Pair<PatientActor, ErpClient> createRandomPatient() {
    val name = "Random Patient " + System.currentTimeMillis();
    val egkConf = new SmartcardConfigDto();
    egkConf.setType(SmartcardType.EGK);
    egkConf.setIccsn("RANDOM_ICCSN_" + System.currentTimeMillis());
    egkConf.setIdentifier(KVNR.randomStringValue());
    egkConf.setOwnerName(name);
    val egk = DummyEgk.fromConfig(egkConf);
    return createMockedPatientActor(name, egk);
  }

  public static Pair<PatientActor, ErpClient> createPatientNamed(String name) {
    val pc = getConfig(name, CONFIG.getActors().getPatients());
    val egk = SMARTCARD_ARCHIVE.getEgkByICCSN(pc.getEgkIccsn());

    return createMockedPatientActor(name, egk);
  }

  private static Pair<PatientActor, ErpClient> createMockedPatientActor(String name, Egk egk) {
    val erpClient = createMockedErpClient(parser);
    val patientActor = new PatientActor(name);

    givenThat(patientActor)
        .whoCan(UseTheErpClient.with(erpClient))
        .can(ProvidePatientBaseData.forGkvPatient(KVNR.from(egk.getKvnr()), name))
        .can(ProvideEGK.sheOwns(egk))
        .can(ManageDataMatrixCodes.heGetsPrescribed())
        .can(ManageCommunications.heExchanges());

    return Pair.of(patientActor, erpClient);
  }

  public static Pair<PharmacyActor, ErpClient> createRandomPharmacy() {
    val name = "Random Pharmacy " + System.currentTimeMillis();
    val pharmacyConfig = CONFIG.getActors().getPharmacies().getFirst();

    val smcb = SMARTCARD_ARCHIVE.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());
    val erpClient = createMockedErpClient(parser);

    val pharmacyActor = new PharmacyActor(name);
    givenThat(pharmacyActor)
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(UseTheKonnektor.with(smcb).on(SOFT_KON))
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges())
        //        .can(UsePoppTokenGenerator.with(getPoppTokenGenerator(), smcb))
        .can(
            ProvidePharmacyBaseData.forNationalPharmacy()
                .practitionerIdentifier("telematik-id HBA")
                .organizationIdentifier(smcb)
                .build())
        .can(UseTheErpClient.with(erpClient));
    return Pair.of(pharmacyActor, erpClient);
  }

  public static Pair<DoctorActor, ErpClient> createRandomDoctor() {
    val name = "Random Doctor " + System.currentTimeMillis();
    val doctorConfig = CONFIG.getActors().getDoctors().getFirst();

    val smcb = SMARTCARD_ARCHIVE.getSmcbByICCSN(doctorConfig.getSmcbIccsn());
    val hba = SMARTCARD_ARCHIVE.getHbaByICCSN(doctorConfig.getHbaIccsn());
    val erpClient = createMockedErpClient(parser);

    val doctorActor = new DoctorActor(name);
    givenThat(doctorActor)
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        //        .can(UsePoppTokenGenerator.with(getPoppTokenGenerator(), smcb))
        .can(UseHBA.itHasAccessTo(hba))
        .can(UseTheKonnektor.with(smcb).and(hba).on(SOFT_KON))
        .can(UseTheErpClient.with(erpClient))
        .can(ManageDoctorsPrescriptions.heIssued())
        .can(ProvideDoctorBaseData.fromConfiguration(doctorConfig, hba.getTelematikId()));

    return Pair.of(doctorActor, erpClient);
  }

  public static Pair<EuPharmacyActor, ErpClient> createRandomEuPharmacy(String countryCode) {
    val name = "Random EU Pharmacy " + countryCode + " " + System.currentTimeMillis();
    val pharmacyConfig = CONFIG.getActors().getEuPharmacies().getFirst();
    val euPharmacyActor = new EuPharmacyActor(name);
    val erpClient = createMockedErpClient(parser);
    val smcb = SMARTCARD_ARCHIVE.getSmcbByICCSN(pharmacyConfig.getSmcbIccsn());

    euPharmacyActor.can(UseTheErpClient.with(erpClient));

    EuPharmacyConfiguration cfg = new EuPharmacyConfiguration();
    cfg.setCountryCode(countryCode);
    euPharmacyActor.can(ProvidePharmacyBaseData.fromConfiguration(cfg));
    euPharmacyActor.whoCan(UseSMCB.itHasAccessTo(smcb));

    return Pair.of(euPharmacyActor, erpClient);
  }

  private static Konnektor createSoftKonn() {
    val config = new KonnektorConfiguration();
    config.setName("Test SoftKon");
    config.setContext(KonnektorContextConfiguration.getDefaultContextConfiguration());

    val service = new SoftKonServiceConfiguration();
    service.setType("Soft-Kon");
    config.setService(service);
    return Konnektor.create(config);
  }

  private static <T extends NamedConfigurationElement> T getConfig(String name, List<T> configs) {
    return configs.stream()
        .filter(actor -> actor.getName().equalsIgnoreCase(name))
        .findFirst()
        .orElseThrow(() -> new ConfigurationMappingException(name, configs));
  }
}
