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

package de.gematik.test.erezept.common;

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.fhir.de.valueset.InsuranceTypeDe;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.common.screenplay.abilities.*;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.screenplay.abilities.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

/** Configured Factory for the PrimSys-BDD (a.k.a. E2E)-Testsuite */
@Slf4j
public class PrimSysBddFactory extends ConfiguredFactory {

  @Getter private final PrimsysConfigurationDto dto;
  private final SmartcardArchive sca;
  @Getter private final EnvironmentConfiguration activeEnvironment;
  private final ErpClientFactory erpClientFactory;

  public PrimSysBddFactory(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    this.dto = dto;
    this.sca = sca;
    this.activeEnvironment = getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
    this.erpClientFactory =
        ErpClientFactory.forEnvironment(activeEnvironment).withSmartcardArchive(sca).build();
  }

  public void equipAsDoctor(Actor theDoctor) {
    val name = theDoctor.getName();
    val cfg = getConfig(name, dto.getActors().getDoctors());
    val hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb)
            .and(hba)
            .and(algorithm)
            .on(instantiateKonnektor(cfg.getKonnektor()));

    // as PS we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    // equip the doctor with abilities
    givenThat(theDoctor)
        .describedAs(cfg.getDescription())
        .whoCan(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient))
        .can(UseSMCB.itHasAccessTo(smcb))
        .can(ProvideDoctorBaseData.fromConfiguration(cfg, hba.getTelematikId()))
        .can(ManageDoctorsPrescriptions.sheIssued());
  }

  public void equipAsPharmacy(Actor thePharmacy) {
    val name = thePharmacy.getName();
    val cfg = getConfig(name, dto.getActors().getPharmacies());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(algorithm).on(instantiateKonnektor(cfg.getKonnektor()));

    // as PS we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    givenThat(thePharmacy)
        .describedAs(cfg.getDescription())
        .whoCan(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient))
        .can(UseSubscriptionService.use())
        .can(UseSMCB.itHasAccessTo(smcb))
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges())
        .can(ProvideApoVzdInformation.withName(cfg.getApoVzdName()));
  }

  public void equipAsKtr(Actor ktr) {
    val name = ktr.getName();
    val cfg = getConfig(name, dto.getActors().getHealthInsurances());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(algorithm).on(instantiateKonnektor(cfg.getKonnektor()));

    // as PS we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    givenThat(ktr)
        .whoCan(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient))
        .can(UseSubscriptionService.use())
        .can(UseSMCB.itHasAccessTo(smcb))
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges());
  }

  public void equipAsApothecary(Actor theApothecary) {
    val name = theApothecary.getName();
    val cfg = getConfig(name, dto.getActors().getApothecaries());
    val hba = sca.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // equip the apothecary with a konnektor-client ability
    givenThat(theApothecary)
        .describedAs(cfg.getDescription())
        .whoCan(
            UseTheKonnektor.with(hba).and(algorithm).on(instantiateKonnektor(cfg.getKonnektor())));
  }

  /**
   * The Patient will be equipped with all required abilities except of the erp-client because for
   * VSDM use-cases the patient does not actively communicate to the E-Rezepte Fachdienst
   *
   * @param thePatient is the actor to be equipped as a patient without erp-client ability
   */
  public void equipAsPatientForVsdm(Actor thePatient, String insuranceType) {
    val name = thePatient.getName();
    val cfg = getConfig(name, dto.getActors().getPatients());
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
    val cfg = getConfig(name, dto.getActors().getPatients());

    val p = erpClientFactory.prepareStagedClientFor(cfg);
    val erpClient = p.getLeft();
    val egk = p.getRight();

    this.equipAsBasePatient(thePatient, egk, insuranceType);
    givenThat(thePatient)
        .describedAs(
            format(
                "Ein {0} Krankenversicherter der E-Rezepte verschrieben bekommt und in Apotheken"
                    + " einlöst",
                insuranceType))
        .can(UseTheErpClient.with(erpClient));
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

    if (InsuranceTypeDe.fromCode(insuranceType).equals(InsuranceTypeDe.PKV)) {
      givenThat(thePatient).can(ManageChargeItems.heReceives());
    }
  }

  private Konnektor instantiateKonnektor(String name) {
    return Konnektor.create(getConfig(name, dto.getKonnektors()));
  }

  public static PrimSysBddFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    return new PrimSysBddFactory(dto, sca);
  }
}
