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

import static java.text.MessageFormat.format;
import static net.serenitybdd.screenplay.GivenWhenThen.givenThat;

import de.gematik.bbriccs.crypto.CryptoSystem;
import de.gematik.bbriccs.fhir.builder.FakerBrick;
import de.gematik.bbriccs.fhir.compare.fhirpatch.HapiPatchComparator;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.konnektor.Konnektor;
import de.gematik.bbriccs.konnektor.cfg.KonnektorConfiguration;
import de.gematik.bbriccs.konnektor.cfg.SoftKonServiceConfiguration;
import de.gematik.bbriccs.popp.PoppTokenGenerator;
import de.gematik.bbriccs.rest.UnirestHttpClient;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.vsdm.VsdmService;
import de.gematik.idp.client.IIdpClient;
import de.gematik.idp.client.IdpTokenResult;
import de.gematik.idp.token.JsonWebToken;
import de.gematik.test.core.StopwatchProvider;
import de.gematik.test.erezept.abilities.*;
import de.gematik.test.erezept.actors.DoctorActor;
import de.gematik.test.erezept.actors.ErpActor;
import de.gematik.test.erezept.actors.PharmacyActor;
import de.gematik.test.erezept.client.cfg.ErpClientFactory;
import de.gematik.test.erezept.client.idpclient.InvalidIdpClient;
import de.gematik.test.erezept.common.apimeasure.ApiCallStopwatch;
import de.gematik.test.erezept.common.screenplay.abilities.*;
import de.gematik.test.erezept.config.ConfigurationReader;
import de.gematik.test.erezept.config.dto.ConfiguredFactory;
import de.gematik.test.erezept.config.dto.actor.*;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.primsys.PrimsysConfigurationDto;
import de.gematik.test.erezept.config.exceptions.ConfigurationException;
import de.gematik.test.erezept.config.exceptions.ConfigurationMappingException;
import de.gematik.test.erezept.eml.EpaMockClient;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.ValidatorType;
import de.gematik.test.erezept.screenplay.abilities.*;
import de.gematik.test.erezept.trezept.TRegisterMockClient;
import java.util.Optional;
import java.util.function.UnaryOperator;
import javax.annotation.Nullable;
import kong.unirest.core.Unirest;
import kong.unirest.jackson.JacksonObjectMapper;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Delegate;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import org.apache.commons.lang3.StringUtils;

