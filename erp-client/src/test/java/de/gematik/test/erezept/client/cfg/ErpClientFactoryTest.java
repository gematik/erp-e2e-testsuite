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

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.anyRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockConstruction;

import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.gid.exception.GidClientConfigurationException;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.bbriccs.smartcards.DummyEgk;
import de.gematik.bbriccs.smartcards.SmartcardArchive;
import de.gematik.bbriccs.smartcards.exceptions.CardNotFoundException;
import de.gematik.test.erezept.client.ClientType;
import de.gematik.test.erezept.client.testutils.IdpDiscoveryDocumentStub;
import de.gematik.test.erezept.client.testutils.VauCertificateGenerator;
import de.gematik.test.erezept.client.vau.VauException;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.EuPharmacyConfiguration;
import de.gematik.test.erezept.config.dto.actor.HealthInsuranceConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.IdpCentralConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.IdpSectoralConfiguration;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import lombok.SneakyThrows;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

@WireMockTest(httpPort = 8082)
class ErpClientFactoryTest {

  private static final String FD_BASE_URL = "http://localhost:8082";
  private static final String VAU_CERTIFICATE_PATH = "/VAUCertificate";
  private static final String SEK_IDP_PATH_PREFIX = "/sek-idp";

  private static IdpDiscoveryDocumentStub idpStub;

  private final SmartcardArchive sca = SmartcardArchive.fromResources();

  @BeforeAll
  static void createIdpDiscoveryDocument() {
    // generating keys and signing the discovery document is expensive: do it only once
    idpStub = new IdpDiscoveryDocumentStub(FD_BASE_URL);
  }

