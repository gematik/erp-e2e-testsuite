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

package de.gematik.test.core.expectations.rawhttp.pki.bodyVerifierTest;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.bbriccs.utils.PrivateConstructorsUtil;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.verifier.rawhttpverifier.OCSPRespVerifier;
import de.gematik.test.erezept.abilities.OCSPAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetOCSPRequest;
import de.gematik.test.erezept.actions.rawhttpactions.OcspRequestParameters;
import de.gematik.test.erezept.fhir.testutil.ErpFhirParsingTest;
import java.util.Base64;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.SneakyThrows;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest()
class OCSPRespVerifierTest extends ErpFhirParsingTest {

  private static final String BASE64_RESP_BODY =
      "TUlJRVh3b0JBS0NDQkZnd2dnUlVCZ2tyQmdFRkJRY3dBUUVFZ2dSRk1JSUVRVENDQVVhaFZqQlVNUXN3Q1FZRFZRUUdFd0pFUlRFYU1CZ0dBMVVFQ2d3UloyVnRZWFJwYXlCT1QxUXRWa0ZNU1VReEtUQW5CZ05WQkFNTUlHVm9ZMkVnVDBOVFVDQlRhV2R1WlhJZ05TQmxZMk1nVkVWVFZDMVBUa3haR0E4eU1ESTBNRFV4TkRBNE16WXhPVm93Z2JZd2diTXdRREFKQmdVckRnTUNHZ1VBQkJSOEplMmhldnlmdWZjYnVIL2xPUExpTEtlRW1BUVVCcGpwQWxYL3laOWNvMlVPOFYzaUlQV0UrNU1DQndDNy81YnpqUmlBQUJnUE1qQXlOREExTVRRd09ETTJNVGxhb1Z3d1dqQWFCZ1VySkFnRERBUVJHQTh5TURJek1ERXlOakUwTURnd05sb3dQQVlGS3lRSUF3MEVNekF4TUEwR0NXQ0dTQUZsQXdRQ0FRVUFCQ0M0NnM5cms4R2Q3aEFxSXlYc3RJZ1puTWVWTkliWElVK0p4OUJyRDNKU2hLRWlNQ0F3SGdZSkt3WUJCUVVITUFFR0JCRVlEekU0TnpBd01UQTNNREF3TURBd1dqQUtCZ2dxaGtqT1BRUURBZ05JQURCRkFpQklHNE56RVdvNVRMOG5oMnZ2OG5DVlo0VFAwU1grUnR6aWZOWk9ldDRNVUFJaEFJY0cxNS95bjdlaFUxTW1nT0VDKzhWZnBVMGhnRjN3TytjdHB5aEIyRlV5b0lJQ25UQ0NBcGt3Z2dLVk1JSUNQS0FEQWdFQ0FnY0JKZENkbmlOZE1Bb0dDQ3FHU000OUJBTUNNSUdETVFzd0NRWURWUVFHRXdKRVJURWZNQjBHQTFVRUNnd1daMlZ0WVhScGF5QkhiV0pJSUU1UFZDMVdRVXhKUkRFeU1EQUdBMVVFQ3d3cFQwTlRVQzFUYVdkdVpYSXRRMEVnWkdWeUlGUmxiR1Z0WVhScGEybHVabkpoYzNSeWRXdDBkWEl4SHpBZEJnTlZCQU1NRmtkRlRTNVBRMU5RTFVOQk9TQlVSVk5VTFU5T1RGa3dIaGNOTWpBd01qSTBNREF3TURBd1doY05NalV3TWpJME1qTTFPVFU1V2pCVU1Rc3dDUVlEVlFRR0V3SkVSVEVhTUJnR0ExVUVDZ3dSWjJWdFlYUnBheUJPVDFRdFZrRk1TVVF4S1RBbkJnTlZCQU1NSUdWb1kyRWdUME5UVUNCVGFXZHVaWElnTlNCbFkyTWdWRVZUVkMxUFRreFpNRm93RkFZSEtvWkl6ajBDQVFZSkt5UURBd0lJQVFFSEEwSUFCQWdmeTg2K21vUUJLbmJZREY4bHUySnRmTkNSUjFtMEZjcTJHL1FDN1JmWGxtRW4ycGQ5dTlyVlllVWJzKzNQTHlKZmNaMWxmZkc5ZW1hbTBsSUUrQStqZ2Njd2djUXdGUVlEVlIwZ0JBNHdEREFLQmdncWdoUUFUQVNCSXpBVEJnTlZIU1VFRERBS0JnZ3JCZ0VGQlFjRENUQTRCZ2dyQmdFRkJRY0JBUVFzTUNvd0tBWUlLd1lCQlFVSE1BR0dIR2gwZEhBNkx5OWxhR05oTG1kbGJXRjBhV3N1WkdVdmIyTnpjQzh3SFFZRFZSME9CQllFRkZkYmdrWUlkdkhPR0ptazdTVkZ5Q1Z4WG0wT01Bd0dBMVVkRXdFQi93UUNNQUF3SHdZRFZSMGpCQmd3Rm9BVXc4VXM1MkhRN0huUVlORURmSzdHQUc0d1JOMHdEZ1lEVlIwUEFRSC9CQVFEQWdaQU1Bb0dDQ3FHU000OUJBTUNBMGNBTUVRQ0lDSlR3WUVrNExBVGJOckp2eXlJUTRnbU1TOUpILzZwZmpTTCszdFd3Q2JRQWlCQ1JFYzdaRjdONUdHLzhoNmN0ZTRTT0tZV2dKNzg1UUdhVzYvS0lWNTd3dz09";

