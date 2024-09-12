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
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.abilities.OCSPAbility;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.abilities.TSLAbility;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.apimeasure.ApiCallStopwatch;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.actor.ApothecaryConfiguration;
import de.gematik.test.erezept.config.dto.actor.BaseActorConfiguration;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.KonnektorConfiguration;
import de.gematik.test.erezept.config.dto.konnektor.LocalKonnektorConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import de.gematik.test.erezept.fhir.values.KVNR;
import de.gematik.test.erezept.screenplay.abilities.ManagePharmacyPrescriptions;
import de.gematik.test.erezept.screenplay.abilities.ProvideDoctorBaseData;
import de.gematik.test.erezept.screenplay.abilities.ProvideEGK;
import de.gematik.test.erezept.screenplay.abilities.ProvidePatientBaseData;
import de.gematik.test.erezept.screenplay.abilities.UseSMCB;
import de.gematik.test.erezept.screenplay.abilities.UseTheErpClient;
import de.gematik.test.erezept.screenplay.abilities.UseTheKonnektor;
import de.gematik.test.konnektor.Konnektor;
import de.gematik.test.konnektor.cfg.KonnektorFactory;
import de.gematik.test.konnektor.soap.mock.vsdm.VsdmService;
import kong.unirest.core.Unirest;
import kong.unirest.jackson.JacksonObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;