  @SneakyThrows
  @BeforeEach
  void mockVauCertificateEndpoint() {
    // @WireMockTest resets all runtime stubs before each test, so (re-)register them here
    idpStub.register();

    stubFor(
        get(urlPathEqualTo(VAU_CERTIFICATE_PATH))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        VauCertificateGenerator.generateRandomVauCertificate().getEncoded())));
  }

  private EnvironmentConfiguration createEnvironmentConfiguration() {
    val env = new EnvironmentConfiguration();
    env.setName("UT");

    val idpConfig = new IdpCentralConfiguration();
    idpConfig.setClientId("gematikTestPs");
    idpConfig.setRedirectUrl("https://gemtest.de");
    idpConfig.setDiscoveryDocument(idpStub.getDiscoveryDocumentUrl());

    val secIdpConfig = new IdpSectoralConfiguration();
    secIdpConfig.setFedAuthEndpoint(FD_BASE_URL + SEK_IDP_PATH_PREFIX + "/auth");
    secIdpConfig.setTokenEndpoint(FD_BASE_URL + SEK_IDP_PATH_PREFIX + "/token");
    secIdpConfig.setSekIdpIss(FD_BASE_URL + SEK_IDP_PATH_PREFIX);
    secIdpConfig.setCodeChallenge("E9Melhoa2OwvFrEMTJguCHaoeK1t8URWbuGJSstw-cM");
    secIdpConfig.setCodeVerifier("dBjftJeZ4CVP-mB92K27uhbUJU1p1r_wW1gFWFOEjXk");
    secIdpConfig.setTokenKey(
        Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("0123456789abcdef0123456789abcdef".getBytes(StandardCharsets.UTF_8)));

    val envRoute = new BackendRouteConfiguration();
    envRoute.setIdp(idpConfig);
    envRoute.setSecIdp(secIdpConfig);
    envRoute.setFdBaseUrl(FD_BASE_URL);
    envRoute.setUserAgent("Test-Agent");
    envRoute.setXapiKey("123");

    env.setTi(envRoute);
    env.setInternet(envRoute);
    return env;
  }

  private ErpClientFactory createFactory(EnvironmentConfiguration env) {
    return ErpClientFactory.forEnvironment(env).withSmartcardArchive(sca).build();
  }

  private DoctorConfiguration createDoctorConfiguration() {
    val doctor = new DoctorConfiguration();
    doctor.setName("Dr. Test");
    doctor.setSmcbIccsn(sca.getSmcB(0).getIccsn());
    return doctor;
  }

  private PatientConfiguration createPatientConfiguration(String egkIccsn) {
    val patient = new PatientConfiguration();
    patient.setName("Test Patient");
    patient.setEgkIccsn(egkIccsn);
    return patient;
  }

  private void verifyNoSectoralIdpRequests() {
    verify(0, anyRequestedFor(urlPathMatching(SEK_IDP_PATH_PREFIX + ".*")));
  }

  // ----- FactoryBuilder / VAU-Certificate -----

  @Test
  void shouldRequestVauCertificateOnBuild() {
    val env = createEnvironmentConfiguration();
    assertDoesNotThrow(() -> createFactory(env));
    verify(1, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @Test
  void shouldRequestVauCertificateOnlyOncePerFactory() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);

    try (val mocked = mockConstruction(FhirParser.class)) {
      factory.prepareStagedClientFor(createDoctorConfiguration()).createWithSmcBAuthentication();
      factory.prepareStagedClientFor(createDoctorConfiguration()).createWithSmcBAuthentication();
      factory.prepareStagedClientFor(createPatientConfiguration(sca.getEgk(0).getIccsn()));
    }

    verify(1, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @Test
  void shouldNotRequestVauCertificateWhenGivenAsX509() {
    val env = createEnvironmentConfiguration();
    val certificate = VauCertificateGenerator.generateRandomVauCertificate();

    assertDoesNotThrow(
        () ->
            ErpClientFactory.forEnvironment(env)
                .withSmartcardArchive(sca)
                .withVauCertificate(certificate)
                .build());
    verify(0, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @SneakyThrows
  @Test
  void shouldNotRequestVauCertificateWhenGivenAsBase64String() {
    val env = createEnvironmentConfiguration();
    val certificate = VauCertificateGenerator.generateRandomVauCertificate();
    val base64 = Base64.getEncoder().encodeToString(certificate.getEncoded());

    assertDoesNotThrow(
        () ->
            ErpClientFactory.forEnvironment(env)
                .withSmartcardArchive(sca)
                .withVauCertificate(base64)
                .build());
    verify(0, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @SneakyThrows
  @Test
  void shouldNotRequestVauCertificateWhenGivenAsPemString() {
    val env = createEnvironmentConfiguration();
    val certificate = VauCertificateGenerator.generateRandomVauCertificate();
    val pem =
        "-----BEGIN CERTIFICATE-----\n"
            + Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8))
                .encodeToString(certificate.getEncoded())
            + "\n-----END CERTIFICATE-----\n";

    assertDoesNotThrow(
        () ->
            ErpClientFactory.forEnvironment(env)
                .withSmartcardArchive(sca)
                .withVauCertificate(pem)
                .build());
    verify(0, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @SneakyThrows
  @Test
  void shouldPreferVauCertificateFromEnvironmentConfiguration() {
    val env = createEnvironmentConfiguration();
    val certificate = VauCertificateGenerator.generateRandomVauCertificate();
    env.getInternet()
        .setVauCertificate(Base64.getEncoder().encodeToString(certificate.getEncoded()));

    assertDoesNotThrow(() -> createFactory(env));
    verify(0, getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH)));
  }

  @Test
  void shouldThrowOnInvalidBase64VauCertificate() {
    val env = createEnvironmentConfiguration();
    val builder = ErpClientFactory.forEnvironment(env).withSmartcardArchive(sca);

    val exception =
        assertThrows(VauException.class, () -> builder.withVauCertificate("not-a-certificate!"));
    assertTrue(exception.getMessage().contains("Base64 encoded VAU-Certificate"));
  }

  @Test
  void shouldThrowOnInvalidVauCertificateResponse() {
    val env = createEnvironmentConfiguration();

    // reset WireMock will result in a string response, which is an invalid X509 certificate
    reset();
    val exception = assertThrows(VauException.class, () -> createFactory(env));
    assertTrue(exception.getMessage().contains("Error during Requesting VAU-Certificate"));
  }

  @Test
  void shouldSendApiKeyAndUserAgent() {
    val env = createEnvironmentConfiguration();
    createFactory(env);

    verify(
        1,
        getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH))
            .withHeader(AuthHttpHeaderKey.X_API_KEY.getKey(), equalTo("123"))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.getKey(), equalTo("Test-Agent")));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldNotSendEmptyApiKey(String apiKey) {
    val env = createEnvironmentConfiguration();
    env.getInternet().setXapiKey(apiKey);

    createFactory(env);

    verify(
        1,
        getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH))
            .withoutHeader(AuthHttpHeaderKey.X_API_KEY.getKey()));
  }

  @ParameterizedTest
  @NullAndEmptySource
  void shouldNotSendEmptyUserAgent(String userAgent) {
    val env = createEnvironmentConfiguration();
    val originalAgent = env.getInternet().getUserAgent();
    env.getInternet().setUserAgent(userAgent);

    createFactory(env);

    // unirest sets its own User-Agent if none is given, so only verify the configured one is absent
    verify(
        0,
        getRequestedFor(urlPathEqualTo(VAU_CERTIFICATE_PATH))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.getKey(), equalTo(originalAgent)));
  }

  // ----- PS actors (SMC-B) -----

  @Test
  void shouldCreatePsClientWithSmcBAuthentication() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val doctor = createDoctorConfiguration();

    try (val mocked = mockConstruction(FhirParser.class)) {
      val stage = factory.prepareStagedClientFor(doctor);
      assertEquals(doctor.getSmcbIccsn(), stage.getSmcb().getIccsn());

      val client = stage.createWithSmcBAuthentication();
      assertNotNull(client);
      assertEquals(ClientType.PS, client.getClientType());
    }
    // the IDP is contacted for the discovery document on instantiation (IdpClient.initialize()),
    // but the actual login flow is executed later during the requests
    verify(1, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.DISCOVERY_DOCUMENT_PATH)));
  }

  @Test
  void shouldCreatePsClientWithExternalAuthentication() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val doctor = createDoctorConfiguration();
    val smcb = sca.getSmcB(0);

    try (val mocked = mockConstruction(FhirParser.class)) {
      val client =
          factory
              .prepareStagedClientFor(doctor)
              .createWithExternalAuthentication(
                  () -> smcb.getAutCertificate().getX509Certificate(), challenge -> challenge);
      assertNotNull(client);
      assertEquals(ClientType.PS, client.getClientType());
    }
  }

  @Test
  void shouldCreateKtrClient() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val ktr = new HealthInsuranceConfiguration();
    ktr.setName("KTR");
    ktr.setSmcbIccsn(sca.getSmcB(0).getIccsn());

    try (val mocked = mockConstruction(FhirParser.class)) {
      val client = factory.prepareStagedClientFor(ktr).createWithSmcBAuthentication();
      assertEquals(ClientType.KTR, client.getClientType());
    }
  }

  @Test
  void shouldCreateNcpehClient() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val euPharmacy = new EuPharmacyConfiguration();
    euPharmacy.setName("EU Pharmacy");
    euPharmacy.setSmcbIccsn(sca.getSmcB(0).getIccsn());

    try (val mocked = mockConstruction(FhirParser.class)) {
      val client = factory.prepareStagedClientFor(euPharmacy).createWithSmcBAuthentication();
      assertEquals(ClientType.NCPEH, client.getClientType());
    }
  }

  @Test
  void shouldThrowOnUnknownSmcbIccsn() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val doctor = createDoctorConfiguration();
    doctor.setSmcbIccsn("00000000000000000000");

    try (val mocked = mockConstruction(FhirParser.class)) {
      assertThrows(CardNotFoundException.class, () -> factory.prepareStagedClientFor(doctor));
    }
  }

  // ----- Patients (eGK / GID) -----

  @Test
  void shouldCreatePatientClientWithIdpWhenEgkIsInArchive() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val egk = sca.getEgk(0);
    val patient = createPatientConfiguration(egk.getIccsn());

    try (val mocked = mockConstruction(FhirParser.class)) {
      val pair = factory.prepareStagedClientFor(patient);
      assertEquals(ClientType.FDV, pair.getLeft().getClientType());
      assertEquals(egk.getIccsn(), pair.getRight().getIccsn());
      assertEquals(egk.getKvnr(), pair.getRight().getKvnr());
      assertFalse(pair.getRight() instanceof DummyEgk);
    }

    // IdpClient.initialize() fetches the discovery document and the referenced public keys
    verify(1, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.DISCOVERY_DOCUMENT_PATH)));
    verify(1, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.PUK_IDP_SIG_PATH)));
    verify(1, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.PUK_IDP_ENC_PATH)));
    verifyNoSectoralIdpRequests();
  }

  @Test
  void shouldCreatePatientClientWithGidWhenEgkIsNotInArchive() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val patient = createPatientConfiguration("99999999999999999999");
    patient.setKvnr("X110407071");

    try (val mocked = mockConstruction(FhirParser.class)) {
      val pair = factory.prepareStagedClientFor(patient);
      assertEquals(ClientType.FDV, pair.getLeft().getClientType());
      assertInstanceOf(DummyEgk.class, pair.getRight());
      assertEquals(patient.getEgkIccsn(), pair.getRight().getIccsn());
      assertEquals(patient.getKvnr(), pair.getRight().getKvnr());
    }

    // GidClient.initialize() is a no-op; the login flow only runs lazily on the first request
    verify(0, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.DISCOVERY_DOCUMENT_PATH)));
    verifyNoSectoralIdpRequests();
  }

  @Test
  void shouldThrowOnIncompleteSectoralIdpConfiguration() {
    val env = createEnvironmentConfiguration();
    env.getInternet().getSecIdp().setTokenKey(null);
    val factory = createFactory(env);
    val patient = createPatientConfiguration("99999999999999999999");

    try (val mocked = mockConstruction(FhirParser.class)) {
      val exception =
          assertThrows(
              GidClientConfigurationException.class, () -> factory.prepareStagedClientFor(patient));
      assertTrue(exception.getMessage().contains("tokenKey"));
    }
  }

  @Test
  void shouldFailWhenIdpDiscoveryDocumentIsUnavailable() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val patient = createPatientConfiguration(sca.getEgk(0).getIccsn());

    // the VAU certificate was already fetched on build(); dropping all stubs now only affects the
    // IDP
    reset();

    try (val mocked = mockConstruction(FhirParser.class)) {
      assertThrows(Exception.class, () -> factory.prepareStagedClientFor(patient));
    }
    verify(1, getRequestedFor(urlPathEqualTo(IdpDiscoveryDocumentStub.DISCOVERY_DOCUMENT_PATH)));
  }

  // ----- Misc -----

  @Test
  void shouldFallbackToNoneValidatorOnInvalidValidatorType() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val doctor = createDoctorConfiguration();
    doctor.setFhirValidator("ABC");

    // unlike the legacy factory, unknown validator types fall back to ValidatorType.NONE
    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(
          () -> factory.prepareStagedClientFor(doctor).createWithSmcBAuthentication());
    }
  }

  @Test
  void shouldCreateRandomDummyEgkFromGivenValues() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val patient = createPatientConfiguration("12345678901234567890");
    patient.setKvnr("X110407071");

    val egk = factory.prepareStagedClientFor(patient).getRight();
    assertInstanceOf(DummyEgk.class, egk);
    assertEquals("12345678901234567890", egk.getIccsn());
    assertEquals("X110407071", egk.getKvnr());
  }

  @Test
  void shouldCreateRandomDummyEgkWithRandomValues() {
    val env = createEnvironmentConfiguration();
    val factory = createFactory(env);
    val patient = new PatientConfiguration();

    val first = factory.prepareStagedClientFor(patient).getRight();
    val second = factory.prepareStagedClientFor(patient).getRight();
    assertNotNull(first.getKvnr());
    assertNotNull(second.getKvnr());
    assertNotEquals(first.getKvnr(), second.getKvnr());
  }
}
