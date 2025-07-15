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
 *
 * *******
 *
 * For additional notes and disclaimer from gematik and in case of changes by gematik find details in the "Readme" file.
 */

package de.gematik.test.erezept.client.cfg;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.reset;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static com.github.tomakehurst.wiremock.client.WireMock.verify;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.mockStatic;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.rest.headers.AuthHttpHeaderKey;
import de.gematik.bbriccs.rest.headers.StandardHttpHeaderKey;
import de.gematik.test.erezept.client.testutils.VauCertificateGenerator;
import de.gematik.test.erezept.client.vau.VauException;
import de.gematik.test.erezept.config.dto.actor.DoctorConfiguration;
import de.gematik.test.erezept.config.dto.actor.PatientConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.BackendRouteConfiguration;
import de.gematik.test.erezept.config.dto.erpclient.EnvironmentConfiguration;
import de.gematik.test.erezept.fhir.parser.FhirParser;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@Slf4j
@WireMockTest(httpPort = 8081)
class ErpClientFactoryTest {

  @RegisterExtension
  private final WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  private final String fdBaseUrl = "http://localhost:8081";

  @SneakyThrows
  @BeforeEach
  void mockVauCertificateEndpoint() {
    stubFor(
        get(urlPathEqualTo("/VAUCertificate"))
            .willReturn(
                aResponse()
                    .withStatus(200)
                    .withBody(
                        VauCertificateGenerator.generateRandomVauCertificate().getEncoded())));

    // reset the cache before each test
    val certificateField = ErpClientFactory.class.getDeclaredField("vauCertificate");
    certificateField.setAccessible(true);
    certificateField.set(null, null);
  }

  private EnvironmentConfiguration createEnvironmentConfiguration() {
    val env = new EnvironmentConfiguration();

    val envRoute = new BackendRouteConfiguration();
    env.setTi(envRoute);
    env.setInternet(envRoute);
    envRoute.setClientId("gematikTestPs");
    envRoute.setRedirectUrl("https://gemtest.de");
    envRoute.setDiscoveryDocumentUrl("https://idp.gemtest.de/.well-known/openid-configuration");
    envRoute.setFdBaseUrl(fdBaseUrl);
    envRoute.setUserAgent("Test-Agent");
    envRoute.setXapiKey("123");

    return env;
  }

  @Test
  void shouldCreateDoctorErpClient() {
    val env = createEnvironmentConfiguration();
    val actor = new DoctorConfiguration();

    // mocking FhirParser-Constructor saves some execution time for reading Fhir-Profiles
    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, actor));
    }
  }

  @Test
  void shouldCreatePatientErpClient() {
    val env = createEnvironmentConfiguration();
    val actor = new PatientConfiguration();

    // mocking FhirParser-Constructor saves some execution time for reading Fhir-Profiles
    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, actor));
    }
  }

  @Test
  void shouldCacheCertificate() {
    val env = createEnvironmentConfiguration();
    val patient = new PatientConfiguration();
    val doctor = new DoctorConfiguration();

    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, patient));
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, doctor));
    }

    // verify the endpoint was called only once!
    verify(1, getRequestedFor(urlPathEqualTo("/VAUCertificate")));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldNotSendEmptyApiKey(boolean isEmpty) {
    val env = createEnvironmentConfiguration();
    val patient = new PatientConfiguration();

    val headerValue = isEmpty ? "" : null;
    env.getInternet().setXapiKey(headerValue);

    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, patient));
    }

    // verify only one request without the X_API_KEY
    verify(
        1,
        getRequestedFor(urlPathEqualTo("/VAUCertificate"))
            .withoutHeader(AuthHttpHeaderKey.X_API_KEY.getKey()));
  }

  @ParameterizedTest
  @ValueSource(booleans = {true, false})
  void shouldNotSendEmptyUserAgent(boolean isEmpty) {
    val env = createEnvironmentConfiguration();
    val patient = new DoctorConfiguration();

    val headerValue = isEmpty ? "" : null;
    val originalAgent = env.getTi().getUserAgent();
    env.getTi().setUserAgent(headerValue);

    try (val mocked = mockConstruction(FhirParser.class)) {
      assertDoesNotThrow(() -> ErpClientFactory.createErpClient(env, patient));
    }

    // verify no Requests with the original User-Agent
    // this workaround is required because unirest sets its own User-Agent if no one given
    verify(
        0,
        getRequestedFor(urlPathEqualTo("/VAUCertificate"))
            .withHeader(StandardHttpHeaderKey.USER_AGENT.getKey(), equalTo(originalAgent)));
  }

  @Test
  void shouldThrowOnInvalidVauCertificate() {
    val env = createEnvironmentConfiguration();
    val actor = new DoctorConfiguration();

    // reset WireMock will result in a string response, which is an invalid X509 certificate
    reset();
    val exception =
        assertThrows(VauException.class, () -> ErpClientFactory.createErpClient(env, actor));
    assertTrue(exception.getMessage().contains("Error during Requesting VAU-Certificate"));
  }

  @Test
  void shouldSneakyThrowOnCertificateFactory() {
    val env = createEnvironmentConfiguration();
    val actor = new DoctorConfiguration();

    try (val certFactory = mockStatic(CertificateFactory.class)) {
      certFactory
          .when(() -> CertificateFactory.getInstance(anyString()))
          .thenThrow(CertificateException.class);
      assertThrows(CertificateException.class, () -> ErpClientFactory.createErpClient(env, actor));
    }
  }
}