/** Configured Factory for the erp-fd-product-Testsuite */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErpFdTestsuiteFactory extends ConfiguredFactory {

  private final PrimsysConfigurationDto dto;
  @Delegate private final SmartcardArchive smartcards;
  private final ApiCallStopwatch stopwatch = StopwatchProvider.getInstance().getStopwatch();

  public VsdmService getSoftKonnVsdmService() {
    val softKonnConfig = (LocalKonnektorConfiguration) this.getKonnektorConfig("Soft-Konn");
    return VsdmService.createFrom(softKonnConfig.getVsdmServiceConfiguration());
  }

  public <A extends Actor> void equipAsDoctor(A actor) {
    val name = actor.getName();
    log.info(format("Equip Doctor {0}", name));

    val cfg = this.getDoctorConfig(name);
    val smcb = this.getSmcbByICCSN(cfg.getSmcbIccsn());
    val hba = this.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(hba).and(algorithm).on(instantiateDoctorKonnektor(cfg));
    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(useTheKonnektor)
        .can(useTheErpClientFrom(cfg).authenticatingWith(useTheKonnektor))
        .can(ProvideDoctorBaseData.fromConfiguration(cfg));
  }

  public <A extends Actor> void equipAsPharmacy(A actor) {
    val name = actor.getName();
    log.info(format("Equip Pharmacy {0}", name));

    val cfg = this.getPharmacyConfig(name);
    val smcb = this.getSmcbByICCSN(cfg.getSmcbIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(algorithm).on(instantiatePharmacyKonnektor(cfg));
    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(useTheKonnektor)
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(useTheErpClientFrom(cfg).authenticatingWith(useTheKonnektor));
  }

  public <A extends Actor> void equipAsPatient(A actor) {
    val name = actor.getName();
    log.info(format("Equip Patient {0}", name));

    val cfg = this.getPatientConfig(name);
    val egk = this.getEgkByICCSN(cfg.getEgkIccsn());

    givenThat(actor)
        .describedAs("Ein/e 'E-Rezept-ready' Versicherte/r der E-Rezepte erhalten kann")
        .whoCan(ProvidePatientBaseData.forGkvPatient(KVNR.from(egk.getKvnr()), name))
        .can(ProvideEGK.sheOwns(egk))
        .can(useTheErpClientFrom(cfg).authenticatingWith(egk));
  }

  public <A extends Actor> void equipWithRawHttp(A actor) {
    val client = Unirest.spawnInstance();
    client.config().setObjectMapper(new JacksonObjectMapper());
    BackendRouteConfiguration envConfig;
    if (actor instanceof PharmacyActor || actor instanceof DoctorActor) {
      envConfig = this.getActiveEnvironment().getTi();
    } else {
      envConfig = this.getActiveEnvironment().getInternet();
    }
    client
        .config()
        .defaultBaseUrl(envConfig.getFdBaseUrl())
        .addDefaultHeader("X-api-key", envConfig.getXapiKey())
        .addDefaultHeader("User-Agent", envConfig.getUserAgent());
    val rawHttpAbility = new RawHttpAbility(client);
    actor.can(rawHttpAbility);
  }

  public <A extends Actor> void equipForOCSP(A actor) {
    val client = Unirest.spawnInstance();
    client.config().setObjectMapper(new JacksonObjectMapper());
    BackendRouteConfiguration envConfig;
    if (actor instanceof PharmacyActor || actor instanceof DoctorActor) {
      envConfig = this.getActiveEnvironment().getTi();
    } else {
      envConfig = this.getActiveEnvironment().getInternet();
    }
    client
        .config()
        .defaultBaseUrl(envConfig.getFdBaseUrl())
        .addDefaultHeader("X-api-key", envConfig.getXapiKey())
        .addDefaultHeader("User-Agent", envConfig.getUserAgent());
    val oCSPAbility = new OCSPAbility(client, "/OCSPResponse");
    actor.can(oCSPAbility);
  }

  public <A extends Actor> void equipForTslDownload(A actor) {
    val client = Unirest.spawnInstance();
    client.config().setObjectMapper(new JacksonObjectMapper());
    BackendRouteConfiguration envConfig;
    if (actor instanceof PharmacyActor || actor instanceof DoctorActor) {
      envConfig = this.getActiveEnvironment().getTi();
    } else {
      envConfig = this.getActiveEnvironment().getInternet();
    }
    val environment = this.getActiveEnvironment().getName();
    val r =
        switch (environment.toUpperCase()) {
          case "TU":
            yield "/ECC-RSA_TSL-test.xml";
          case "RU", "RU-DEV":
            yield "/ECC-RSA_TSL-ref.xml";
          default:
            yield "/ECC-RSA_TSL-ref.xml";
        };
    client.config().defaultBaseUrl(envConfig.getTslBaseUrl());
    val tslAbility = new TSLAbility(client, r);
    actor.can(tslAbility);
  }

  public <A extends Actor> void equipAsApothecary(A actor) {
    val name = actor.getName();
    log.info(format("Equip Apothecary {0}", name));

    val cfg = this.getApothecaryConfig(name);
    val hba = this.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .can(UseTheKonnektor.with(hba).and(algorithm).on(instantiateApothecaryKonnektor(cfg)));
  }

  private <C extends BaseActorConfiguration> UseTheErpClient useTheErpClientFrom(C config) {
    if (config instanceof PatientConfiguration pcfg) {
      return UseTheErpClient.with(
          ErpClientFactory.createErpClient(getActiveEnvironment(), pcfg), stopwatch);
    } else if (config instanceof PsActorConfiguration pcfg) {
      return UseTheErpClient.with(
          ErpClientFactory.createErpClient(getActiveEnvironment(), pcfg), stopwatch);
    } else {
      throw new ConfigurationException(
          format(
              "Cannot create erp-client for {0} with configuration {1}",
              config.getName(), config.getClass().getSimpleName()));
    }
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return this.getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public DoctorConfiguration getDoctorConfig(String name) {
    return getConfig(name, dto.getActors().getDoctors());
  }

  public PharmacyConfiguration getPharmacyConfig(String name) {
    return getConfig(name, dto.getActors().getPharmacies());
  }

  public PatientConfiguration getPatientConfig(String name) {
    return getConfig(name, dto.getActors().getPatients());
  }

  public ApothecaryConfiguration getApothecaryConfig(String name) {
    return getConfig(name, dto.getActors().getApothecaries());
  }

  public Konnektor instantiateKonnektorClient(String name) {
    return KonnektorFactory.createKonnektor(this.getKonnektorConfig(name));
  }

  public Konnektor instantiateDoctorKonnektor(DoctorConfiguration docConfig) {
    return instantiateKonnektorClient(docConfig.getKonnektor());
  }

  public Konnektor instantiatePharmacyKonnektor(PharmacyConfiguration pharmacyConfig) {
    return instantiateKonnektorClient(pharmacyConfig.getKonnektor());
  }

  public Konnektor instantiateApothecaryKonnektor(ApothecaryConfiguration apothecaryConfig) {
    return instantiateKonnektorClient(apothecaryConfig.getKonnektor());
  }

  private KonnektorConfiguration getKonnektorConfig(String name) {
    return this.getConfig(name, dto.getKonnektors());
  }

  public static ErpFdTestsuiteFactory create() {
    val smartcards = SmartcardArchive.fromResources();
    return ConfigurationReader.forPrimSysConfiguration()
        .wrappedBy(dto -> ErpFdTestsuiteFactory.fromDto(dto, smartcards));
  }

  public static ErpFdTestsuiteFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    return new ErpFdTestsuiteFactory(dto, sca);
  }
}