/** Configured Factory for the erp-fd-product-Testsuite */
@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ErpFdTestsuiteFactory extends ConfiguredFactory {

  private final PrimsysConfigurationDto dto;
  @Delegate private final SmartcardArchive smartcards;
  private final ErpClientFactory erpClientFactory;
  private final ApiCallStopwatch stopwatch = StopwatchProvider.getInstance().getStopwatch();
  private static final int GEM_PROXY_PORT = 3128;

  public VsdmService getSoftKonnVsdmService() {
    val cfg = this.getKonnektorConfig("Soft-Konn");

    if (!(cfg.getService() instanceof SoftKonServiceConfiguration serviceCfg)) {
      throw new IllegalArgumentException(
          "The configuration is not a SoftKon service configuration");
    }
    return Optional.ofNullable(serviceCfg.getVsdmConfiguration())
        .map(VsdmService::createFrom)
        .orElseGet(VsdmService::instantiateWithTestKey);
  }

  public PoppTokenGenerator getPoppTokenGenerator() {
    val cfg = dto.getPoppTokenGenerator();
    return PoppTokenGenerator.from(cfg);
  }

  public <A extends Actor> void equipAsPatientWithInvalidIdpToken(A actor) {
    this.equipAsPatient(actor);
    val patient =
        getConfigOptionally(actor.getName(), dto.getActors().getPatients())
            .orElseThrow(
                () ->
                    new ConfigurationMappingException(
                        "Patient with invalid IdP token must be configured in the configuration"));

    // Negative test-case setup: the patient shall authenticate with a *syntactically broken* access
    // token so that the FD rejects the request. This is achieved in two steps:
    //
    // 1) tokenManipulator: receives the genuine IdpTokenResult issued by the real IDP and corrupts
    //    it by appending "invalid" to the raw JWT string. This breaks the signature (and the JWT
    //    structure itself), so the token can never be verified by the FD.
    UnaryOperator<IdpTokenResult> tokenManipulator =
        token -> {
          val invalidToken = token.getAccessToken().getRawString() + "invalid";
          token.setAccessToken(new JsonWebToken(invalidToken));
          return token;
        };

    // 2) idpClientDelegationFactory: the ErpClientFactory creates the real IIdpClient as usual and
    //    hands it to this function, which wraps it in an InvalidIdpClient. The wrapper delegates
    //    the actual login to the real client and then applies the tokenManipulator on the result,
    //    i.e. the ErpClient sees a valid login flow but ends up using the corrupted token.
    UnaryOperator<IIdpClient> idpClientDelegationFactory =
        idp -> new InvalidIdpClient(idp, tokenManipulator);

    val p =
        erpClientFactory.prepareStagedClientWithDelegatedIdp(patient, idpClientDelegationFactory);
    val erpClient = p.getLeft();
    val erpClientAbility = UseTheErpClient.with(erpClient, stopwatch);
    // now replace the existing ability with the new one that uses the invalid IDP token
    actor.can(erpClientAbility);
  }

  public <A extends Actor> void equipAsDoctor(A actor) {
    val name = actor.getName();
    log.info("Equip Doctor {}", name);

    val cfg = this.getDoctorConfig(name);
    val hba = this.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(hba).and(algorithm).on(instantiateKonnektorClient(cfg));

    // as PS we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(UsePoppTokenGenerator.with(getPoppTokenGenerator(), smcb))
        .can(UseHBA.itHasAccessTo(hba))
        .can(useTheKonnektor)
        .can(UseTheErpClient.with(erpClient, stopwatch))
        .can(ManageDoctorsPrescriptions.heIssued())
        .can(ProvideDoctorBaseData.fromConfiguration(cfg, hba.getTelematikId()));
  }

  public <A extends Actor> void equipWithEpaMockClient(A actor) {
    val name = actor.getName();
    log.info("Equip Actor {} with EpaMockClient", name);

    val epaMockClientConfig = getActiveEnvironment().getEpaMockClient();
    val epaMockUrl = epaMockClientConfig.getEpaMockUrl();
    val epaMockApiKey = epaMockClientConfig.getEpaMockApiKey();
    val logRetrieveInterval = epaMockClientConfig.getLogRetrieveInterval();
    val maxWait = epaMockClientConfig.getMaxWait();

    givenThat(actor)
        .can(
            UseTheEpaMockClient.with(
                EpaMockClient.withRestClient(
                    UnirestHttpClient.forUrl(epaMockUrl)
                        .xAuthorization(epaMockApiKey)
                        .withoutTlsVerification(),
                    logRetrieveInterval,
                    maxWait)));
  }

  @SuppressWarnings("java:S1313")
  public <A extends Actor> void equipWithTPrescriptionMockClient(A actor) {
    val name = actor.getName();
    log.info("Equip Actor {} with TRezept-MockClient", name);

    val mockClientConfig = getActiveEnvironment().getTRegisterMockClient();
    val mockUrl = mockClientConfig.getTRegisterMockUrl();
    val gemProxy = System.getProperty("https.gematikProxy");

    var builder = UnirestHttpClient.forUrl(mockUrl);

    if (StringUtils.isNotBlank(gemProxy)) {
      builder = builder.proxy(gemProxy, GEM_PROXY_PORT);
    }

    val fhirParser = new FhirParser(ValidatorType.BRICKS);

    val tregisterMockClient = TRegisterMockClient.withRestClient(builder.withoutTlsVerification());
    givenThat(actor).can(UseTheTRegisterMockClient.with(tregisterMockClient, fhirParser));
    givenThat(actor).can(UseResourceComparator.with(new HapiPatchComparator()));
  }

  public <A extends Actor> void equipAsPharmacy(A actor) {
    val name = actor.getName();
    log.info("Equip Pharmacy {}", name);

    val cfg = this.getPharmacyConfig(name);
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(algorithm).on(instantiateKonnektorClient(cfg));

    // as PS we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(useTheKonnektor)
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges())
        .can(UsePoppTokenGenerator.with(getPoppTokenGenerator(), smcb))
        .can(
            ProvidePharmacyBaseData.forNationalPharmacy()
                .practitionerIdentifier("telematik-id HBA")
                .organizationIdentifier(smcb)
                .build())
        .can(UseTheErpClient.with(erpClient, stopwatch));
  }

  public <T extends ErpActor> void equipAsEuPharmacy(T actor) {
    val name = actor.getName();
    log.info("Equip EU-Pharmacy {}", name);

    val cfg = this.getEuPharmacyConfig(name);

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    // as EU pharmacy we want always to use IdP directly for authentication
    val erpClient = erpClientStage.createWithSmcBAuthentication();

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(ProvidePharmacyBaseData.fromConfiguration(cfg))
        .can(UseTheErpClient.with(erpClient, stopwatch));
  }

  public <A extends Actor> void equipAsKtr(A actor) {
    val name = actor.getName();
    log.info("Equip KTR {}", name);

    val cfg = this.getKtrConfig(name);
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    // wire up erp-client with konnektor and SMC-B for external authenticate
    val erpClientStage = this.erpClientFactory.prepareStagedClientFor(cfg);
    val smcb = erpClientStage.getSmcb();

    val useTheKonnektor =
        UseTheKonnektor.with(smcb).and(algorithm).on(instantiateKonnektorClient(cfg));

    // as KTR we want always to use external authenticate...
    val erpClient =
        erpClientStage.createWithExternalAuthentication(
            () -> useTheKonnektor.getSmcbAuthCertificate().getPayload(),
            challenge -> useTheKonnektor.externalAuthenticate(challenge).getPayload());

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .whoCan(UseSMCB.itHasAccessTo(smcb))
        .can(useTheKonnektor)
        .can(ManagePharmacyPrescriptions.itWorksWith())
        .can(ManageCommunications.heExchanges())
        .can(UseTheErpClient.with(erpClient, stopwatch));
  }

  public <A extends Actor> void equipAsPatient(A actor) {
    val cfg = this.getPatientConfig(actor.getName());

    // the patient might not had have a name assigned before
    // getPatientConfig might have generated a random name for the patient, so we assign it to the
    // actor here
    val name = cfg.getName();
    actor.assignName(name);
    log.info("Equip Patient {}", name);

    val p = erpClientFactory.prepareStagedClientFor(cfg);
    val erpClient = p.getLeft();
    val egk = p.getRight();

    givenThat(actor)
        .describedAs("Ein/e 'E-Rezept-ready' Versicherte/r der E-Rezepte erhalten kann")
        .whoCan(UseTheErpClient.with(erpClient, stopwatch))
        .can(ProvidePatientBaseData.forGkvPatient(KVNR.from(egk.getKvnr()), name))
        .can(ProvideEGK.sheOwns(egk))
        .can(ManageDataMatrixCodes.heGetsPrescribed())
        .can(ManageCommunications.heExchanges());
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
    log.info("Equip Apothecary {}", name);

    val cfg = this.getApothecaryConfig(name);
    val hba = this.getHbaByICCSN(cfg.getHbaIccsn());
    val algorithm = CryptoSystem.fromString(cfg.getAlgorithm());

    givenThat(actor)
        .describedAs(cfg.getDescription())
        .can(
            UseTheKonnektor.with(hba)
                .and(algorithm)
                .on(instantiateKonnektorClient(cfg.getKonnektor())));
  }

  public EnvironmentConfiguration getActiveEnvironment() {
    return getConfig(dto.getActiveEnvironment(), dto.getEnvironments());
  }

  public String getOcsp() {
    return getActiveEnvironment().getTi().getOcsp();
  }

  public DoctorConfiguration getDoctorConfig(String name) {
    return getConfig(name, dto.getActors().getDoctors());
  }

  public PharmacyConfiguration getPharmacyConfig(String name) {
    return getConfig(name, dto.getActors().getPharmacies());
  }

  public EuPharmacyConfiguration getEuPharmacyConfig(String name) {
    return getConfig(name, dto.getActors().getEuPharmacies());
  }

  public HealthInsuranceConfiguration getKtrConfig(String name) {
    return getConfig(name, dto.getActors().getHealthInsurances());
  }

  /**
   * Resolves the {@link PatientConfiguration} for the given actor name.
   *
   * <p><b>Note:</b> this method behaves differently from the other {@code getXXXConfig} methods:
   *
   * <ul>
   *   <li>the other {@code getXXXConfig} methods throw an exception if no configuration with the
   *       given name exists
   *   <li>this method does <b>not</b> throw; instead it fakes a {@link PatientConfiguration} with a
   *       random KVNR and a random eGK-ICCSN
   *   <li>because the faked ICCSN is unknown to the {@code SmartcardArchive}, the {@code
   *       ErpClientFactory} will not find a matching eGK and automatically falls back to GID
   *       authentication for this patient
   * </ul>
   *
   * This allows test-cases to use patients "out of thin air" without declaring them explicitly in
   * the configuration file.
   *
   * @param name the actor name of the patient as declared in the configuration (or any arbitrary
   *     name for an ad-hoc GID patient)
   * @return the configured {@link PatientConfiguration}, or a faked one if no configuration with
   *     the given name exists
   */
  public PatientConfiguration getPatientConfig(@Nullable String name) {
    val patientName =
        Optional.ofNullable(name).filter(it -> !it.isBlank()).orElse(GemFaker.fakerName());

    return getConfigOptionally(patientName, dto.getActors().getPatients())
        .orElseGet(
            () -> {
              val template = dto.getActors().getPatients().getFirst();
              val cfg = new PatientConfiguration();
              val kvnr = KVNR.randomStringValue();

              cfg.setName(patientName);
              cfg.setDescription(
                  format("Ad-hoc GID patient ''{0}'' with KVNR {1}", patientName, kvnr));
              cfg.setKvnr(kvnr);
              cfg.setEgkIccsn("802768831100001" + FakerBrick.getGerman().regexify("^\\d{5}$"));

              cfg.setAcceptMime(template.getAcceptMime());
              cfg.setSendMime(template.getSendMime());
              cfg.setAlgorithm(template.getAlgorithm());
              cfg.setCountryCode(template.getCountryCode());
              cfg.setFhirValidator(template.getFhirValidator());
              return cfg;
            });
  }

  public ApothecaryConfiguration getApothecaryConfig(String name) {
    return getConfig(name, dto.getActors().getApothecaries());
  }

  public Konnektor instantiateKonnektorClient(PsActorConfiguration config) {
    return instantiateKonnektorClient(config.getKonnektor());
  }

  public Konnektor instantiateKonnektorClient(String name) {
    return Konnektor.create(this.getKonnektorConfig(name));
  }

  private KonnektorConfiguration getKonnektorConfig(String name) {
    return getConfig(name, dto.getKonnektors());
  }

  public static ErpFdTestsuiteFactory create() {
    val smartcards = SmartcardArchive.fromResources();
    return ConfigurationReader.forPrimSysConfiguration()
        .wrappedBy(dto -> ErpFdTestsuiteFactory.fromDto(dto, smartcards));
  }

  public static ErpFdTestsuiteFactory fromDto(PrimsysConfigurationDto dto, SmartcardArchive sca) {
    val env =
        dto.getEnvironments().stream()
            .filter(it -> it.getName().equalsIgnoreCase(dto.getActiveEnvironment()))
            .findFirst()
            .orElseThrow(
                () ->
                    new ConfigurationException(
                        format(
                            "Active environment ''{0}'' not found in configuration",
                            dto.getActiveEnvironment())));
    val erpClientFactory = ErpClientFactory.forEnvironment(env).withSmartcardArchive(sca).build();

    return new ErpFdTestsuiteFactory(dto, sca, erpClientFactory);
  }
}
