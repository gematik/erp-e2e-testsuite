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

package de.gematik.test.core.expectations.verifier;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static de.gematik.test.core.expectations.verifier.rawhttpverifier.RawHttpResponseVerifier.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import de.gematik.test.core.annotations.TestcaseId;
import de.gematik.test.core.expectations.requirements.CoverageReporter;
import de.gematik.test.core.expectations.requirements.ErpAfos;
import de.gematik.test.erezept.abilities.RawHttpAbility;
import de.gematik.test.erezept.actions.rawhttpactions.GetOcspListResponse;
import de.gematik.test.erezept.actions.rawhttpactions.VerifyRawHttp;
import kong.unirest.core.Unirest;
import kong.unirest.core.UnirestInstance;
import lombok.val;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.actors.Cast;
import net.serenitybdd.screenplay.actors.OnStage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@WireMockTest()
class VerifyRwaHttpTest {

  @RegisterExtension
  static WireMockExtension wiremockExtension =
      WireMockExtension.newInstance().options(WireMockConfiguration.wireMockConfig()).build();

  private Actor actor;
  private UnirestInstance client;

  @BeforeEach
  void setup() {
    OnStage.setTheStage(Cast.ofStandardActors());
    CoverageReporter.getInstance().startTestcase("don't care");
    actor = OnStage.theActor("Leonie");
    client = Unirest.spawnInstance();
    client.config().defaultBaseUrl(wiremockExtension.baseUrl());
    actor.can(new RawHttpAbility(client));
  }

  @Test
  @TestcaseId("ut_httpVerifier_01")
  @DisplayName("Positive Unit Test for an RawHttpVerify")
  void verifierShouldWork() {
    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPList"))
            .willReturn(
                aResponse().withHeader("1", "2").withBody("123456_TestBody").withStatus(200)));

    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));

    actor.attemptsTo(
        VerifyRawHttp.that(response, String.class)
            .hasResponseWith(containsHeaderWith("1", "2", ErpAfos.A_19248))
            .andHttp(returnCode(200))
            .isCorrect());
  }

  @Test
  @TestcaseId("ut_httpVerifier_02")
  @DisplayName("Positive Unit Test for an RawHttpVerify and his Body")
  void verifierShoulddeteectBodyContent() {
    wiremockExtension.stubFor(
        get(urlEqualTo("/OCSPList"))
            .willReturn(aResponse().withBody("123456TestBody").withStatus(200)));

    val response = assertDoesNotThrow(() -> actor.asksFor(new GetOcspListResponse()));

    actor.attemptsTo(
        VerifyRawHttp.that(response, String.class)
            .and(stringBodyContains("123456TestBody", ErpAfos.A_19248))
            .has(stringBodyContains("123456TestBody", ErpAfos.A_19248))
            .is(stringBodyContains("123456TestBody", ErpAfos.A_19248))
            .isCorrect());
  }
}
