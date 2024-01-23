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

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
import de.gematik.test.erezept.pspwsclient.config.PSPClientFactory;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.cfg.KonnektorModuleFactory;
import de.gematik.test.smartcard.Algorithm;
import de.gematik.test.smartcard.SmartcardArchive;
import de.gematik.test.smartcard.SmcB;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

/** Configured Factory for the PrimSys-BDD (a.k.a. E2E)-Testsuite */
@Slf4j
public class PrimSysBddFactory extends ConfiguredFactory {

  @Getter private final PrimsysConfigurationDto dto;
  private final SmartcardArchive sca;
  private final KonnektorModuleFactory konnektorFactory;

  public PrimSysBddFactory(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    this.dto = dto;
    this.sca = sca;
    this.konnektorFactory = KonnektorModuleFactory.fromKonnektorConfigs(dto.getKonnektors());
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return this.getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public void equipPharmacyWithPspClient(Actor thePharmacy) {
    val smcbAbility = SafeAbility.getAbility(thePharmacy, UseSMCB.class);
    val pspClientAbility = this.createPspClientFor(smcbAbility.getSmcB());
    givenThat(thePharmacy).can(pspClientAbility);
  }

  public UsePspClient createPspClientFor(SmcB smcb) {
    val telematikId = smcb.getTelematikId();
    val pspClient =
        UsePspClient.with(PSPClientFactory.create(dto.getPspClientConfig(), telematikId))
            .andConfig(dto.getPspClientConfig());

    // check psp connected successfully
    assertTrue(pspClient.isConnected());
    return pspClient;
  }

  public void equipAsDoctor(Actor theDoctor) {
    val name = theDoctor.getName();
    val cfg = this.getConfig(name, dto.getActors().getDoctors());
    val smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());
    val hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = Algorithm.fromString(cfg.getAlgorithm());

    val useTheKonnektor =
        UseTheKonnektor.with(smcb)
            .and(hba)
            .and(algorithm)
            .on(konnektorFactory.createKonnektorClient(cfg.getKonnektor()));
    val erpClient = ErpClientFactory.createErpClient(this.getActiveEnvironment(), cfg);

    // equip the doctor with abilities
    givenThat(theDoctor)
        .describedAs(cfg.getDescription())
        .whoCan(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient).authenticatingWith(useTheKonnektor))
        .can(ProvideDoctorBaseData.fromConfiguration(cfg))
        .can(ManageDoctorsPrescriptions.sheIssued());
  }

  public void equipAsPharmacy(Actor thePharmacy) {
    val name = thePharmacy.getName();
    val cfg = this.getConfig(name, dto.getActors().getPharmacies());
    val smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());
    val algorithm = Algorithm.fromString(cfg.getAlgorithm());

    val useTheKonnektor =
        UseTheKonnektor.with(smcb)
            .and(algorithm)
            .on(konnektorFactory.createKonnektorClient(cfg.getKonnektor()));
    val erpClient = ErpClientFactory.createErpClient(this.getActiveEnvironment(), cfg);

    givenThat(thePharmacy)
        .describedAs(cfg.getDescription())
        .whoCan(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient).authenticatingWith(useTheKonnektor))
        .can(UseSubscriptionService.use())
        .can(UseSMCB.itHasAccessTo(smcb))
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges())
        .can(ProvideApoVzdInformation.withName(cfg.getApoVzdName()));
  }

  public void equipAsApothecary(Actor theApothecary) {
    val name = theApothecary.getName();
    val cfg = this.getConfig(name, dto.getActors().getApothecaries());
    val hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = Algorithm.fromString(cfg.getAlgorithm());

    // equip the apothecary with a konnektor-client ability
    givenThat(theApothecary)
        .describedAs(cfg.getDescription())
        .whoCan(
            UseTheKonnektor.with(hba)
                .and(algorithm)
                .on(konnektorFactory.createKonnektorClient(cfg.getKonnektor())));
  }

  /**
   * The Patient will be equipped with all required abilities except of the erp-client because for
   * VSDM use-cases the patient does not actively communicate to the E-Rezepte Fachdienst
   *
   * @param thePatient is the actor to be equipped as a patient without erp-client ability
   */
  public void equipAsPatientForVsdm(Actor thePatient, String insuranceType) {
    this.equipAsBasePatient(thePatient, insuranceType);

    // give the patient a proper description
    givenThat(thePatient)
        .describedAs(
            format(
                "Ein {0} Krankenversicherter der E-Rezepte über 'eGK in der Apotheke' abholen kann",
                insuranceType));
  }

  public void equipAsPatient(Actor thePatient, String insuranceType) {
    this.equipAsBasePatient(thePatient, insuranceType);
    val name = thePatient.getName();
    val cfg = this.getConfig(name, dto.getActors().getPatients());
    val egk = sca.getEgkByICCSN(cfg.getEgkIccsn());

    givenThat(thePatient)
        .describedAs(
            format(
                "Ein {0} Krankenversicherter der E-Rezepte verschrieben bekommt und in Apotheken"
                    + " einlöst",
                insuranceType))
        .can(
            UseTheErpClient.with(ErpClientFactory.createErpClient(this.getActiveEnvironment(), cfg))
                .authenticatingWith(egk));
  }

  private void equipAsBasePatient(Actor thePatient, String insuranceType) {
    val name = thePatient.getName();
    val cfg = this.getConfig(name, dto.getActors().getPatients());
    val egk = sca.getEgkByICCSN(cfg.getEgkIccsn());

    givenThat(thePatient)
        .whoCan(ProvideEGK.sheOwns(egk))
        .can(ManageDataMatrixCodes.heGetsPrescribed())
        .can(ReceiveDispensedDrugs.forHimself())
        .can(ProvidePatientBaseData.forPatient(KVNR.from(egk.getKvnr()), name, insuranceType))
        .can(ManagePatientPrescriptions.heReceived())
        .can(ManageCommunications.heExchanges())
        .can(DecideUserBehaviour.withGiven(dto.isPreferManualSteps()));

    if (VersicherungsArtDeBasis.fromCode(insuranceType).equals(VersicherungsArtDeBasis.PKV)) {
      givenThat(thePatient).can(ManageChargeItems.heReceives());
    }
  }

  public static PrimSysBddFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    return new PrimSysBddFactory(dto, sca);
  }
}
