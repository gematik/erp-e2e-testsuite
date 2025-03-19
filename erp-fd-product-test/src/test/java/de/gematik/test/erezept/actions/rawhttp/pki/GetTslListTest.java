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

package de.gematik.test.erezept.actions.rawhttp.pki;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import de.gematik.bbriccs.utils.ResourceLoader;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.erezept.abilities.TSLAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetTslList;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.val;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class GetTslListTest {

  private static final String CERT_BODY_PATH = "tslexamples/ECC_RSA_TSLCerts.txt";

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");
    val actor = OnStage.theActor("Leonie");
    UnirestInstance client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new TSLAbility(client, "/ECC-RSA_TSL-ref.xml"));
  }

  @AfterEach
  void tearDown() {
    OnStage.drawTheCurtain();
  }

  @Test
  void callCertificateShouldWork() {
    val respBody = ResourceLoader.readFileFromResource(CERT_BODY_PATH);
    wiremockExtension.stubFor(
        get(urlMatching("/ECC-RSA_TSL-.*"))
            .willReturn(
                aResponse().withHeader("1", "2").withBody(respBody.getBytes()).withStatus(200)));

    val actor = OnStage.theActorInTheSpotlight();
    assertDoesNotThrow(() -> actor.asksFor(GetTslList.direct()));
  }

  @Test
  void callCertificateShouldNotThrow() {
    wiremockExtension.stubFor(
        get(urlMatching("/ECC-RSA_TSL-.*"))
            .willReturn(aResponse().withHeader("1", "2").withStatus(200)));

    val actor = OnStage.theActorInTheSpotlight();
    assertDoesNotThrow(() -> actor.asksFor(GetTslList.direct()));
  }
}
