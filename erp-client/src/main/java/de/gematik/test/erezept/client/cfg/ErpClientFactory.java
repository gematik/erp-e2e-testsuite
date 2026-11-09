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

package de.gematik.test.erezept.client.cfg;

import static java.text.MessageFormat.format;

import de.gematik.bbriccs.fhir.codec.FhirCodec;
import de.gematik.bbriccs.fhir.de.value.KVNR;
import de.gematik.bbriccs.gid.GidClient;
import de.gematik.bbriccs.gid.config.GidAuthenticationConfig;
import de.gematik.bbriccs.idp.data.Scope;
import de.gematik.bbriccs.rest.HttpBClient;
import de.gematik.bbriccs.rest.fd.FhirBResponseCreator;
import de.gematik.bbriccs.rest.fd.MediaType;
import de.gematik.bbriccs.rest.gid.GidTokenHeaderProvider;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.rest.idp.IdpTokenHeaderProvider;
import de.gematik.bbriccs.rest.plugins.HttpBRequestCustomizer;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.Egk;
import de.gematik.bbriccs.smartcards.Smartcard;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.SmartcardType;
import de.gematik.bbriccs.smartcards.SmcB;
import de.gematik.bbriccs.smartcards.cfg.SmartcardConfigDto;
import de.gematik.idp.client.IIdpClient;
import de.gematik.idp.client.IdpClient;
import de.gematik.idp.crypto.model.PkiIdentity;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.ErpClient;
import de.gematik.test.erezept.client.UnirestRetryWrapper;
import de.gematik.test.erezept.client.vau.VauClient;
import de.gematik.test.erezept.client.vau.VauException;
import de.gematik.test.erezept.config.dto.actor.BaseActorConfiguration;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.HealthInsuranceConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.actor.PsActorConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.builder.GemFaker;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import de.gematik.test.erezept.fhir.parser.ValidatorType;
import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import kong.unirest.core.Unirest;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Root factory for creating {@link ErpClient} instances for a configured {@link
 * EnvironmentConfiguration}.
 *
 * <p>The factory is built once per environment (see {@link
 * #forEnvironment(EnvironmentConfiguration)}) and holds everything that is <em>common</em> to all
 * clients of that environment: the backend routes, the VAU certificate, the {@link
 * SmartcardArchive} and the default strategies for creating IDP- and VAU-clients.
 *
 * <h2>Staged creation</h2>
 *
 * <p>Client creation follows a <em>staged builder</em> approach. Rather than exposing a single
 * {@code create}-method with a large parameter list, the factory pre-applies all common
 * configuration and then hands the caller a small, actor-specific <em>stage</em> which exposes only
 * the choices that are still open:
 *
 * <ol>
 *   <li><b>Root stage</b> ({@code ErpClientFactory}): bound to the environment, owns the common
 *       configuration and the default IDP-/VAU-client strategies.
 *   <li><b>Actor stage</b> (e.g. {@link ErpClientPsBuilder} returned by {@link
 *       #prepareStagedClientFor(PsActorConfiguration)}): carries a pre-filled {@link
 *       ErpClient.ErpClientBuilder} and the actor's smartcard. It lets the caller override the
 *       IDP-/VAU-client strategies and, most importantly, decide <em>how to authenticate</em>.
 *   <li><b>Terminal call</b> (e.g. {@link ErpClientPsBuilder#createWithSmcBAuthentication()} or
 *       {@link ErpClientPsBuilder#createWithExternalAuthentication}): applies the remaining choice
 *       and builds the final {@link ErpClient}.
 * </ol>
 *
 * <pre>{@code
 * val factory = ErpClientFactory.forEnvironment(env).withSmartcardArchive(sca).build();
 *
 * // PS actors: the stage is returned, authentication is decided by the caller
 * val psClient = factory.prepareStagedClientFor(doctorCfg)
 *     .useExternalAuthentication(certSupplier, challengeSigner);
 *
 * // Patients: no open choices remain, the finished client is returned directly
 * val pair = factory.createErpClientFor(patientCfg);
 * }</pre>
 *
 * <p>Note that not every overload returns a stage: where no actor-specific decision is left
 * (currently for {@link PatientConfiguration}), the finished product is returned immediately.
 */
@Slf4j
public class ErpClientFactory {

  @Getter private final EnvironmentConfiguration env;
  private final X509Certificate vauCertificate;
  @Getter // for legacy reasons, no getter should be required in the future!
  private final SmartcardArchive smartcards;
  private final Function<BackendRouteConfiguration, IIdpClient> idpClientFactory;
  private final BiFunction<BackendRouteConfiguration, X509Certificate, HttpBClient>
      vauClientFactory;

  private ErpClientFactory(FactoryBuilder builder) {
    this.env = builder.env;
    this.vauCertificate = builder.vauCertificate;
    this.smartcards = builder.sca;
    this.idpClientFactory = this::createSmartcardIdpClient;
    this.vauClientFactory = this::createVauClient;
  }

  /**
   * Prepares an {@link ErpClient} for a Primärsystem (PS) actor and returns the <em>actor
   * stage</em> of the staged creation (see class documentation).
   *
   * <p>All common configuration (environment route, FHIR codec, MIME types, validation settings) is
   * already applied and the actor's SMC-B is resolved from the smartcard archive. What is
   * deliberately left open is the authentication strategy, which the caller selects on the returned
   * stage via {@link ErpClientPsBuilder#createWithSmcBAuthentication()} or {@link
   * ErpClientPsBuilder#createWithExternalAuthentication}.
   *
   * <p>The stage is pre-wired with this factory's default IDP- and VAU-client strategies; both can
   * be replaced on the stage before the terminal call.
   *
   * @param actor the PS actor configuration
   * @return a pre-configured stage; <b>not</b> a finished {@link ErpClient}
   */
  public ErpClientPsBuilder prepareStagedClientFor(PsActorConfiguration actor) {
    val clientType = ClientType.PS;
    val smcb = smartcards.getSmcbByICCSN(actor.getSmcbIccsn());
    return prepareStagedClientWithSmcB(clientType, actor, smcb);
  }

  public ErpClientPsBuilder prepareStagedClientFor(HealthInsuranceConfiguration actor) {
    val clientType = ClientType.KTR;
    val smcb = smartcards.getSmcbByICCSN(actor.getSmcbIccsn());
    return prepareStagedClientWithSmcB(clientType, actor, smcb);
  }

  public ErpClientPsBuilder prepareStagedClientFor(EuPharmacyConfiguration actor) {
    val clientType = ClientType.NCPEH;
    val smcb = smartcards.getSmcbByICCSN(actor.getSmcbIccsn());
    return prepareStagedClientWithSmcB(clientType, actor, smcb);
  }

  private ErpClientPsBuilder prepareStagedClientWithSmcB(
      ClientType clientType, BaseActorConfiguration actor, SmcB smcb) {
    val route = env.getTi();
    val fhirCodec = fhirCodec(actor);

    // pre-build the ErpClient with all common configuration, but without the customizations
    val erpClientBuilder =
        ErpClient.builder()
            .clientType(clientType)
            .fhir(fhirCodec)
            .responseFactory(new FhirBResponseCreator(fhirCodec, fhirCodec::decode))
            .baseFdUrl(route.getFdBaseUrl())
            .acceptCharset(actor.getAcceptCharset())
            .acceptMime(MediaType.fromString(actor.getAcceptMime()))
            .sendMime(MediaType.fromString(actor.getSendMime()))
            .validateRequest(actor.isValidateRequest());

    // concrete factory for the ErpClientPsFactory, which will create the ErpClient with the correct
    // IdpClient and VauClient
    return new ErpClientPsBuilder(route, vauCertificate, erpClientBuilder, smcb)
        .withIdpClientFrom(idpClientFactory)
        .withVauClientFrom(vauClientFactory);
  }

  /**
   * Create an Erp-Client which is paired with an eGK for use-cases performed in the role of a FdV
   *
   * @param actor is the PatientConfiguration which is used to create the ErpClient and the eGK
   * @return a Pair of ErpClient and Egk, where the ErpClient is configured to use the eGK for
   *     authentication
   */
  public Pair<ErpClient, Egk> prepareStagedClientFor(PatientConfiguration actor) {
    return prepareStagedClientFor(actor, idpClientFactory);
  }

  public Pair<ErpClient, Egk> prepareStagedClientFor(
      PatientConfiguration actor,
      Function<BackendRouteConfiguration, IIdpClient> customIdpClientFactory) {

    return prepareStagedClientWithDelegatedIdp(
        actor, idpClient -> customIdpClientFactory.apply(env.getInternet()));
  }

  /**
   * Create an Erp-Client which is paired with an eGK for use-cases performed in the role of a FdV,
   * while allowing the caller to wrap the originally created {@link IIdpClient}.
   *
   * <p><b>Note:</b> this method is only required for very specific edge-cases where the originally
   * created {@link IIdpClient} must be obtainable by the caller. The typical scenario is a caller
   * who wants to inject a custom {@link IIdpClient} which wraps the original one in order to
   * perform invalidating manipulations for negative test-cases (e.g. tampering with the issued
   * {@code IdpTokenResult}, see {@link de.gematik.test.erezept.client.idpclient.InvalidIdpClient}).
   *
   * <p>For all regular use-cases prefer {@link #prepareStagedClientFor(PatientConfiguration)}.
   *
   * @param actor is the PatientConfiguration which is used to create the ErpClient and the eGK
   * @param idpClientDelegatorFactory receives the originally created {@link IIdpClient} and returns
   *     the {@link IIdpClient} which shall actually be used by the ErpClient (e.g. a wrapping
   *     delegate)
   * @return a Pair of ErpClient and Egk, where the ErpClient is configured to use the eGK for
   *     authentication
   */
  public Pair<ErpClient, Egk> prepareStagedClientWithDelegatedIdp(
      PatientConfiguration actor, UnaryOperator<IIdpClient> idpClientDelegatorFactory) {
    val clientType = ClientType.FDV;
    val route = env.getInternet();
    val vauClient = vauClientFactory.apply(route, vauCertificate);
    val fhirCodec = fhirCodec(actor);

    val requestCustomizers = new LinkedList<HttpBRequestCustomizer>();

    // choose auth method depending on if we have a smartcard in the SmartcardArchive for the given
    // ICCSN or not
    val pluginPair =
        smartcards.getConfigsFor(SmartcardType.EGK).stream()
            .filter(cfg -> cfg.getIccsn().equals(actor.getEgkIccsn()))
            .map(cfg -> smartcards.getEgkByICCSN(cfg.getIccsn()))
            .map(
                egk -> {
                  // we have found a matching eGK in SmartcardArchive, now create the IdpClient and
                  // the IdpAuthCustomizer
                  log.info(
                      "Found matching eGK for ICCSN {} in SmartcardArchive: use IdpClient",
                      actor.getEgkIccsn());
                  // first create the original IdpClient, then pass it to the custom factory to
                  // allow wrapping
                  val originalIdpClient = idpClientFactory.apply(route);
                  // now apply the custom factory to allow wrapping
                  // (which in default case will return simply the original IdpClient back)
                  val idpClient = idpClientDelegatorFactory.apply(originalIdpClient).initialize();
                  // create the plugin which will add the IdpTokenHeader to the requests
                  val plugin = createIdpAuthCustomizer(idpClient, egk);
                  return Pair.of(plugin, egk);
                })
            .findFirst()
            .orElseGet(
                () -> {
                  val dummyEgk = createRandomDummyEgkFor(actor);
                  log.info(
                      "No matching eGK for ICCSN {} found in SmartcardArchive: use GID for"
                          + " KVNR {}",
                      actor.getEgkIccsn(),
                      dummyEgk.getKvnr());
                  val gidClient = createGidClient(route, dummyEgk);
                  val plugin = createGidAuthCustomizer(gidClient);
                  return Pair.of(plugin, dummyEgk);
                });
    requestCustomizers.add(pluginPair.getLeft());

    val erpClient =
        ErpClient.builder()
            .clientType(clientType)
            .requestCustomizers(requestCustomizers)
            .vauClient(vauClient)
            .fhir(fhirCodec)
            .responseFactory(new FhirBResponseCreator(fhirCodec, fhirCodec::decode))
            .baseFdUrl(route.getFdBaseUrl())
            .acceptCharset(actor.getAcceptCharset())
            .acceptMime(MediaType.fromString(actor.getAcceptMime()))
            .sendMime(MediaType.fromString(actor.getSendMime()))
            .validateRequest(actor.isValidateRequest())
            .build();

    return Pair.of(erpClient, pluginPair.getRight());
  }

  private FhirCodec fhirCodec(BaseActorConfiguration cfg) {
    val validatorType = ValidatorType.fromString(cfg.getFhirValidator()).orElse(ValidatorType.NONE);
    log.trace("Choosing FHIR Validator {} for {}", validatorType, cfg.getName());
    return new FhirParser(validatorType);
  }

  private Egk createRandomDummyEgkFor(PatientConfiguration actor) {
    val egkConfig = new SmartcardConfigDto();
    egkConfig.setType(SmartcardType.EGK);
    val iccsn =
        Optional.ofNullable(actor.getEgkIccsn())
            .filter(it -> !it.isBlank())
            .orElseGet(() -> "802768831100001" + GemFaker.getFaker().regexify("^\\d{5}$"));
    egkConfig.setIccsn(iccsn);

    val kvnr = Optional.ofNullable(actor.getKvnr()).orElseGet(KVNR::randomStringValue);
    egkConfig.setIdentifier(kvnr);

    val name =
        Optional.ofNullable(actor.getName())
            .filter(it -> !it.isBlank())
            .orElseGet(() -> GemFaker.getFaker().name().fullName());
    egkConfig.setOwnerName(name);

    egkConfig.setNote(format("Randomly generated dummy eGK for ''{0}'' with KVNR {1}", name, kvnr));
    return DummyEgk.fromConfig(egkConfig);
  }

  private HttpBClient createVauClient(
      BackendRouteConfiguration cfg, X509Certificate vauCertificate) {
    return new VauClient(cfg.getFdBaseUrl(), vauCertificate, cfg.getXapiKey(), cfg.getUserAgent());
  }

  private IIdpClient createSmartcardIdpClient(BackendRouteConfiguration cfg) {
    val scopes = getScopes(cfg).stream().map(Scope::value).collect(Collectors.toSet());
    val idpCfg = cfg.getIdp();
    return IdpClient.builder()
        .clientId(idpCfg.getClientId())
        .redirectUrl(idpCfg.getRedirectUrl())
        .discoveryDocumentUrl(idpCfg.getDiscoveryDocument())
        .scopes(scopes)
        .build();
  }

  private GidClient createGidClient(BackendRouteConfiguration cfg, Egk egk) {
    val idpCfg = cfg.getSecIdp();
    val config =
        GidAuthenticationConfig.builder()
            .fedAuthEndpoint(idpCfg.getFedAuthEndpoint())
            .tokenEndpoint(idpCfg.getTokenEndpoint())
            .sekIdpIss(idpCfg.getSekIdpIss())
            .clientId(idpCfg.getClientId())
            .redirectUri(idpCfg.getRedirectUri())
            .codeChallenge(idpCfg.getCodeChallenge())
            .codeVerifier(idpCfg.getCodeVerifier())
            .keyVerifier(idpCfg.getKeyVerifier())
            .tokenKey(idpCfg.getTokenKey())
            .scope(getScopes(cfg))
            .kvnr(egk.getKvnr())
            .build();

    return GidClient.create(config).initialize();
  }

  /**
   * Creates an {@link HttpBRequestCustomizer} which adds the IDP authentication header to requests.
   * This method has to be package private because it is used by the {@link ErpClientPsBuilder} to
   * create the final {@link ErpClient} with the correct IDP authentication header.
   *
   * @param idpClient the IDP client which is used to authenticate the requests
   * @param smartcard the smartcard which is used to authenticate the requests
   * @return an {@link HttpBRequestCustomizer} which adds the IDP authentication header to requests
   */
  static HttpBRequestCustomizer createIdpAuthCustomizer(IIdpClient idpClient, Smartcard smartcard) {
    val autCert = smartcard.getAutCertificate();
    val pki =
        PkiIdentity.builder()
            .certificate(autCert.getX509Certificate())
            .privateKey(autCert.getPrivateKey())
            .build();

    return IdpTokenHeaderProvider.using(() -> idpClient.login(pki));
  }

  private static HttpBRequestCustomizer createGidAuthCustomizer(GidClient gidClient) {
    return GidTokenHeaderProvider.using(gidClient);
  }

  private List<Scope> getScopes(BackendRouteConfiguration cfg) {
    return cfg.getFdBaseUrl().startsWith("https://erp-dev.")
        ? Scope.openId(GidScopeErp.E_REZEPT_DEV)
        : Scope.openId(GidScopeErp.E_REZEPT);
  }

  public static FactoryBuilder forEnvironment(EnvironmentConfiguration env) {
    return new FactoryBuilder(env);
  }

  /** This builder is responsible for building the ErpClientFactory. */
  @Slf4j
  public static class FactoryBuilder {

    private final UnirestRetryWrapper retryWrapper = new UnirestRetryWrapper();
    private final EnvironmentConfiguration env;

    private SmartcardArchive sca;

    /**
     * If true, the public VAU certificate will be requested via internet route from the FD If
     * false, the VAU certificate will be requested via TI route from the FD.
     */
    private final boolean preferPublicVauCertificate = true; // NOSONAR

    private X509Certificate vauCertificate;

    private FactoryBuilder(EnvironmentConfiguration env) {
      this.env = env;

      // pre-set the VAU certificate if given hardcoded in the environment configuration
      Stream.of(env.getInternet(), env.getTi())
          .filter(Objects::nonNull)
          .map(BackendRouteConfiguration::getVauCertificate)
          .filter(Objects::nonNull)
          .findFirst()
          .ifPresent(this::withVauCertificate);
    }

    public FactoryBuilder withSmartcardArchive(SmartcardArchive sca) {
      this.sca = sca;
      return this;
    }

    /**
     * Provide the VAU certificate as a Base64 encoded String (DER or PEM encoded X.509
     * certificate).
     *
     * @param vauCertificate the Base64 encoded certificate
     * @return this builder
     * @throws VauException if the String cannot be decoded or parsed as X.509 certificate
     */
    public FactoryBuilder withVauCertificate(String vauCertificate) {
      val base64 =
          vauCertificate
              .replace("-----BEGIN CERTIFICATE-----", "")
              .replace("-----END CERTIFICATE-----", "")
              .replaceAll("\\s", "");
      try {
        val data = Base64.getDecoder().decode(base64);
        return withVauCertificate(parseCertificate(data));
      } catch (IllegalArgumentException | CertificateException e) {
        throw new VauException("Error during parsing of Base64 encoded VAU-Certificate", e);
      }
    }

    public FactoryBuilder withVauCertificate(X509Certificate vauCertificate) {
      this.vauCertificate = vauCertificate;
      return this;
    }

    public ErpClientFactory build() {
      if (vauCertificate == null) {
        // if no VAU certificate is provided, request it from the FD via the appropriate route
        val route = preferPublicVauCertificate ? env.getInternet() : env.getTi();
        withVauCertificate(requestVauCertificate(route));
      }
      return new ErpClientFactory(this);
    }

    /**
     * Requests the VAU certificate from the FD via the given route configuration. This is performed
     * only once during the build process of the ErpClientFactory.
     *
     * @param cfg the backend route configuration
     * @return the X509Certificate of the VAU
     * @throws VauException if the request fails or the certificate cannot be parsed
     */
    @SneakyThrows
    private X509Certificate requestVauCertificate(BackendRouteConfiguration cfg) {
      val certificateFactory = CertificateFactory.getInstance("X.509");
      val certUrl = format("{0}/VAUCertificate", cfg.getFdBaseUrl());

      val headers = new HashMap<String, String>();

      // set the x-api-key header if it is present in the configuration
      Optional.ofNullable(cfg.getXapiKey())
          .filter(it -> !it.isEmpty())
          .ifPresent(it -> AuthHttpHeaderKey.X_API_KEY.createHeader(it).apply(headers::put));
      // set the user-agent header if it is present in the configuration
      Optional.ofNullable(cfg.getUserAgent())
          .filter(it -> !it.isEmpty())
          .ifPresent(it -> StandardHttpHeaderKey.USER_AGENT.createHeader(it).apply(headers::put));

      log.info("Request {} Certificate from {}", certificateFactory.getType(), certUrl);
      try (val client = Unirest.spawnInstance()) {
        val conf =
            client
                .config()
                .defaultBaseUrl(cfg.getFdBaseUrl())
                .connectTimeout(UnirestRetryWrapper.CONNECT_TIMEOUT * 1000)
                .verifySsl(false);
        headers.forEach(conf::addDefaultHeader);

        val req = client.get("/VAUCertificate");

        val response = retryWrapper.requestWithRetries(req);
        val data = response.getBody();
        log.info(
            "Received response with {} bytes ({}) from VAU-Certificate with status code {}",
            data.length,
            response.getHeaders().getFirst("content-type"),
            response.getStatus());
        val cert = parseCertificate(data);
        log.info("Received VAU-Certificate successfully");
        return cert;
      } catch (CertificateException e) {
        throw new VauException("Error during Requesting VAU-Certificate", e);
      }
    }

    private static X509Certificate parseCertificate(byte[] data) throws CertificateException {
      val certificateFactory = CertificateFactory.getInstance("X.509");
      return (X509Certificate)
          certificateFactory.generateCertificate(new ByteArrayInputStream(data));
    }
  }
}