  private static final String ISSUER_NAME = "GEM.SMCB-CA51 TEST-ONLY";
  private static final String CERT_SERIAL_NR = "206706423598360";

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  private Actor actor;

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void shouldNotInstantiateTaskVerifier() {
    assertTrue(PrivateConstructorsUtil.isUtilityConstructor(OCSPRespVerifier.class));
  }

  @SneakyThrows
  @Test()
  @TestcaseId("ut_httpOcspResponseVerifierTest_01")
  @DisplayName("Positive Unit Test for an RawHttpOCSPRespVerifier to check Status == null / GOOD")
  void statusVerifierShouldWork() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");

    val byteBody = Base64.getDecoder().decode(BASE64_RESP_BODY);

    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPResponse?issuer-cn=GEM.SMCB-CA51+TEST-ONLY&serial-nr=206706423598360"))
            .willReturn(aResponse().withHeader("1", "2").withBody(byteBody).withStatus(200)));
    val actor = OnStage.theActor("Leonie");
    UnirestInstance client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new OCSPAbility(client, "/OCSPResponse"));
    val ocspResp =
        actor.asksFor(
            GetOCSPRequest.with(
                OcspRequestParameters.passConcreteParams(ISSUER_NAME, CERT_SERIAL_NR)));

    val step = OCSPRespVerifier.ocspResponseStatusIsGood();
    step.apply(ocspResp.getBody());
  }

  @SneakyThrows
  @Test()
  @TestcaseId("ut_httpOcspResponseVerifierTest_02")
  @DisplayName(
      "Positiver Unit Test zur Überprüfung, ob ein Fehlerstring im Body korrekt validiert wird")
  void verifierShouldWork() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");

    val byteBody = "Teststring".getBytes();

    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPResponse?issuer-cn=GEM.SMCB-CA51+TEST-ONLY&serial-nr=206706423598360"))
            .willReturn(aResponse().withHeader("1", "2").withBody(byteBody).withStatus(400)));
    val actor = OnStage.theActor("Leonie Hütter");
    UnirestInstance client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new OCSPAbility(client, "/OCSPResponse"));
    val ocspResp =
        actor.asksFor(
            GetOCSPRequest.with(
                OcspRequestParameters.passConcreteParams(ISSUER_NAME, CERT_SERIAL_NR)));

    val step = OCSPRespVerifier.responseContainsDescription("Teststring");
    step.apply(ocspResp.getBody());
  }
}
