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
 */

package de.gematik.test.erezept.actions.rawhttp;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.CallCertificateFromBackend;
import de.gematik.test.erezept.actions.rawhttpactions.pki.PKICertificatesDTOEnvelop;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class CallCertificateFromBackendTest {

  private static final String CERT_BODY_PATH = "certexamples/ExampleCerts2.json";

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH);
    wiremockExtension.stubFor(
        get(urlEqualTo("/PKICertificates?currentRoot=GEM.RCA3%2520TEST-ONLY"))
            .willReturn(aResponse().withHeader("1", "2").withBody(respBody).withStatus(200)));
    val actor = OnStage.theActor("Leonie");
    UnirestInstance client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new RawHttpAbility(client));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  @TestcaseId("ut_httpResponseCallCertificate_01")
  @DisplayName("Positive Unit Test for an Get_Certificate with rootCa")
  void callCertificateShouldWork() {

    val actor = OnStage.theActorInTheSpotlight();
    val response =
        assertDoesNotThrow(
            () -> actor.asksFor(CallCertificateFromBackend.withRootCa("GEM.RCA3%20TEST-ONLY")));
  }

  @Test
  void shouldMap() throws JsonProcessingException {
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH);
    val mapper = new ObjectMapper();
    val content = mapper.readValue(respBody, PKICertificatesDTOEnvelop.class);
    assertFalse(content.getAddRoots().isEmpty());
    assertFalse(content.getCaCerts().isEmpty());
  }
}
