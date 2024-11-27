/*
 * Copyright 2024 gematik GmbH
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

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.exceptions.WebSocketException;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.fhir.valuesets.VersicherungsArtDeBasis;
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
import de.gematik.test.erezept.screenplay.abilities.UsePspClient;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseSubscriptionService;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.erezept.screenplay.util.SafeAbility;
import de.gematik.test.konnektor.cfg.KonnektorModuleFactory;
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
    if (!pspClient.isConnected()) {
      throw new WebSocketException("PSP-Client could not connect to PSP-Server");
    }
    return pspClient;
  }

  public void equipAsDoctor(Actor theDoctor) {
    val name = theDoctor.getName();
    val cfg = this.getConfig(name, dto.getActors().getDoctors());
    val smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());
    val hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

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
        .can(ProvideDoctorBaseData.fromConfiguration(cfg, hba.getTelematikId()))
        .can(ManageDoctorsPrescriptions.sheIssued());
  }

  public void equipAsPharmacy(Actor thePharmacy) {
    val name = thePharmacy.getName();
    val cfg = this.getConfig(name, dto.getActors().getPharmacies());
    val smcb = sca.getSmcbByICCSN(cfg.getSmcbIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

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
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

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
    val name = thePatient.getName();
    val cfg = this.getConfig(name, dto.getActors().getPatients());
    val egkConfig =
        sca.getConfigsFor(SmartcardType.EGK).stream()
            .filter(c -> c.getIccsn().equals(cfg.getEgkIccsn()))
            .findFirst()
            .orElseThrow(() -> new CardNotFoundException(SmartcardType.EGK, cfg.getEgkIccsn()));
    val egk = DummyEgk.fromConfig(egkConfig);
    this.equipAsBasePatient(thePatient, egk, insuranceType);

    // give the patient a proper description
    givenThat(thePatient)
        .describedAs(
            format(
                "Ein {0} Krankenversicherter der E-Rezepte über 'eGK in der Apotheke' abholen kann",
                insuranceType));
  }

  public void equipAsPatient(Actor thePatient, String insuranceType) {
    val name = thePatient.getName();
    val cfg = this.getConfig(name, dto.getActors().getPatients());
    val egk = sca.getEgkByICCSN(cfg.getEgkIccsn());

    this.equipAsBasePatient(thePatient, egk, insuranceType);
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

  private void equipAsBasePatient(Actor thePatient, Egk egk, String insuranceType) {
    val name = thePatient.getName();

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
